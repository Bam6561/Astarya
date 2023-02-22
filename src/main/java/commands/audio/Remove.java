package commands.audio;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import commands.audio.managers.AudioScheduler;
import commands.audio.managers.PlayerManager;
import commands.owner.Settings;
import net.dv8tion.jda.api.entities.GuildVoiceState;

import java.util.ArrayList;

public class Remove extends Command {
  public Remove() {
    this.name = "remove";
    this.aliases = new String[]{"remove", "rm", "r"};
    this.arguments = "[1]queueNumber";
    this.help = "Removes an audio track from the queue.";
  }

  // Removes a track from the queue
  @Override
  protected void execute(CommandEvent ce) {
    Settings.deleteInvoke(ce);

    GuildVoiceState userVoiceState = ce.getMember().getVoiceState();
    GuildVoiceState botVoiceState = ce.getGuild().getSelfMember().getVoiceState();

    boolean userInVoiceChannel = ce.getMember().getVoiceState().inVoiceChannel();
    boolean userInSameVoiceChannel = userVoiceState.getChannel().equals(botVoiceState.getChannel());

    if (userInVoiceChannel) {
      if (userInSameVoiceChannel) {
        parseRemoveTrackRequest(ce);
      } else {
        ce.getChannel().sendMessage("User not in the same voice channel.").queue();
      }
    } else {
      ce.getChannel().sendMessage("User not in a voice channel.").queue();
    }
  }

  // Validates remove track request before proceeding
  private void parseRemoveTrackRequest(CommandEvent ce) {
    // Parse message for arguments
    String[] arguments = ce.getMessage().getContentRaw().split("\\s");
    int numberOfArguments = arguments.length - 1;

    boolean validNumberOfArguments = numberOfArguments == 1;
    if (validNumberOfArguments) {
      try {
        removeTrack(ce, Integer.parseInt(arguments[1]));
      } catch (NumberFormatException e) {
        ce.getChannel().sendMessage("Specify what queue number to be removed with an integer.").queue();
      }
    } else {
      ce.getChannel().sendMessage("Invalid number of arguments.").queue();
    }
  }

  // Removes track from the queue
  public void removeTrack(CommandEvent ce, int queueIndex) {
    try {
      AudioScheduler audioScheduler = PlayerManager.getINSTANCE().getPlaybackManager(ce.getGuild()).audioScheduler;

      // Storage objects to access
      ArrayList<AudioTrack> trackQueue = audioScheduler.getTrackQueue();
      ArrayList<String> requesterList = audioScheduler.getRequesterList();

      // Displayed index to users are different from data index, so subtract 1
      queueIndex = queueIndex - 1;

      // Removed track confirmation
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
}
