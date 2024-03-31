package me.dannynguyen.astarya.commands.audio;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import me.dannynguyen.astarya.commands.audio.managers.AudioScheduler;
import me.dannynguyen.astarya.commands.audio.managers.PlayerManager;
import me.dannynguyen.astarya.commands.owner.Settings;
import me.dannynguyen.astarya.enums.BotMessage;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;

import java.util.Collections;
import java.util.List;

/**
 * Command invocation that swaps the position of a track in queue with another.
 *
 * @author Danny Nguyen
 * @version 1.8.12
 * @since 1.2.14
 */
public class Swap extends Command {
  /**
   * Associates the command with its properties.
   */
  public Swap() {
    this.name = "switch";
    this.aliases = new String[]{"swap", "switch", "sw"};
    this.arguments = "[1]QueueNumber [2]QueueNumber";
    this.help = "Swaps the position of a track in queue with another.";
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
      readSwapRequest(ce);
    } else {
      ce.getChannel().sendMessage(BotMessage.USER_NOT_IN_SAME_VC.getMessage()).queue();
    }
  }

  /**
   * Checks if the command request was formatted correctly before swapping tracks.
   *
   * @param ce command event
   */
  private void readSwapRequest(CommandEvent ce) {
    String[] parameters = ce.getMessage().getContentRaw().split("\\s");
    int numberOfParameters = parameters.length - 1;

    if (numberOfParameters == 2) {
      processSwapRequest(ce, parameters);
    } else {
      ce.getChannel().sendMessage(BotMessage.INVALID_NUMBER_OF_PARAMETERS.getMessage()).queue();
    }
  }

  /**
   * Processes user provided parameters to swap tracks in the {@link AudioScheduler#getTrackQueue() queue}.
   *
   * @param ce         command event
   * @param parameters user provided parameters
   */
  private void processSwapRequest(CommandEvent ce, String[] parameters) {
    try {
      // Displayed indices to users are different from data index, so subtract 1
      int originalIndex = Integer.parseInt(parameters[1]) - 1;
      int swapIndex = Integer.parseInt(parameters[2]) - 1;

      try {
        List<TrackQueueIndex> trackQueue = PlayerManager.getINSTANCE().getPlaybackManager(ce.getGuild()).audioScheduler.getTrackQueue();
        AudioTrack originalTrack = trackQueue.get(originalIndex).getAudioTrack();
        AudioTrack swapTrack = trackQueue.get(swapIndex).getAudioTrack();

        Collections.swap(trackQueue, originalIndex, swapIndex);

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
      } catch (IndexOutOfBoundsException e) {
        ce.getChannel().sendMessage(BotMessage.INVALID_QUEUE_NUMBER.getMessage()).queue();
      }
    } catch (NumberFormatException e) {
      ce.getChannel().sendMessage("Provide numbers to swap tracks in track queue.").queue();
    }
  }
}