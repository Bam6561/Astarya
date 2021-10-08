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
    this.aliases = new String[]{"searchtrack", "search", "find"};
    this.arguments = "[1++]YouTubeQuery";
    this.help = "Searches for an audio track to add to the queue.";
    this.waiter = waiter;
    this.userID = 0;
  }

  @Override
  protected void execute(CommandEvent ce) {
    Settings.deleteInvoke(ce);
    GuildVoiceState userVoiceState = ce.getMember().getVoiceState();
    GuildVoiceState botVoiceState = ce.getGuild().getSelfMember().getVoiceState();
    if (userVoiceState.inVoiceChannel()) { // User in any voice channel
      if (botVoiceState.inVoiceChannel()) { // Bot already in voice channel
        if (userVoiceState.getChannel()
            .equals(botVoiceState.getChannel())) { // User in same voice channel as bot
          searchForAudioTrack(ce);
          this.userID = Long.parseLong(ce.getAuthor().getId()); // Lock requester
          userResponse(ce); // User response
        } else { // User not in same voice channel as bot
          ce.getChannel().sendMessage("User not in same voice channel.").queue();
        }
      } else { // Bot not in any voice channel
        ce.getChannel().sendMessage("Not in a voice channel.").queue();
      }
    } else { // User not in any voice channel
      ce.getChannel().sendMessage("User not in a voice channel.").queue();
    }
  }

  private void searchForAudioTrack(CommandEvent ce) {
    String[] args = ce.getMessage().getContentRaw().split("\\s"); // Parse message for arguments
    int arguments = args.length;
    if (arguments > 1) { // Search for audio track
      StringBuilder searchQuery = new StringBuilder();
      for (int i = 1; i < arguments; i++) {
        searchQuery.append(args[i]);
      }
      String youtubeSearchQuery = "ytsearch:" + String.join(" ", searchQuery);
      PlayerManager.getINSTANCE().searchAudioTrack(ce, youtubeSearchQuery);
    } else { // Invalid arguments
      ce.getChannel().sendMessage("Invalid number of arguments.").queue();
    }
  }

  private void userResponse(CommandEvent ce) { // Wait for user response
    waiter.waitForEvent(GuildMessageReceivedEvent.class,
        // Message sent matches invoker user's ID
        w -> Long.parseLong(w.getMessage().getAuthor().getId()) == userID,
        w -> {
          String[] args = w.getMessage().getContentRaw().split("\\s"); // Parse message for arguments
          int arguments = args.length;
          if (arguments == 1) {
            queueUserResponse(ce, args);
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

  private void queueUserResponse(CommandEvent ce, String[] args) {
    try { // Add user response to queue
      this.userID = 0;
      PlaybackManager playbackManager = PlayerManager.getINSTANCE().getPlaybackManager(ce.getGuild());
      AudioTrack audioTrack = PlayerManager.getINSTANCE().searchTrackResults.get(Integer.parseInt(args[0]) - 1);
      playbackManager.audioScheduler.queue(audioTrack); // Add audio track
      String requester = "[" + ce.getAuthor().getAsTag() + "]";
      playbackManager.audioScheduler.addToRequesterList(requester); // Add requester
      long trackDurationLong = audioTrack.getDuration();
      String trackDuration = floatTimeConversion(trackDurationLong); // Track duration
      StringBuilder userResponseConfirmation = new StringBuilder();
      userResponseConfirmation.append("**Added:** `").append(audioTrack.getInfo().title).
          append("` {*").append(trackDuration).append("*} ").append(requester);
      ce.getChannel().sendMessage(userResponseConfirmation).queue();
    } catch (IndexOutOfBoundsException error) { // Out of bounds
      this.userID = 0;
      ce.getChannel().sendMessage("Argument must be in range of 1-5.").queue();
    } catch (NumberFormatException error) { // Not an integer
      this.userID = 0;
      ce.getChannel().sendMessage("Argument must be an integer in range of 1-5.").queue();
    }
  }

  private String floatTimeConversion(long floatTime) {
    long days = floatTime / 86400000 % 30;
    long hours = floatTime / 3600000 % 24;
    long minutes = floatTime / 60000 % 60;
    long seconds = floatTime / 1000 % 60;
    return (days == 0 ? "" : days < 10 ? "0" + days + ":" : days + ":") +
        (hours == 0 ? "" : hours < 10 ? "0" + hours + ":" : hours + ":") +
        (minutes == 0 ? "00:" : minutes < 10 ? "0" + minutes + ":" : minutes + ":") +
        (seconds == 0 ? "00" : seconds < 10 ? "0" + seconds : seconds + "");
  }
}