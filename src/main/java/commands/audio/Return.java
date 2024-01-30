package commands.audio;

import astarya.BotMessage;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import commands.audio.managers.AudioScheduler;
import commands.audio.managers.PlayerManager;
import commands.audio.objects.TrackQueueIndex;
import commands.audio.utility.TrackTime;
import commands.owner.Settings;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.GuildVoiceState;

import java.util.LinkedList;
import java.util.List;

/**
 * Return is a command invocation that displays skipped tracks and
 * provides an option to return a recently skipped track to the queue.
 *
 * @author Danny Nguyen
 * @version 1.8.0
 * @since 1.5.2
 */
public class Return extends Command {
  public Return() {
    this.name = "return";
    this.aliases = new String[]{"return", "ret"};
    this.arguments = "[0]RecentlySkipped [1]SkippedTrackNumber";
    this.help = "Returns a recently skipped track to the track queue.";
  }

  private enum Success {
    RETURN_NO_SKIPPED_TRACKS("No recently skipped tracks.");

    public final String text;

    Success(String text) {
      this.text = text;
    }
  }

  private enum Failure {
    RETURN_SPECIFY("Provide number to be returned.");

    public final String text;

    Failure(String text) {
      this.text = text;
    }
  }

  /**
   * Checks if the user is in the same voice channel as the bot to read a return command request.
   *
   * @param ce command event
   * @throws NullPointerException user not in same voice channel
   */
  @Override
  protected void execute(CommandEvent ce) {
    Settings.deleteInvoke(ce);

    GuildVoiceState userVoiceState = ce.getMember().getVoiceState();
    GuildVoiceState botVoiceState = ce.getGuild().getSelfMember().getVoiceState();

    try {
      boolean userInSameVoiceChannel = userVoiceState.getChannel().equals(botVoiceState.getChannel());
      if (userInSameVoiceChannel) {
        interpretReturnTrackRequest(ce);
      } else {
        ce.getChannel().sendMessage(BotMessage.Failure.USER_NOT_IN_SAME_VC.text).queue();
      }
    } catch (NullPointerException e) {
      ce.getChannel().sendMessage(BotMessage.Failure.USER_NOT_IN_VC.text).queue();
    }
  }

  /**
   * Either displays the skipped tracks or returns a skipped track back to the queue.
   *
   * @param ce command event
   * @throws NumberFormatException user provided non-integer value
   */
  private void interpretReturnTrackRequest(CommandEvent ce) {
    String[] parameters = ce.getMessage().getContentRaw().split("\\s");
    int numberOfParameters = parameters.length - 1;

    switch (numberOfParameters) {
      case 0 -> sendSkippedTracks(ce);
      case 1 -> {
        try {
          int returnIndex = Integer.parseInt(parameters[1]);
          processReturnTrackRequest(ce, returnIndex);
        } catch (NumberFormatException e) {
          ce.getChannel().sendMessage(Failure.RETURN_SPECIFY.text).queue();
        }
      }
      default -> ce.getChannel().sendMessage(BotMessage.Failure.INVALID_NUMBER_OF_PARAMETERS.text).queue();
    }
  }

  /**
   * Sends an embed containing recently skipped tracks.
   *
   * @param ce command event
   */
  private void sendSkippedTracks(CommandEvent ce) {
    List<TrackQueueIndex> skippedTracks =
        PlayerManager.getINSTANCE().getPlaybackManager(ce.getGuild()).audioScheduler.getSkippedTracks();

    if (!skippedTracks.isEmpty()) {
      EmbedBuilder display = new EmbedBuilder();
      display.setAuthor("Recently Skipped");
      display.addField("**Tracks:**", (buildSkippedTracksPage(skippedTracks)), false);
      Settings.sendEmbed(ce, display);
    } else {
      ce.getChannel().sendMessage(Success.RETURN_NO_SKIPPED_TRACKS.text).queue();
    }
  }

  /**
   * Checks if user provided integer within range of skipped
   * tracks before returning a skipped track to the queue.
   *
   * @param ce                 command event
   * @param skippedTracksIndex track index in skipped tracks to be returned
   * @throws IndexOutOfBoundsException user provided index out of range of skipped tracks
   */
  private void processReturnTrackRequest(CommandEvent ce, int skippedTracksIndex) {
    try {
      AudioScheduler audioScheduler = PlayerManager.getINSTANCE().getPlaybackManager(ce.getGuild()).audioScheduler;
      LinkedList<TrackQueueIndex> skippedTracks = audioScheduler.getSkippedTracks();

      // Displayed indices to users are different from data index, so subtract 1
      AudioTrack skippedTrack = skippedTracks.get(skippedTracksIndex - 1).getAudioTrack();

      returnSkippedTrack(ce, skippedTracksIndex, audioScheduler, skippedTracks, skippedTrack);
      sendReturnConfirmation(ce, skippedTrack);
    } catch (IndexOutOfBoundsException e) {
      ce.getChannel().sendMessage(BotMessage.Failure.INVALID_QUEUE_NUMBER.text).queue();
    }
  }

  /**
   * Creates the display text to represent the skipped tracks.
   * <p>
   * Skipped tracks can only contain up to 10 tracks. As another skipped
   * track is added, all previously existing tracks indices are incremented
   * by 1. After exceeding the amount, the least recent track is removed.
   * </p>
   *
   * @param skippedTracks skipped tracks
   * @return string representing skipped tracks
   */
  private String buildSkippedTracksPage(List<TrackQueueIndex> skippedTracks) {
    StringBuilder skippedTracksPage = new StringBuilder();
    for (int i = 0; i < skippedTracks.size(); i++) {
      String trackDuration = TrackTime.convertLong(skippedTracks.get(i).getAudioTrack().getDuration());
      skippedTracksPage.append("**[").append(i + 1).append("]** `").
          append(skippedTracks.get(i).getAudioTrack().getInfo().title)
          .append("` {*").append(trackDuration).append("*} ").append("\n");
    }
    return skippedTracksPage.toString();
  }

  /**
   * Returns a track from recently skipped tracks.
   *
   * @param ce                 command event
   * @param skippedTracksIndex index of the skipped tracks to be returned
   * @param audioScheduler     audio scheduler
   * @param skippedTracks      skipped tracks
   * @param skippedTrack       chosen skipped track to be returned
   */
  private void returnSkippedTrack(CommandEvent ce, int skippedTracksIndex, AudioScheduler audioScheduler,
                                  List<TrackQueueIndex> skippedTracks, AudioTrack skippedTrack) {
    String requester = "[" + ce.getAuthor().getAsTag() + "]";
    audioScheduler.queue(skippedTrack, requester);
    skippedTracks.remove(skippedTracksIndex - 1);
  }

  /**
   * Sends confirmation the skipped track was returned to the queue.
   *
   * @param ce           command event
   * @param skippedTrack chosen skipped track to be returned to the queue
   */
  private void sendReturnConfirmation(CommandEvent ce, AudioTrack skippedTrack) {
    StringBuilder returnTrackConfirmation = new StringBuilder();
    String trackDuration = TrackTime.convertLong(skippedTrack.getDuration());
    returnTrackConfirmation.append("**Returned:** `")
        .append(skippedTrack.getInfo().title)
        .append("` {*").append(trackDuration).append("*} ")
        .append("[").append(ce.getAuthor().getAsTag()).append("]");
    ce.getChannel().sendMessage(returnTrackConfirmation).queue();
  }
}
