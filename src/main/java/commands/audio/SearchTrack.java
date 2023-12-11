package commands.audio;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import commands.audio.managers.AudioScheduler;
import commands.audio.managers.PlaybackManager;
import commands.audio.managers.PlayerManager;
import commands.audio.objects.TrackQueueIndex;
import commands.owner.Settings;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 * SearchTrack is a command invocation that searches for a track
 * to add to the queue using a query of user provided parameters.
 *
 * @author Danny Nguyen
 * @version 1.7.0
 * @since 1.2.15
 */
public class SearchTrack extends Command {
  private EventWaiter waiter;
  private long invokerUserId;

  public SearchTrack(EventWaiter waiter) {
    this.name = "searchtrack";
    this.aliases = new String[]{"searchtrack", "search", "st"};
    this.arguments = "[1++]YouTubeQuery";
    this.help = "Searches for a track to add to the queue.";
    this.waiter = waiter;
    this.invokerUserId = 0;
  }

  /**
   * Determines whether the user is in the same voice channel as the bot to process a searchTrack
   * command request, locks the potential response to the requester, and awaits for their response.
   *
   * @param ce object containing information about the command event
   * @throws InterruptedException thread sleep is interrupted
   */
  @Override
  protected void execute(CommandEvent ce) {
    Settings.deleteInvoke(ce);

    GuildVoiceState userVoiceState = ce.getMember().getVoiceState();
    GuildVoiceState botVoiceState = ce.getGuild().getSelfMember().getVoiceState();

    try {
      boolean userInSameVoiceChannel = userVoiceState.getChannel().equals(botVoiceState.getChannel());
      if (userInSameVoiceChannel) {
        processSearchTrackRequest(ce);
        setInvokerUserId(Long.parseLong(ce.getAuthor().getId())); // Lock searchTrack command request to requester
        try { // Small delay to ignore command invocation as user response
          Thread.sleep(50);
        } catch (InterruptedException e) {
        }
        awaitUserResponse(ce);
      } else {
        ce.getChannel().sendMessage("User not in the same voice channel.").queue();
      }
    } catch (NullPointerException e) {
      ce.getChannel().sendMessage("User not in a voice channel.").queue();
    }
  }

  /**
   * Returns a list of YouTube search results from the user provided search query.
   *
   * @param ce object containing information about the command event
   */
  private void processSearchTrackRequest(CommandEvent ce) {
    String[] parameters = ce.getMessage().getContentRaw().split("\\s");
    int numberOfParameters = parameters.length - 1;

    if (numberOfParameters > 0) {
      // Input search query into YouTube
      StringBuilder searchQuery = new StringBuilder();
      for (int i = 1; i < numberOfParameters; i++) {
        searchQuery.append(parameters[i]);
      }
      String youtubeSearchQuery = "ytsearch:" + String.join(" ", searchQuery);
      PlayerManager.getINSTANCE().searchAudioTrack(ce, youtubeSearchQuery);
    } else {
      ce.getChannel().sendMessage("Invalid number of parameters.").queue();
    }
  }

  /**
   * Awaits for user response to searchTrack command request. After a response
   * or period of inactivity, the locked status is removed from the requester.
   *
   * @param ce object containing information about the command event
   * @throws NumberFormatException user provided non-integer value
   */
  private void awaitUserResponse(CommandEvent ce) {
    waiter.waitForEvent(MessageReceivedEvent.class,
        // Message sent matches invoker user's Id
        w -> Long.parseLong(w.getMessage().getAuthor().getId()) == getInvokerUserId(),
        w -> {
          setInvokerUserId(0);
          String[] parameters = w.getMessage().getContentRaw().split("\\s");
          try {
            handleUserResponse(ce, Integer.parseInt(parameters[0]));
          } catch (NumberFormatException e) {
            setInvokerUserId(0);
            ce.getChannel().sendMessage("Responses must be an integer.").queue();
          }
        }, 15, TimeUnit.SECONDS, () -> { // Timeout
          setInvokerUserId(0);
          ce.getChannel().sendMessage("No response. Search timed out.").queue();
        });
  }

  /**
   * Queues the user's track choice from results sent by searchTrackRequest.
   *
   * @param ce                      object containing information about the command event
   * @param searchTrackResultsIndex track index in the searchTrackResults to be queued
   * @throws IndexOutOfBoundsException user provided an integer value out of range of 1-5
   */
  private void handleUserResponse(CommandEvent ce, int searchTrackResultsIndex) {
    try {
      PlaybackManager playbackManager = PlayerManager.getINSTANCE().getPlaybackManager(ce.getGuild());
      AudioScheduler audioScheduler = playbackManager.audioScheduler;
      ArrayList<AudioTrack> searchTrackResults = PlayerManager.getINSTANCE().getSearchTrackResults();
      //ArrayList<AudioTrack> searchTrackResults = PlayerManager.getINSTANCE().getSearchTrackResults();

      // Displayed index to users are different from data index, so subtract 1
      AudioTrack track = searchTrackResults.get(searchTrackResultsIndex - 1);
      String requester = "[" + ce.getAuthor().getAsTag() + "]";


      audioScheduler.getTrackQueue().add(new TrackQueueIndex(track, requester));

      // SearchTrack confirmation
      String trackDuration = longTimeConversion(track.getDuration());
      StringBuilder userResponseConfirmation = new StringBuilder();
      userResponseConfirmation.append("**Added:** `").append(track.getInfo().title).
          append("` {*").append(trackDuration).append("*} ").append(requester);
      ce.getChannel().sendMessage(userResponseConfirmation).queue();
    } catch (IndexOutOfBoundsException e) {
      setInvokerUserId(0);
      ce.getChannel().sendMessage("Responses must be in range of 1-5.").queue();
    }
  }

  /**
   * Converts long duration to conventional readable time.
   *
   * @param longTime duration of the track in long
   * @return readable time format
   */
  private String longTimeConversion(long longTime) {
    long days = longTime / 86400000 % 30;
    long hours = longTime / 3600000 % 24;
    long minutes = longTime / 60000 % 60;
    long seconds = longTime / 1000 % 60;
    return (days == 0 ? "" : days < 10 ? "0" + days + ":" : days + ":") +
        (hours == 0 ? "" : hours < 10 ? "0" + hours + ":" : hours + ":") +
        (minutes == 0 ? "00:" : minutes < 10 ? "0" + minutes + ":" : minutes + ":") +
        (seconds == 0 ? "00" : seconds < 10 ? "0" + seconds : seconds + "");
  }

  private long getInvokerUserId() {
    return this.invokerUserId;
  }

  private void setInvokerUserId(long invokerUserId) {
    this.invokerUserId = invokerUserId;
  }
}