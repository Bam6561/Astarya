package commands.audio;

import astarya.BotMessage;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import commands.audio.managers.AudioScheduler;
import commands.audio.managers.PlayerManager;
import commands.audio.objects.TrackQueueIndex;
import commands.owner.Settings;
import net.dv8tion.jda.api.entities.GuildVoiceState;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Remove is a command invocation that removes track(s) from the track queue.
 *
 * @author Danny Nguyen
 * @version 1.7.12
 * @since 1.2.2
 */
public class Remove extends Command {
  public Remove() {
    this.name = "remove";
    this.aliases = new String[]{"remove", "rm", "r"};
    this.arguments = "[1]QueueNumber [1 ++]QueueNumbers";
    this.help = "Removes track(s) from the track queue.";
  }

  /**
   * Checks if the user is in the same voice channel as the bot to read a remove command request.
   *
   * @param ce command event
   * @throws NullPointerException user not in same voice channel
   */
  @Override
  protected void execute(CommandEvent ce) {
    Settings.deleteInvoke(ce);

    GuildVoiceState userVoiceState = ce.getMember().getVoiceState();
    GuildVoiceState botVoiceState = ce.getGuild().getSelfMember().getVoiceState();

    try {
      boolean userInSameVoiceChannel = userVoiceState.getChannel().equals(botVoiceState.getChannel());
      if (userInSameVoiceChannel) {
        interpretRemoveTrackRequest(ce);
      } else {
        ce.getChannel().sendMessage(BotMessage.Failure.USER_NOT_IN_SAME_VC.text).queue();
      }
    } catch (NullPointerException e) {
      ce.getChannel().sendMessage(BotMessage.Failure.USER_NOT_IN_VC.text).queue();
    }
  }

  /**
   * Either removes a singular track from the track queue or multiple.
   *
   * @param ce command event
   * @throws NumberFormatException user provided non-integer value
   */
  private void interpretRemoveTrackRequest(CommandEvent ce) {
    String[] parameters = ce.getMessage().getContentRaw().split("\\s");
    int numberOfParameters = parameters.length - 1;

    switch (numberOfParameters) {
      case 0 -> ce.getChannel().sendMessage(BotMessage.Failure.INVALID_NUMBER_OF_PARAMETERS.text).queue();
      case 1 -> {
        try {
          removeTrack(ce, Integer.parseInt(parameters[1]));
        } catch (NumberFormatException e) {
          ce.getChannel().sendMessage(BotMessage.Failure.REMOVE_SPECIFY.text).queue();
        }
      }
      default -> readRemoveMultipleTrackRequest(ce, parameters, numberOfParameters);
    }
  }

  /**
   * Removes a track from the track queue.
   *
   * @param ce         command event
   * @param queueIndex track to be removed from the track queue
   * @throws IndexOutOfBoundsException user provided number out of range of track queue
   */
  private void removeTrack(CommandEvent ce, int queueIndex) {
    try {
      AudioScheduler audioScheduler = PlayerManager.getINSTANCE().getPlaybackManager(ce.getGuild()).audioScheduler;
      ArrayList<TrackQueueIndex> trackQueue = audioScheduler.getTrackQueue();

      // Displayed index to users are different from data index, so subtract 1
      queueIndex = queueIndex - 1;

      // Confirmation is sent first before removal to show the correct track being removed
      sendRemoveConfirmation(ce, queueIndex, trackQueue);
      trackQueue.remove(queueIndex);
    } catch (IndexOutOfBoundsException e) {
      ce.getChannel().sendMessage(BotMessage.Failure.INVALID_QUEUE_NUMBER.text).queue();
    }
  }

  /**
   * Checks if user provided parameters are integers and
   * adds the values into an ArrayList to be mass removed.
   *
   * @param ce                 command event
   * @param parameters         user provided parameters
   * @param numberOfParameters number of user provided parameters
   * @throws NumberFormatException user provided non-integer value
   */
  private void readRemoveMultipleTrackRequest(CommandEvent ce, String[] parameters, int numberOfParameters) {
    try {
      // Validate and convert values to integers
      ArrayList<Integer> queueIndicesToBeRemoved = new ArrayList<>();
      for (int i = 1; i < numberOfParameters + 1; i++) {
        parameters[i] = parameters[i].replace(",", "");
        queueIndicesToBeRemoved.add(Integer.valueOf(parameters[i]));
      }
      removeMultipleTracks(ce, queueIndicesToBeRemoved);
    } catch (NumberFormatException e) {
      ce.getChannel().sendMessage(BotMessage.Failure.REMOVE_SPECIFY_GROUP.text).queue();
    }
  }

  /**
   * Removes multiple tracks from the track queue.
   *
   * @param ce           command event
   * @param queueIndices ArrayList containing queue indices to be removed
   * @throws IndexOutOfBoundsException user provided queue number out of range of track queue
   */
  private void removeMultipleTracks(CommandEvent ce, ArrayList<Integer> queueIndices) {
    try {
      // Removes the largest queue numbers first as to avoid disrupting the track queue order
      Collections.sort(queueIndices);
      for (int i = queueIndices.size() - 1; i >= 0; i--) {
        removeTrack(ce, queueIndices.get(i));
      }
    } catch (IndexOutOfBoundsException e) {
      ce.getChannel().sendMessage(BotMessage.Failure.INVALID_QUEUE_NUMBER.text).queue();
    }
  }

  /**
   * Sends confirmation the track was removed from the track queue.
   *
   * @param ce         command event
   * @param queueIndex index in the track queue to be removed
   * @param trackQueue arraylist containing the tracks
   */
  private void sendRemoveConfirmation(CommandEvent ce, int queueIndex, ArrayList<TrackQueueIndex> trackQueue) {
    StringBuilder removeTrackConfirmation = new StringBuilder();
    removeTrackConfirmation.append("**Removed:** **[").append(queueIndex + 1).append("]** `")
        .append(trackQueue.get(queueIndex).getAudioTrack().getInfo().title).append("`")
        .append(trackQueue.get(queueIndex).getRequester())
        .append(" *[").append(ce.getAuthor().getAsTag()).append("]*");
    ce.getChannel().sendMessage(removeTrackConfirmation).queue();
  }
}

