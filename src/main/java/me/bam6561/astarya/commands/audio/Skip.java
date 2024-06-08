package me.bam6561.astarya.commands.audio;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import me.bam6561.astarya.commands.audio.managers.AudioScheduler;
import me.bam6561.astarya.commands.audio.managers.PlayerManager;
import me.bam6561.astarya.commands.owner.Settings;
import me.bam6561.astarya.enums.BotMessage;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;

/**
 * Command invocation that skips the currently playing track in the audio player.
 *
 * @author Danny Nguyen
 * @version 1.8.12
 * @since 1.2.4
 */
public class Skip extends Command {
  /**
   * Associates the command with its properties.
   */
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

    AudioChannelUnion userChannel = ce.getMember().getVoiceState().getChannel();
    AudioChannelUnion botChannel = ce.getGuild().getSelfMember().getVoiceState().getChannel();

    if (userChannel == null) {
      ce.getChannel().sendMessage(BotMessage.USER_NOT_IN_VC.getMessage()).queue();
      return;
    }

    if (userChannel.equals(botChannel)) {
      skipCurrentlyPlayingTrack(ce);
    } else {
      ce.getChannel().sendMessage(BotMessage.USER_NOT_IN_SAME_VC.getMessage()).queue();
    }
  }

  /**
   * Checks if there is a track currently playing to skip before skipping the track.
   *
   * @param ce command event
   */
  private void skipCurrentlyPlayingTrack(CommandEvent ce) {
    AudioScheduler audioScheduler = PlayerManager.getINSTANCE().getPlaybackManager(ce.getGuild()).audioScheduler;
    AudioPlayer audioPlayer = audioScheduler.getAudioPlayer();

    boolean currentlyPlayingTrack = !(audioPlayer.getPlayingTrack() == null);
    if (currentlyPlayingTrack) {
      String requester = "[" + ce.getAuthor().getAsTag() + "]";
      audioScheduler.addToSkippedTracks(new TrackQueueIndex(audioPlayer.getPlayingTrack().makeClone(), requester));
      audioScheduler.nextTrack();

      StringBuilder skipTrackConfirmation = new StringBuilder();
      skipTrackConfirmation.append("**Skip:** [").append(ce.getAuthor().getAsTag()).append("]");
      ce.getChannel().sendMessage(skipTrackConfirmation).queue();
    } else {
      ce.getChannel().sendMessage("Nothing to skip.").queue();
    }
  }
}
