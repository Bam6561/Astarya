package commands.audio;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import commands.audio.managers.AudioScheduler;
import commands.audio.managers.PlayerManager;
import commands.audio.objects.TrackQueueIndex;
import commands.owner.Settings;
import net.dv8tion.jda.api.entities.GuildVoiceState;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Swap is a command invocation that swaps the position of a track in queue with another.
 *
 * @author Danny Nguyen
 * @version 1.7.0
 * @since 1.2.14
 */
public class Swap extends Command {
  public Swap() {
    this.name = "switch";
    this.aliases = new String[]{"swap", "switch", "sw"};
    this.arguments = "[1]QueueNumber [2]QueueNumber";
    this.help = "Swaps the position of a track in queue with another.";
  }

  /**
   * Determines whether the user is in the same voice channel as the bot to process a swap command request.
   *
   * @param ce object containing information about the command event
   */
  @Override
  protected void execute(CommandEvent ce) {
    Settings.deleteInvoke(ce);

    GuildVoiceState userVoiceState = ce.getMember().getVoiceState();
    GuildVoiceState botVoiceState = ce.getGuild().getSelfMember().getVoiceState();

    try {
      boolean userInSameVoiceChannel = userVoiceState.getChannel().equals(botVoiceState.getChannel());
      if (userInSameVoiceChannel) {
        parseSwapRequest(ce);
      } else {
        ce.getChannel().sendMessage("User not in the same voice channel.").queue();
      }
    } catch (NullPointerException e) {
      ce.getChannel().sendMessage("User not in a voice channel.").queue();
    }
  }

  /**
   * Processes user provided parameters to determine whether the swap command request was formatted correctly.
   *
   * @param ce object containing information about the command event
   * @throws NumberFormatException user provided non-integer values
   */
  private void parseSwapRequest(CommandEvent ce) {
    String[] parameters = ce.getMessage().getContentRaw().split("\\s");
    int numberOfParameters = parameters.length - 1;

    boolean validNumberOfParameters = numberOfParameters == 2;
    if (validNumberOfParameters) {
      try {
        // Displayed index to users are different from data index, so subtract 1
        int originalIndex = Integer.parseInt(parameters[1]) - 1;
        int swapIndex = Integer.parseInt(parameters[2]) - 1;

        swapTracks(ce, originalIndex, swapIndex);
      } catch (NumberFormatException e) {
        ce.getChannel().sendMessage("Specify integers to swap tracks in queue.").queue();
      }
    } else {
      ce.getChannel().sendMessage("Invalid number of parameters.").queue();
    }
  }

  /**
   * Swaps two tracks' order in the queue.
   *
   * @param ce            object containing information about the command event
   * @param originalIndex original track index
   * @param swapIndex     track index to be swapped
   * @throws IndexOutOfBoundsException user provided indices out of queue range
   */
  private void swapTracks(CommandEvent ce, int originalIndex, int swapIndex) {
    try {
      AudioScheduler audioScheduler = PlayerManager.getINSTANCE().getPlaybackManager(ce.getGuild()).audioScheduler;

      ArrayList<TrackQueueIndex> trackQueue = audioScheduler.getTrackQueue();

      // Audio track objects
      AudioTrack originalTrack = trackQueue.get(originalIndex).getAudioTrack();
      AudioTrack swapTrack = trackQueue.get(swapIndex).getAudioTrack();

      Collections.swap(trackQueue, originalIndex, swapIndex);

      // tracks' duration
      String originalTrackDuration = longTimeConversion(originalTrack.getDuration());
      String swapTrackDuration = longTimeConversion(swapTrack.getDuration());

      // Swap confirmation
      StringBuilder swapConfirmation = new StringBuilder();
      swapConfirmation.append("**Swap:** ").append(" [").
          append(ce.getAuthor().getAsTag()).append("]\n**[").append(originalIndex + 1).
          append("]** `").append(originalTrack.getInfo().title).
          append("` {*").append(originalTrackDuration).append("*} ").
          append(trackQueue.get(originalIndex).getRequester()).append("\n**[").
          append(swapIndex + 1).append("]** `").append(swapTrack.getInfo().title).
          append("` {*").append(swapTrackDuration).append("*} ").
          append(trackQueue.get(swapIndex).getRequester());
      ce.getChannel().sendMessage(swapConfirmation).queue();
    } catch (IndexOutOfBoundsException e) {
      ce.getChannel().sendMessage("Queue number does not exist.").queue();
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
}