package commands.audio;

import astarya.BotMessage;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import commands.audio.managers.AudioScheduler;
import commands.audio.managers.PlayerManager;
import commands.audio.utility.TrackTime;
import commands.owner.Settings;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.concurrent.TimeUnit;

/**
 * SearchTrack is a command invocation that searches for a track
 * to add to the queue using a query of user provided parameters.
 *
 * @author Danny Nguyen
 * @version 1.8.1
 * @since 1.2.15
 */
public class SearchTrack extends Command {
  private final EventWaiter waiter;
  private long invokerUserId;

  public SearchTrack(EventWaiter waiter) {
    this.name = "searchtrack";
    this.aliases = new String[]{"searchtrack", "search", "st"};
    this.arguments = "[1 ++]YouTubeQuery";
    this.help = "Searches for a track to add to the track queue.";
    this.waiter = waiter;
    this.invokerUserId = 0;
  }

  private enum Failure {
    SPECIFY_RESULT_NUMBER("Provide result number."),
    RESPONSE_EXCEED_RANGE("Responses must be in range of 1-5."),
    RESPONSE_TIMED_OUT("No response. Search timed out.");

    public final String text;

    Failure(String text) {
      this.text = text;
    }
  }

  /**
   * Checks if the user is in the same voice channel as the bot to read a searchTrack command request.
   * If so, the command locks the potential response to the requester and awaits for their response.
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
        readSearchTrackRequest(ce);
        awaitUserResponse(ce);
      } else {
        ce.getChannel().sendMessage(BotMessage.Failure.USER_NOT_IN_SAME_VC.text).queue();
      }
    } catch (NullPointerException e) {
      ce.getChannel().sendMessage(BotMessage.Failure.USER_NOT_IN_VC.text).queue();
    }
  }

  /**
   * Checks if the searchTrack command request was formatted correctly before querying YouTube for match results.
   *
   * @param ce command event
   */
  private void readSearchTrackRequest(CommandEvent ce) {
    String[] parameters = ce.getMessage().getContentRaw().split("\\s");
    int numberOfParameters = parameters.length - 1;

    if (numberOfParameters > 0) {
      setInvokerUserId(Long.parseLong(ce.getAuthor().getId())); // Lock searchTrack command request to requester
      queryYouTube(ce, parameters, numberOfParameters);
    } else {
      ce.getChannel().sendMessage(BotMessage.Failure.INVALID_NUMBER_OF_PARAMETERS.text).queue();
    }
  }

  /**
   * Awaits for user response to the searchTrack command request. After a response
   * or period of inactivity, the locked status is removed from the requester.
   *
   * @param ce command event
   * @throws NumberFormatException user provided non-integer value
   */
  private void awaitUserResponse(CommandEvent ce) {
    ce.getChannel().sendTyping().queue(response -> waiter.waitForEvent(MessageReceivedEvent.class,
        // Message sent matches invoker user's Id
        w -> Long.parseLong(w.getMessage().getAuthor().getId()) == getInvokerUserId(),
        w -> {
          setInvokerUserId(0);
          readUserResponse(ce, w);
        }, 15, TimeUnit.SECONDS, () -> { // Timeout
          setInvokerUserId(0);
          ce.getChannel().sendMessage(Failure.RESPONSE_TIMED_OUT.text).queue();
        }));
  }

  /**
   * Gets YouTube search results from the search query.
   *
   * @param ce                 command event
   * @param parameters         user provided parameters
   * @param numberOfParameters number of user provided parameters
   */
  private void queryYouTube(CommandEvent ce, String[] parameters, int numberOfParameters) {
    StringBuilder searchQuery = new StringBuilder();
    for (int i = 1; i < numberOfParameters; i++) {
      searchQuery.append(parameters[i]);
    }
    String youtubeSearchQuery = "ytsearch:" + String.join(" ", searchQuery);
    PlayerManager.getINSTANCE().searchAudioTrack(ce, youtubeSearchQuery);
  }

  /**
   * Checks if the user's response is an integer.
   *
   * @param ce command event
   * @param w  message received event
   * @throws NumberFormatException user provided non-integer response
   */
  private void readUserResponse(CommandEvent ce, MessageReceivedEvent w) {
    String[] parameters = w.getMessage().getContentRaw().split("\\s");
    try {
      processUserResponse(ce, Integer.parseInt(parameters[0]));
    } catch (NumberFormatException e) {
      ce.getChannel().sendMessage(Failure.SPECIFY_RESULT_NUMBER.text).queue();
    }
  }

  /**
   * Queues the user's track choice from YouTube results.
   *
   * @param ce                      command event
   * @param searchTrackResultsIndex track index in the searchTrackResults to be queued
   * @throws IndexOutOfBoundsException user provided an integer value out of range of 1-5
   */
  private void processUserResponse(CommandEvent ce, int searchTrackResultsIndex) {
    try {
      AudioScheduler audioScheduler = PlayerManager.getINSTANCE().getPlaybackManager(ce.getGuild()).audioScheduler;
      AudioTrack[] searchTrackResults = PlayerManager.getINSTANCE().getSearchTrackResults();

      // Displayed indices to users are different from data index, so subtract 1
      AudioTrack track = searchTrackResults[searchTrackResultsIndex - 1];
      String requester = "[" + ce.getAuthor().getAsTag() + "]";

      audioScheduler.queue(track, requester);
      sendSearchTrackConfirmation(ce, track, requester);
    } catch (IndexOutOfBoundsException e) {
      setInvokerUserId(0);
      ce.getChannel().sendMessage(Failure.RESPONSE_EXCEED_RANGE.text).queue();
    }
  }

  /**
   * Sends confirmation the chosen track result was added to the queue.
   *
   * @param ce        command event
   * @param track     chosen track from the YouTube results
   * @param requester user who invoked the command
   */
  private void sendSearchTrackConfirmation(CommandEvent ce, AudioTrack track, String requester) {
    String trackDuration = TrackTime.convertLong(track.getDuration());
    StringBuilder userResponseConfirmation = new StringBuilder();
    userResponseConfirmation.append("**Added:** `").append(track.getInfo().title).
        append("` {*").append(trackDuration).append("*} ").append(requester);
    ce.getChannel().sendMessage(userResponseConfirmation).queue();
  }

  private long getInvokerUserId() {
    return this.invokerUserId;
  }

  private void setInvokerUserId(long invokerUserId) {
    this.invokerUserId = invokerUserId;
  }
}