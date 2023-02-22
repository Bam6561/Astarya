package commands.audio;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import commands.audio.managers.AudioScheduler;
import commands.audio.managers.PlayerManager;
import commands.owner.Settings;
import net.dv8tion.jda.api.entities.GuildVoiceState;

import java.util.ArrayList;

public class PlayNext extends Command {
  public PlayNext() {
    this.name = "playnext";
    this.aliases = new String[]{"playnext", "pn"};
    this.arguments = "[1]QueueNumber";
    this.help = "Sets the position of the currently playing audio track.";
  }

  // Puts a track immediately after currently playing track
  @Override
  protected void execute(CommandEvent ce) {
    Settings.deleteInvoke(ce);

    GuildVoiceState userVoiceState = ce.getMember().getVoiceState();
    GuildVoiceState botVoiceState = ce.getGuild().getSelfMember().getVoiceState();

    boolean userInVoiceChannel = ce.getMember().getVoiceState().inVoiceChannel();
    boolean userInSameVoiceChannel = userVoiceState.getChannel().equals(botVoiceState.getChannel());

    if (userInVoiceChannel) {
      if (userInSameVoiceChannel) {
        parsePlayNextRequest(ce);
      } else {
        ce.getChannel().sendMessage("User not in the same voice channel.").queue();
      }
    } else {
      ce.getChannel().sendMessage("User not in a voice channel.").queue();
    }
  }

  // Validate playNext request before proceeding
  private void parsePlayNextRequest(CommandEvent ce) {
    // Parse message for arguments
    String[] arguments = ce.getMessage().getContentRaw().split("\\s");
    int numberOfArguments = arguments.length;

    boolean validNumberOfArguments = numberOfArguments == 2;
    if (validNumberOfArguments) {
      try {
        playNext(ce, Integer.parseInt(arguments[1]));
      } catch (NumberFormatException error) {
        ce.getChannel().sendMessage("Specify an integer to play the next track number.").queue();
      }
    } else {
      ce.getChannel().sendMessage("Invalid number of arguments.").queue();
    }
  }

  // Puts a track immediately after currently playing track
  private void playNext(CommandEvent ce, int queueNumber) {
    try {
      AudioScheduler audioScheduler = PlayerManager.getINSTANCE().getPlaybackManager(ce.getGuild()).audioScheduler;

      // Storage objects to access
      ArrayList<AudioTrack> trackQueue = audioScheduler.getTrackQueue();
      ArrayList<String> requesterList = audioScheduler.getRequesterList();

      // Displayed index to users are different from data index so subtract 1
      queueNumber = queueNumber - 1;

      AudioTrack audioTrack = trackQueue.get(queueNumber);
      String trackDuration = longTimeConversion(audioTrack.getDuration());

      trackQueue.remove(queueNumber);
      trackQueue.add(0, audioTrack);

      // Send playNext confirmation
      StringBuilder playNextConfirmation = new StringBuilder();
      playNextConfirmation.append("**Play Next:** **[").append(queueNumber + 1).
          append("]** `").append(audioTrack.getInfo().title).
          append("` {*").append(trackDuration).append("*} ").
          append(requesterList.get(queueNumber)).append(" [").
          append(ce.getAuthor().getAsTag()).append("]");
      ce.getChannel().sendMessage(playNextConfirmation).queue();
    } catch (IndexOutOfBoundsException error) { // Track number out of bounds
      ce.getChannel().sendMessage("Queue number does not exist.").queue();
    }
  }

  // Converts long duration to conventional readable time
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