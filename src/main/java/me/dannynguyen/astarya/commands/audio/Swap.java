package me.dannynguyen.astarya.commands.audio;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import me.dannynguyen.astarya.commands.audio.managers.PlayerManager;
import me.dannynguyen.astarya.commands.owner.Settings;
import me.dannynguyen.astarya.enums.BotMessage;
import net.dv8tion.jda.api.entities.GuildVoiceState;

import java.util.Collections;
import java.util.List;

/**
 * Swap is a command invocation that swaps the position of a track in queue with another.
 *
 * @author Danny Nguyen
 * @version 1.8.0
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
        ce.getChannel().sendMessage(BotMessage.USER_NOT_IN_SAME_VC.getMessage()).queue();
      }
    } catch (NullPointerException e) {
      ce.getChannel().sendMessage(BotMessage.USER_NOT_IN_VC.getMessage()).queue();
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
      ce.getChannel().sendMessage(BotMessage.INVALID_NUMBER_OF_PARAMETERS.getMessage()).queue();
    }
  }

  /**
   * Processes user provided parameters to swap tracks in the queue.
   *
   * @param ce         command event
   * @param parameters user provided parameters
   * @throws NumberFormatException user provided non-integer values
   */
  private void processSwapRequest(CommandEvent ce, String[] parameters) {
    try {
      // Displayed indices to users are different from data index, so subtract 1
      int originalIndex = Integer.parseInt(parameters[1]) - 1;
      int swapIndex = Integer.parseInt(parameters[2]) - 1;

      swapTracks(ce, originalIndex, swapIndex);
    } catch (NumberFormatException e) {
      ce.getChannel().sendMessage(Failure.SWAP_SPECIFY.text).queue();
    }
  }

  /**
   * Swaps two tracks' order in the queue.
   *
   * @param ce            command event
   * @param originalIndex original track index
   * @param swapIndex     track index to be swapped
   * @throws IndexOutOfBoundsException user provided indices out of queue range
   */
  private void swapTracks(CommandEvent ce, int originalIndex, int swapIndex) {
    try {
      List<TrackQueueIndex> trackQueue =
          PlayerManager.getINSTANCE().getPlaybackManager(ce.getGuild()).audioScheduler.getTrackQueue();
      AudioTrack originalTrack = trackQueue.get(originalIndex).getAudioTrack();
      AudioTrack swapTrack = trackQueue.get(swapIndex).getAudioTrack();

      Collections.swap(trackQueue, originalIndex, swapIndex);
      sendSwapConfirmation(ce, originalIndex, swapIndex, trackQueue, originalTrack, swapTrack);
    } catch (IndexOutOfBoundsException e) {
      ce.getChannel().sendMessage(BotMessage.INVALID_QUEUE_NUMBER.getMessage()).queue();
    }
  }

  /**
   * Sends confirmation the two tracks were swapped.
   *
   * @param ce            command event
   * @param originalIndex original rack index
   * @param swapIndex     track index to be swapped
   * @param trackQueue    list containing the tracks
   * @param originalTrack track at the original index
   * @param swapTrack     track at the index to be swapped
   */
  private void sendSwapConfirmation(CommandEvent ce, int originalIndex, int swapIndex,
                                    List<TrackQueueIndex> trackQueue,
                                    AudioTrack originalTrack, AudioTrack swapTrack) {
    String originalTrackDuration = TrackTime.convertLong(originalTrack.getDuration());
    String swapTrackDuration = TrackTime.convertLong(swapTrack.getDuration());

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

  private enum Failure {
    SWAP_SPECIFY("Provide numbers to swap tracks in track queue.");

    public final String text;

    Failure(String text) {
      this.text = text;
    }
  }
}