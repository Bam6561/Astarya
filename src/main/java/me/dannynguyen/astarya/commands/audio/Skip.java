package me.dannynguyen.astarya.commands.audio;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import me.dannynguyen.astarya.commands.audio.managers.AudioScheduler;
import me.dannynguyen.astarya.commands.audio.managers.PlayerManager;
import me.dannynguyen.astarya.commands.owner.Settings;
import me.dannynguyen.astarya.enums.BotMessage;
import net.dv8tion.jda.api.entities.GuildVoiceState;

/**
 * Command invocation that skips the currently playing track in the audio player.
 *
 * @author Danny Nguyen
 * @version 1.8.1
 * @since 1.2.4
 */
public class Skip extends Command {
  public Skip() {
    this.name = "skip";
    this.aliases = new String[]{"skip", "s", "next"};
    this.help = "Skips the currently playing track.";
  }

  /**
   * Checks if the user is in the same voice channel as the bot to read the command request.
   *
   * @param ce command event
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
        ce.getChannel().sendMessage(BotMessage.USER_NOT_IN_SAME_VC.getMessage()).queue();
      }
    } catch (NullPointerException e) {
      ce.getChannel().sendMessage(BotMessage.USER_NOT_IN_VC.getMessage()).queue();
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
      addTrackToSkippedTracks(ce, audioScheduler, audioPlayer);
      sendSkipConfirmation(ce);
    } else {
      ce.getChannel().sendMessage(Failure.NOTHING_TO_SKIP.text).queue();
    }
  }

  /**
   * Skips the currently playing track and adds it to skipped tracks.
   *
   * @param ce             command event
   * @param audioScheduler audio scheduler
   * @param audioPlayer    audio player
   */
  private void addTrackToSkippedTracks(CommandEvent ce, AudioScheduler audioScheduler, AudioPlayer audioPlayer) {
    String requester = "[" + ce.getAuthor().getAsTag() + "]";
    audioScheduler.addToSkippedTracks(new TrackQueueIndex(audioPlayer.getPlayingTrack().makeClone(), requester));
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

  private enum Failure {
    NOTHING_TO_SKIP("Nothing to skip.");

    public final String text;

    Failure(String text) {
      this.text = text;
    }
  }
}
