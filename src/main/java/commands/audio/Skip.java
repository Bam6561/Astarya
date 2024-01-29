package commands.audio;

import astarya.BotMessage;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import commands.audio.managers.AudioScheduler;
import commands.audio.managers.PlayerManager;
import commands.audio.objects.TrackQueueIndex;
import commands.owner.Settings;
import net.dv8tion.jda.api.entities.GuildVoiceState;

/**
 * Skip is a command invocation that skips the currently playing track in the audio player.
 *
 * @author Danny Nguyen
 * @version 1.7.12
 * @since 1.2.4
 */
public class Skip extends Command {
  public Skip() {
    this.name = "skip";
    this.aliases = new String[]{"skip", "s", "next"};
    this.help = "Skips the currently playing track.";
  }

  /**
   * Checks if the user is in the same voice channel as the bot to read a skip command request.
   *
   * @param ce command event
   * @throws NullPointerException user not in the same voice channel
   */
  @Override
  protected void execute(CommandEvent ce) {
    Settings.deleteInvoke(ce);

    GuildVoiceState userVoiceState = ce.getMember().getVoiceState();
    GuildVoiceState botVoiceState = ce.getGuild().getSelfMember().getVoiceState();

    try {
      boolean userInSameVoiceChannel = userVoiceState.getChannel().equals(botVoiceState.getChannel());
      if (userInSameVoiceChannel) {
        skipCurrentlyPlayingTrack(ce);
      } else {
        ce.getChannel().sendMessage(BotMessage.Failure.USER_NOT_IN_SAME_VC.text).queue();
      }
    } catch (NullPointerException e) {
      ce.getChannel().sendMessage(BotMessage.Failure.USER_NOT_IN_VC.text).queue();
    }
  }

  /**
   * Checks if there is a track currently playing to skip.
   *
   * @param ce command event
   */
  private void skipCurrentlyPlayingTrack(CommandEvent ce) {
    AudioScheduler audioScheduler = PlayerManager.getINSTANCE().getPlaybackManager(ce.getGuild()).audioScheduler;
    AudioPlayer audioPlayer = audioScheduler.getAudioPlayer();

    boolean currentlyPlayingTrack = !(audioPlayer.getPlayingTrack() == null);
    if (currentlyPlayingTrack) {
      addSkippedTrackToSkippedTracksStack(ce, audioScheduler, audioPlayer);
      sendSkipConfirmation(ce);
    } else {
      ce.getChannel().sendMessage(BotMessage.Failure.SKIP_NOTHING_TO_SKIP.text).queue();
    }
  }

  /**
   * Adds the currently playing track to the skipped track stack and skips it.
   *
   * @param ce             command event
   * @param audioScheduler bot's audio scheduler
   * @param audioPlayer    bot's audio player
   */
  private void addSkippedTrackToSkippedTracksStack(CommandEvent ce, AudioScheduler audioScheduler, AudioPlayer audioPlayer) {
    String requester = "[" + ce.getAuthor().getAsTag() + "]";
    audioScheduler.addToSkippedTracksStack(new TrackQueueIndex(audioPlayer.getPlayingTrack().makeClone(), requester));
    audioScheduler.nextTrack();
  }

  /**
   * Sends confirmation the track was skipped.
   *
   * @param ce command event
   */
  private void sendSkipConfirmation(CommandEvent ce) {
    StringBuilder skipTrackConfirmation = new StringBuilder();
    skipTrackConfirmation.append("**Skip:** [").append(ce.getAuthor().getAsTag()).append("]");
    ce.getChannel().sendMessage(skipTrackConfirmation).queue();
  }
}
