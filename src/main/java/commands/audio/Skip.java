package commands.audio;

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
 * @version 1.7.2
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
   * @param ce object containing information about the command event
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
        ce.getChannel().sendMessage("User not in the same voice channel.").queue();
      }
    } catch (NullPointerException e) {
      ce.getChannel().sendMessage("User not in a voice channel.").queue();
    }
  }

  /**
   * Checks if there is a track currently playing to skip.
   *
   * @param ce object containing information about the command event
   */
  private void skipCurrentlyPlayingTrack(CommandEvent ce) {
    AudioScheduler audioScheduler = PlayerManager.getINSTANCE().getPlaybackManager(ce.getGuild()).audioScheduler;
    AudioPlayer audioPlayer = audioScheduler.getAudioPlayer();

    boolean currentlyPlayingTrack = !(audioPlayer.getPlayingTrack() == null);
    if (currentlyPlayingTrack) {
      addSkippedTrackToSkippedTracksStack(ce, audioScheduler, audioPlayer);
      sendSkipConfirmation(ce);
    } else {
      ce.getChannel().sendMessage("Nothing to skip.").queue();
    }
  }

  /**
   * Adds the currently playing track to the skipped track stack and skips it.
   *
   * @param ce             object containing information about the command event
   * @param audioScheduler bot's audioscheduler
   * @param audioPlayer    bot's audioplayer
   */
  private void addSkippedTrackToSkippedTracksStack(CommandEvent ce, AudioScheduler audioScheduler, AudioPlayer audioPlayer) {
    String requester = "[" + ce.getAuthor().getAsTag() + "]";
    audioScheduler.addToSkippedTracksStack(new TrackQueueIndex(audioPlayer.getPlayingTrack().makeClone(), requester));
    audioScheduler.nextTrack();
  }

  /**
   * Sends confirmation the track was skipped.
   *
   * @param ce object containing information about the command event
   */
  private void sendSkipConfirmation(CommandEvent ce) {
    StringBuilder skipTrackConfirmation = new StringBuilder();
    skipTrackConfirmation.append("**Skip:** [").append(ce.getAuthor().getAsTag()).append("]");
    ce.getChannel().sendMessage(skipTrackConfirmation).queue();
  }
}
