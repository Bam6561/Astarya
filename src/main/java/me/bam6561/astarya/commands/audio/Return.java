package me.bam6561.astarya.commands.audio;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import me.bam6561.astarya.commands.audio.managers.AudioScheduler;
import me.bam6561.astarya.commands.audio.managers.PlayerManager;
import me.bam6561.astarya.commands.owner.Settings;
import me.bam6561.astarya.enums.BotMessage;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;

import java.util.LinkedList;

/**
 * Command invocation that displays {@link AudioScheduler#getSkippedTracks() skipped tracks} and provides
 * an option to return a recently skipped track to the {@link AudioScheduler#getTrackQueue() queue}.
 *
 * @author Danny Nguyen
 * @version 1.8.12
 * @since 1.5.2
 */
public class Return extends Command {
  /**
   * Associates the command with its properties.
   */
  public Return() {
    this.name = "return";
    this.aliases = new String[]{"return", "ret"};
    this.arguments = "[0]RecentlySkipped [1]SkippedTrackNumber";
    this.help = "Returns a recently skipped track to the track queue.";
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
      new ReturnRequest(ce).interpretRequest();
    } else {
      ce.getChannel().sendMessage(BotMessage.USER_NOT_IN_SAME_VC.getMessage()).queue();
    }
  }

  /**
   * Represents a track to return request.
   *
   * @param ce command event
   * @author Danny Nguyen
   * @version 1.8.12
   * @since 1.8.12
   */
  private record ReturnRequest(CommandEvent ce) {
    /**
     * Either displays the skipped tracks or returns a skipped track
     * back to the {@link AudioScheduler#getTrackQueue() queue}.
     */
    private void interpretRequest() {
      String[] parameters = ce.getMessage().getContentRaw().split("\\s");
      int numberOfParameters = parameters.length - 1;

      switch (numberOfParameters) {
        case 0 -> sendSkippedTracks();
        case 1 -> {
          try {
            processReturnTrackRequest(Integer.parseInt(parameters[1]));
          } catch (NumberFormatException e) {
            ce.getChannel().sendMessage("Provide number to be returned.").queue();
          }
        }
        default -> ce.getChannel().sendMessage(BotMessage.INVALID_NUMBER_OF_PARAMETERS.getMessage()).queue();
      }
    }

    /**
     * Sends an embed containing recently {@link AudioScheduler#getSkippedTracks()}.
     */
    private void sendSkippedTracks() {
      LinkedList<TrackQueueIndex> skippedTracks = PlayerManager.getINSTANCE().getPlaybackManager(ce.getGuild()).audioScheduler.getSkippedTracks();
      if (!skippedTracks.isEmpty()) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setAuthor("Recently Skipped");
        embed.addField("**Tracks:**", (buildSkippedTracksPage(skippedTracks)), false);
        Settings.sendEmbed(ce, embed);
      } else {
        ce.getChannel().sendMessage("No recently skipped tracks.").queue();
      }
    }

    /**
     * Checks if user provided integer within range of {@link AudioScheduler#getSkippedTracks() skipped tracks}
     * before returning a skipped track to the {@link AudioScheduler#getTrackQueue() queue}.
     *
     * @param skippedTracksIndex track index in {@link AudioScheduler#getSkippedTracks()} to be returned
     */
    private void processReturnTrackRequest(int skippedTracksIndex) {
      try {
        AudioScheduler audioScheduler = PlayerManager.getINSTANCE().getPlaybackManager(ce.getGuild()).audioScheduler;
        LinkedList<TrackQueueIndex> skippedTracks = audioScheduler.getSkippedTracks();

        // Displayed indices to users are different from data index, so subtract 1
        AudioTrack skippedTrack = skippedTracks.get(skippedTracksIndex - 1).getAudioTrack();

        String requester = "[" + ce.getAuthor().getAsTag() + "]";
        audioScheduler.queue(skippedTrack, requester);
        skippedTracks.remove(skippedTracksIndex - 1);

        StringBuilder returnTrackConfirmation = new StringBuilder();
        String trackDuration = TrackTime.convertLong(skippedTrack.getDuration());
        returnTrackConfirmation.append("**Returned:** `")
            .append(skippedTrack.getInfo().title)
            .append("` {*").append(trackDuration).append("*} ")
            .append("[").append(ce.getAuthor().getAsTag()).append("]");
        ce.getChannel().sendMessage(returnTrackConfirmation).queue();
      } catch (IndexOutOfBoundsException e) {
        ce.getChannel().sendMessage(BotMessage.INVALID_QUEUE_NUMBER.getMessage()).queue();
      }
    }

    /**
     * Creates the display text to represent the {@link AudioScheduler#getSkippedTracks() skipped tracks}.
     * <p>
     * Skipped tracks can only contain up to 10 tracks.
     * <p>
     * As another skipped track is added, all previously existing tracks indices are
     * incremented by 1. After exceeding the amount, the least recent track is removed.
     *
     * @param skippedTracks {@link AudioScheduler#getSkippedTracks()}
     * @return string representing {@link AudioScheduler#getSkippedTracks() skipped tracks}
     */
    private String buildSkippedTracksPage(LinkedList<TrackQueueIndex> skippedTracks) {
      StringBuilder skippedTracksPage = new StringBuilder();
      for (int i = 0; i < skippedTracks.size(); i++) {
        String trackDuration = TrackTime.convertLong(skippedTracks.get(i).getAudioTrack().getDuration());
        skippedTracksPage.append("**[").append(i + 1).append("]** `").
            append(skippedTracks.get(i).getAudioTrack().getInfo().title)
            .append("` {*").append(trackDuration).append("*} ").append("\n");
      }
      return skippedTracksPage.toString();
    }
  }
}
