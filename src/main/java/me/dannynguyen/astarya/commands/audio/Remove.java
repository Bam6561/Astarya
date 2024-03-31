package me.dannynguyen.astarya.commands.audio;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import me.dannynguyen.astarya.commands.audio.managers.AudioScheduler;
import me.dannynguyen.astarya.commands.audio.managers.PlayerManager;
import me.dannynguyen.astarya.commands.owner.Settings;
import me.dannynguyen.astarya.enums.BotMessage;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Command invocation that removes track(s) from the {@link AudioScheduler#getTrackQueue() queue}.
 *
 * @author Danny Nguyen
 * @version 1.8.12
 * @since 1.2.2
 */
public class Remove extends Command {
  /**
   * Associates the command with its properties.
   */
  public Remove() {
    this.name = "remove";
    this.aliases = new String[]{"remove", "rm", "r"};
    this.arguments = "[1]QueueNumber [1 ++]QueueNumbers";
    this.help = "Removes track(s) from the track queue.";
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
      new RemoveRequest(ce).interpretRequest();
    } else {
      ce.getChannel().sendMessage(BotMessage.USER_NOT_IN_SAME_VC.getMessage()).queue();
    }
  }

  /**
   * Represents a track remove query.
   *
   * @author Danny Nguyen
   * @version 1.8.12
   * @since 1.8.12
   */
  private static class RemoveRequest {
    /**
     * Command event.
     */
    private final CommandEvent ce;

    /**
     * User provided parameters;
     */
    private final String[] parameters;

    /**
     * Number of parameters.
     */
    private final int numberOfParameters;

    /**
     * Associates a remove request with its command event and parameters.
     *
     * @param ce command event
     */
    RemoveRequest(CommandEvent ce) {
      this.ce = ce;
      this.parameters = ce.getMessage().getContentRaw().split("\\s");
      this.numberOfParameters = parameters.length - 1;
    }

    /**
     * Either removes a singular track from the {@link AudioScheduler#getTrackQueue() queue} or multiple.
     */
    private void interpretRequest() {
      switch (numberOfParameters) {
        case 0 -> ce.getChannel().sendMessage(BotMessage.INVALID_NUMBER_OF_PARAMETERS.getMessage()).queue();
        case 1 -> {
          try {
            removeTrack(Integer.parseInt(parameters[1]));
          } catch (NumberFormatException e) {
            ce.getChannel().sendMessage("Provide queue number to be removed.").queue();
          }
        }
        default -> removeMultipleTracks();
      }
    }

    /**
     * Removes a track from the {@link AudioScheduler#getTrackQueue() queue}.
     *
     * @param queueIndex track to be removed from the {@link AudioScheduler#getTrackQueue() queue}
     */
    private void removeTrack(int queueIndex) {
      try {
        AudioScheduler audioScheduler = PlayerManager.getINSTANCE().getPlaybackManager(ce.getGuild()).audioScheduler;
        List<TrackQueueIndex> trackQueue = audioScheduler.getTrackQueue();

        // Displayed indices to users are different from data index, so subtract 1
        queueIndex = queueIndex - 1;

        // Confirmation is sent first before removal to show the correct track being removed
        sendRemoveConfirmation(ce, queueIndex, trackQueue);
        trackQueue.remove(queueIndex);
      } catch (IndexOutOfBoundsException e) {
        ce.getChannel().sendMessage(BotMessage.INVALID_QUEUE_NUMBER.getMessage()).queue();
      }
    }

    /**
     * Checks if user provided parameters are integers and adds the values into a list to be mass removed.
     */
    private void removeMultipleTracks() {
      try {
        // Validate and convert values to integers
        List<Integer> queueIndices = new ArrayList<>();
        for (int i = 1; i < numberOfParameters + 1; i++) {
          parameters[i] = parameters[i].replace(",", "");
          queueIndices.add(Integer.valueOf(parameters[i]));
        }

        try {
          // Removes the largest queue numbers first as to avoid disrupting the queue order
          Collections.sort(queueIndices);
          for (int i = queueIndices.size() - 1; i >= 0; i--) {
            removeTrack(queueIndices.get(i));
          }
        } catch (IndexOutOfBoundsException e) {
          ce.getChannel().sendMessage(BotMessage.INVALID_QUEUE_NUMBER.getMessage()).queue();
        }
      } catch (NumberFormatException e) {
        ce.getChannel().sendMessage("Provide queue numbers to be removed with a space between each.").queue();
      }
    }

    /**
     * Sends confirmation the track was removed from the {@link AudioScheduler#getTrackQueue() queue}.
     *
     * @param ce         command event
     * @param queueIndex index in the {@link AudioScheduler#getTrackQueue() queue} to be removed
     * @param trackQueue {@link AudioScheduler#getTrackQueue() queue}
     */
    private void sendRemoveConfirmation(CommandEvent ce, int queueIndex, List<TrackQueueIndex> trackQueue) {
      StringBuilder removeTrackConfirmation = new StringBuilder();
      removeTrackConfirmation.append("**Removed:** **[").append(queueIndex + 1).append("]** `")
          .append(trackQueue.get(queueIndex).getAudioTrack().getInfo().title).append("`")
          .append(trackQueue.get(queueIndex).getRequester())
          .append(" *[").append(ce.getAuthor().getAsTag()).append("]*");
      ce.getChannel().sendMessage(removeTrackConfirmation).queue();
    }
  }
}

