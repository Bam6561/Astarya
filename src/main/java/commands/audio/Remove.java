package commands.audio;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import commands.audio.managers.AudioScheduler;
import commands.audio.managers.PlayerManager;
import commands.owner.Settings;
import net.dv8tion.jda.api.entities.GuildVoiceState;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Remove is a command invocation that removes track(s) from the queue.
 *
 * @author Danny Nguyen
 * @version 1.6.6
 * @since 1.2.2
 */
public class Remove extends Command {
  public Remove() {
    this.name = "remove";
    this.aliases = new String[]{"remove", "rm", "r"};
    this.arguments = "[1]QueueNumber [1, ++]QueueNumbers";
    this.help = "Removes track(s) from the queue.";
  }

  /**
   * Determines whether the user is in the same voice channel as the bot to process a remove command request.
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
        parseRemoveTrackRequest(ce);
      } else {
        ce.getChannel().sendMessage("User not in the same voice channel.").queue();
      }
    } catch (NullPointerException e) {
      ce.getChannel().sendMessage("User not in a voice channel.").queue();
    }
  }

  /**
   * Either removes a singular track from the queue or multiple.
   *
   * @param ce object containing information about the command event
   * @throws NumberFormatException user provided non-integer value
   */
  private void parseRemoveTrackRequest(CommandEvent ce) {
    // Parse message for parameters
    String[] parameters = ce.getMessage().getContentRaw().split("\\s");
    int numberOfParameters = parameters.length - 1;

    switch (numberOfParameters) {
      case 0 -> ce.getChannel().sendMessage("Invalid number of parameters.").queue();
      case 1 -> {
        try {
          removeTrack(ce, Integer.parseInt(parameters[1]));
        } catch (NumberFormatException e) {
          ce.getChannel().sendMessage("Specify what queue number to be removed with an integer.").queue();
        }
      }
      default -> {
        try {
          ArrayList<Integer> queueIndicesToBeRemoved = parseMultipleTrackRemoveRequest(parameters, numberOfParameters);
          removeMultipleTracks(ce, queueIndicesToBeRemoved);
        } catch (NumberFormatException e) {
          ce.getChannel().sendMessage("Specify what queue numbers to be removed with an integer " +
              "and add a space between each.").queue();
        }
      }
    }
  }

  /**
   * Removes a track from the queue.
   *
   * @param ce         object containing information about the command event
   * @param queueIndex track to be removed from the queue
   * @throws IndexOutOfBoundsException user provided queue number out of range of track queue
   */
  private void removeTrack(CommandEvent ce, int queueIndex) {
    try {
      AudioScheduler audioScheduler = PlayerManager.getINSTANCE().getPlaybackManager(ce.getGuild()).audioScheduler;

      // Storage objects to access
      ArrayList<AudioTrack> trackQueue = audioScheduler.getTrackQueue();
      ArrayList<String> requesterList = audioScheduler.getRequesterList();

      // Displayed index to users are different from data index, so subtract 1
      queueIndex = queueIndex - 1;

      // Remove confirmation
      StringBuilder removeTrackConfirmation = new StringBuilder();
      removeTrackConfirmation.append("**Removed:** **[").append(queueIndex + 1).append("]** `")
          .append(trackQueue.get(queueIndex).getInfo().title).append("`")
          .append(requesterList.get(queueIndex))
          .append(" *[").append(ce.getAuthor().getAsTag()).append("]*");
      ce.getChannel().sendMessage(removeTrackConfirmation).queue();

      trackQueue.remove(queueIndex);
      requesterList.remove(queueIndex);
    } catch (IndexOutOfBoundsException error) {
      ce.getChannel().sendMessage("Queue number does not exist.").queue();
    }
  }

  /**
   * Checks whether user provided parameters are integers and
   * adds the values into an ArrayList to be mass removed.
   *
   * @param parameters         user provided parameters
   * @param numberOfParameters number of user provided parameters
   * @return ArrayList of queue indices to be removed
   */
  private ArrayList<Integer> parseMultipleTrackRemoveRequest(String[] parameters, int numberOfParameters) {
    ArrayList<Integer> queueIndicesToBeRemoved = new ArrayList<>();
    // Validate and convert values to integers
    for (int i = 1; i < numberOfParameters + 1; i++) {
      parameters[i] = parameters[i].replace(",", "");
      queueIndicesToBeRemoved.add(Integer.valueOf(parameters[i]));
    }
    return queueIndicesToBeRemoved;
  }

  /**
   * Removes multiple tracks from the queue.
   *
   * @param ce           object containing information about the command event
   * @param queueIndices ArrayList containing queue indices to be removed
   * @throws IndexOutOfBoundsException user provided queue number out of range of track queue
   */
  private void removeMultipleTracks(CommandEvent ce, ArrayList<Integer> queueIndices) {
    try {
      Collections.sort(queueIndices);
      // Removes largest queue numbers first as to avoid disrupting the queue order
      for (int i = queueIndices.size() - 1; i >= 0; i--) {
        removeTrack(ce, queueIndices.get(i));
      }
    } catch (IndexOutOfBoundsException e) {
      ce.getChannel().sendMessage("Queue number does not exist.").queue();
    }
  }
}

