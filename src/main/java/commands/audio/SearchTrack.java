package commands.audio;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import commands.audio.managers.PlaybackManager;
import commands.audio.managers.PlayerManager;
import commands.owner.Settings;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.util.concurrent.TimeUnit;

public class SearchTrack extends Command {
  private EventWaiter waiter;
  private long userID;

  public SearchTrack(EventWaiter waiter) {
    this.name = "searchtrack";
    this.aliases = new String[]{"searchtrack", "search", "find", "st"};
    this.arguments = "[1++]YouTubeQuery";
    this.help = "Searches for an audio track to add to the queue.";
    this.waiter = waiter;
    this.userID = 0;
  }

  // Searches for a track to be added to the track queue, changes bot's presence and activity
  @Override
  protected void execute(CommandEvent ce) {
    Settings.deleteInvoke(ce);

    GuildVoiceState userVoiceState = ce.getMember().getVoiceState();
    GuildVoiceState botVoiceState = ce.getGuild().getSelfMember().getVoiceState();

    boolean userInVoiceChannel = ce.getMember().getVoiceState().inVoiceChannel();
    boolean userInSameVoiceChannel = userVoiceState.getChannel().equals(botVoiceState.getChannel());

    if (userInVoiceChannel) {
      if (userInSameVoiceChannel) {
        processSearchTrackRequest(ce);
        this.userID = Long.parseLong(ce.getAuthor().getId()); // Lock requester
        userResponse(ce); // User response
      } else {
        ce.getChannel().sendMessage("User not in the same voice channel.").queue();
      }
    } else {
      ce.getChannel().sendMessage("User not in a voice channel.").queue();
    }
  }

  // Processes searchTrack request before proceeding
  private void processSearchTrackRequest(CommandEvent ce) {
    // Parse message for arguments
    String[] arguments = ce.getMessage().getContentRaw().split("\\s");
    int numberOfArguments = arguments.length;

    if (numberOfArguments > 1) { // Search for audio track
      StringBuilder searchQuery = new StringBuilder();
      for (int i = 1; i < numberOfArguments; i++) {
        searchQuery.append(arguments[i]);
      }
      String youtubeSearchQuery = "ytsearch:" + String.join(" ", searchQuery);
      PlayerManager.getINSTANCE().searchAudioTrack(ce, youtubeSearchQuery);
    } else { // Invalid arguments
      ce.getChannel().sendMessage("Invalid number of arguments.").queue();
    }
  }

  // Wait for user response to searchTrackRequest
  private void userResponse(CommandEvent ce) {
    waiter.waitForEvent(GuildMessageReceivedEvent.class,
        // Message sent matches invoker user's ID
        w -> Long.parseLong(w.getMessage().getAuthor().getId()) == userID,
        w -> {
          // Parse message for arguments
          String[] arguments = w.getMessage().getContentRaw().split("\\s");
          int numberOfArguments = arguments.length;
          if (numberOfArguments == 1) {
            queueUserResponse(ce, arguments);
          } else { // Invalid arguments
            this.userID = 0;
            ce.getChannel().sendMessage("Invalid number of arguments.").queue();
          }
        }, 15, TimeUnit.SECONDS, () -> { // No response
          this.userID = 0;
          PlayerManager.getINSTANCE().searchTrackResults.clear();
          ce.getChannel().sendMessage("No response. Search timed out.").queue();
        });
  }

  // Handle user response to searchTrackRequest
  private void queueUserResponse(CommandEvent ce, String[] args) {
    // Add user response to queue
    try {
      this.userID = 0;

      PlaybackManager playbackManager = PlayerManager.getINSTANCE().getPlaybackManager(ce.getGuild());
      AudioTrack audioTrack = PlayerManager.getINSTANCE().searchTrackResults.get(Integer.parseInt(args[0]) - 1);

      // Add audio track
      playbackManager.audioScheduler.queue(audioTrack);

      String requester = "[" + ce.getAuthor().getAsTag() + "]";
      playbackManager.audioScheduler.addToRequesterList(requester); // Add requester

      // Sends a search track confirmation
      String trackDuration = longTimeConversion(audioTrack.getDuration());
      StringBuilder userResponseConfirmation = new StringBuilder();
      userResponseConfirmation.append("**Added:** `").append(audioTrack.getInfo().title).
          append("` {*").append(trackDuration).append("*} ").append(requester);
      ce.getChannel().sendMessage(userResponseConfirmation).queue();
    } catch (IndexOutOfBoundsException | NumberFormatException e) {
      this.userID = 0;
      ce.getChannel().sendMessage("Responses must be an integer in range of 1-5.").queue();
    }
  }

  // Converts float duration to conventional readable time
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
}