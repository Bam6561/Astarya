package me.bam6561.astarya.commands.audio;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import me.bam6561.astarya.commands.audio.managers.AudioScheduler;
import me.bam6561.astarya.commands.audio.managers.PlayerManager;
import me.bam6561.astarya.commands.owner.Settings;
import me.bam6561.astarya.enums.BotMessage;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Command invocation that searches for a track to add to the
 * {@link AudioScheduler#getTrackQueue() queue} using a query of user provided parameters.
 *
 * @author Danny Nguyen
 * @version 1.9.3
 * @since 1.2.15
 */
public class SearchTrack extends Command {
  /**
   * Event waiter.
   */
  private final EventWaiter waiter;

  /**
   * Command user's id.
   */
  private long invokerUserId = -1;

  /**
   * Associates the command with its properties.
   *
   * @param waiter event waiter
   */
  public SearchTrack(@NotNull EventWaiter waiter) {
    this.waiter = Objects.requireNonNull(waiter, "Null waiter");
    this.name = "searchtrack";
    this.aliases = new String[]{"searchtrack", "search", "st"};
    this.arguments = "[1 ++]YouTubeQuery";
    this.help = "Searches for a track to add to the track queue.";
  }

  /**
   * Checks if the user is in the same voice channel as the bot to read the command request.
   * <p>
   * If so, the command locks the potential response to the requester and awaits for their response.
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
      new SearchTrackRequest(ce).interpretRequest();
    } else {
      ce.getChannel().sendMessage(BotMessage.USER_NOT_IN_SAME_VC.getMessage()).queue();
    }
  }

  /**
   * Represents a track search query.
   *
   * @author Danny Nguyen
   * @version 1.8.12
   * @since 1.8.12
   */
  private class SearchTrackRequest {
    /**
     * Command event.
     */
    private final CommandEvent ce;

    /**
     * Associates a search track request with its command event.
     *
     * @param ce command event
     */
    SearchTrackRequest(CommandEvent ce) {
      this.ce = ce;
    }

    /**
     * Checks if the command request was formatted correctly before querying YouTube for match results.
     */
    private void interpretRequest() {
      String[] parameters = ce.getMessage().getContentRaw().split("\\s");
      int numberOfParameters = parameters.length - 1;

      if (numberOfParameters > 0) {
        invokerUserId = Long.parseLong(ce.getAuthor().getId()); // Lock searchTrack command request to requester

        StringBuilder searchQuery = new StringBuilder();
        for (int i = 1; i < numberOfParameters; i++) {
          searchQuery.append(parameters[i]);
        }
        String youtubeSearchQuery = "ytsearch:" + String.join(" ", searchQuery);
        PlayerManager.getINSTANCE().searchAudioTrack(ce, youtubeSearchQuery);

        awaitUserResponse();
      } else {
        ce.getChannel().sendMessage(BotMessage.INVALID_NUMBER_OF_PARAMETERS.getMessage()).queue();
      }
    }

    /**
     * Awaits for user response. After a response or period of
     * inactivity, the locked status is removed from the requester.
     */
    private void awaitUserResponse() {
      ce.getChannel().sendTyping().queue(response -> waiter.waitForEvent(MessageReceivedEvent.class,
          // Message sent matches invoker user's Id
          w -> Long.parseLong(w.getMessage().getAuthor().getId()) == invokerUserId,
          w -> {
            invokerUserId = -1;
            readUserResponse(w);
          }, 15, TimeUnit.SECONDS, () -> { // Timeout
            invokerUserId = -1;
            ce.getChannel().sendMessage("No response. Search timed out.").queue();
          }));
    }

    /**
     * Checks if the user's response is an integer before
     * queueing the user's track choice from YouTube results.
     *
     * @param w message received event
     */
    private void readUserResponse(MessageReceivedEvent w) {
      String[] parameters = w.getMessage().getContentRaw().split("\\s");
      try {
        int searchTrackResultsIndex = Integer.parseInt(parameters[0]);
        try {
          AudioScheduler audioScheduler = PlayerManager.getINSTANCE().getPlaybackManager(ce.getGuild()).audioScheduler;
          AudioTrack[] searchTrackResults = PlayerManager.getINSTANCE().getSearchTrackResults();

          // Displayed indices to users are different from data index, so subtract 1
          AudioTrack track = searchTrackResults[searchTrackResultsIndex - 1];
          String requester = "[" + ce.getAuthor().getAsTag() + "]";

          audioScheduler.queue(track, requester);

          StringBuilder userResponseConfirmation = new StringBuilder();
          userResponseConfirmation.append("**Added:** `")
              .append(track.getInfo().title)
              .append("` {*").append(TrackTime.convertLong(track.getDuration())).append("*} ")
              .append(requester);
          ce.getChannel().sendMessage(userResponseConfirmation).queue();
        } catch (IndexOutOfBoundsException e) {
          invokerUserId = -1;
          ce.getChannel().sendMessage("Responses must be in range of 1-5.").queue();
        }
      } catch (NumberFormatException e) {
        ce.getChannel().sendMessage("Provide result number.").queue();
      }
    }
  }
}