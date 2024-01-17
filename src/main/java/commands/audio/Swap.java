package commands.audio;

import astarya.Text;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import commands.audio.managers.PlayerManager;
import commands.audio.objects.TrackQueueIndex;
import commands.audio.utility.TimeConversion;
import commands.owner.Settings;
import net.dv8tion.jda.api.entities.GuildVoiceState;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Swap is a command invocation that swaps the position of a track in queue with another.
 *
 * @author Danny Nguyen
 * @version 1.7.8
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
   * Checks if the user is in the same voice channel as the bot to read a swap command request.
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
        readSwapRequest(ce);
      } else {
        ce.getChannel().sendMessage(Text.NOT_IN_SAME_VC.value()).queue();
      }
    } catch (NullPointerException e) {
      ce.getChannel().sendMessage(Text.NOT_IN_VC.value()).queue();
    }
  }

  /**
   * Checks if the swap command request was formatted correctly before swapping tracks.
   *
   * @param ce command event
   */
  private void readSwapRequest(CommandEvent ce) {
    String[] parameters = ce.getMessage().getContentRaw().split("\\s");
    int numberOfParameters = parameters.length - 1;

    boolean validNumberOfParameters = numberOfParameters == 2;
    if (validNumberOfParameters) {
      processSwapRequest(ce, parameters);
    } else {
      ce.getChannel().sendMessage("Invalid number of parameters.").queue();
    }
  }

  /**
   * Processes user provided parameters to swap tracks in the track queue.
   *
   * @param ce         command event
   * @param parameters user provided parameters
   * @throws NumberFormatException user provided non-integer values
   */
  private void processSwapRequest(CommandEvent ce, String[] parameters) {
    try {
      // Displayed index to users are different from data index, so subtract 1
      int originalIndex = Integer.parseInt(parameters[1]) - 1;
      int swapIndex = Integer.parseInt(parameters[2]) - 1;

      swapTracks(ce, originalIndex, swapIndex);
    } catch (NumberFormatException e) {
      ce.getChannel().sendMessage("Specify integers to swap tracks in the track queue.").queue();
    }
  }

  /**
   * Swaps two tracks' order in the track queue.
   *
   * @param ce            command event
   * @param originalIndex original track index
   * @param swapIndex     track index to be swapped
   * @throws IndexOutOfBoundsException user provided indices out of queue range
   */
  private void swapTracks(CommandEvent ce, int originalIndex, int swapIndex) {
    try {
      ArrayList<TrackQueueIndex> trackQueue = PlayerManager.getINSTANCE().getPlaybackManager(ce.getGuild()).audioScheduler.getTrackQueue();
      AudioTrack originalTrack = trackQueue.get(originalIndex).getAudioTrack();
      AudioTrack swapTrack = trackQueue.get(swapIndex).getAudioTrack();

      Collections.swap(trackQueue, originalIndex, swapIndex);
      sendSwapConfirmation(ce, originalIndex, swapIndex, trackQueue, originalTrack, swapTrack);
    } catch (IndexOutOfBoundsException e) {
      ce.getChannel().sendMessage(Text.INVALID_QUEUE_NUMBER.value()).queue();
    }
  }

  /**
   * Sends confirmation the two tracks were swapped.
   *
   * @param ce            command event
   * @param originalIndex original rack index
   * @param swapIndex     track index to be swapped
   * @param trackQueue    arraylist containing the tracks
   * @param originalTrack track at the original index
   * @param swapTrack     track at the index to be swapped
   */
  private void sendSwapConfirmation(CommandEvent ce, int originalIndex, int swapIndex,
                                    ArrayList<TrackQueueIndex> trackQueue,
                                    AudioTrack originalTrack, AudioTrack swapTrack) {
    String originalTrackDuration = TimeConversion.convert(originalTrack.getDuration());
    String swapTrackDuration = TimeConversion.convert(swapTrack.getDuration());

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
  }
}