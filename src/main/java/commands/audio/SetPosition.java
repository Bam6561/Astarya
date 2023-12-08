package commands.audio;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import commands.audio.managers.AudioScheduler;
import commands.audio.managers.PlayerManager;
import commands.owner.Settings;
import net.dv8tion.jda.api.entities.GuildVoiceState;

/**
 * SetPosition is a command invocation that sets the position of the currently playing track.
 *
 * @author Danny Nguyen
 * @version 1.6.6
 * @since 1.2.11
 */
public class SetPosition extends Command {
  public SetPosition() {
    this.name = "setPosition";
    this.aliases = new String[]{"setposition", "setpos", "sp"};
    this.arguments = "[1]TimeString";
    this.help = "Sets the position of the currently playing track.";
  }

  /**
   * Determines whether the user is in the same voice channel as the bot to process a setPosition command request.
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
        parseSetPositionRequest(ce);
      } else {
        ce.getChannel().sendMessage("User not in the same voice channel.").queue();
      }
    } catch (NullPointerException e) {
      ce.getChannel().sendMessage("User not in a voice channel.").queue();
    }
  }

  /**
   * Processes user provided parameters to determine whether the setPosition command request was formatted correctly.
   *
   * @param ce object containing information about the command event
   * @throws NumberFormatException user provided non-integer value
   */
  private void parseSetPositionRequest(CommandEvent ce) {
    String[] parameters = ce.getMessage().getContentRaw().split("\\s");
    int numberOfParameters = parameters.length - 1;

    boolean validNumberOfParameters = numberOfParameters == 1;
    if (validNumberOfParameters) {
      try {
        setTrackPosition(ce, parameters[1]);
      } catch (NumberFormatException error) {
        ce.getChannel().sendMessage("Invalid time frame. " +
            "Specify the section to be skipped to using hh:mm:ss.").queue();
      }
    } else {
      ce.getChannel().sendMessage("Invalid number of parameters.").queue();
    }
  }

  /**
   * Sets the position of the currently playing track.
   *
   * @param ce                  object containing information about the command event
   * @param trackPositionString user provided position of the track to be set to
   */
  private void setTrackPosition(CommandEvent ce, String trackPositionString) {
    AudioScheduler audioScheduler = PlayerManager.getINSTANCE().getPlaybackManager(ce.getGuild()).audioScheduler;
    AudioPlayer audioPlayer = audioScheduler.getAudioPlayer();

    boolean currentlyPlayingTrack = !(audioPlayer.getPlayingTrack() == null);
    if (currentlyPlayingTrack) {
      long trackPositionToSet = convertTimeToLong(ce, trackPositionString);

      boolean requestedTrackPositionCanBeSet = audioPlayer.getPlayingTrack().getDuration() > trackPositionToSet;
      if (requestedTrackPositionCanBeSet) {
        audioPlayer.getPlayingTrack().setPosition(trackPositionToSet);

        // setPosition confirmation
        String positionSet = longTimeConversion(trackPositionToSet);
        StringBuilder setPositionConfirmation = new StringBuilder();
        setPositionConfirmation.append("**Set Position:** {*").append(positionSet).
            append("*} [").append(ce.getAuthor().getAsTag()).append("]");
        ce.getChannel().sendMessage(setPositionConfirmation).queue();
      } else {
        ce.getChannel().sendMessage("Requested position exceeds track length.").queue();
      }
    } else {
      ce.getChannel().sendMessage("Nothing is currently playing.").queue();
    }
  }

  /**
   * Converts user provided hh:mm:ss format to long data type.
   *
   * @param ce                  object containing information about the command event
   * @param trackPositionString user provided position of the track to be set to
   * @return position of the track to be set to in long data type
   */
  private long convertTimeToLong(CommandEvent ce, String trackPositionString) {
    String[] trackPositionTimeTypes = trackPositionString.split(":");
    long seconds = 0;
    long minutes = 0;
    long hours = 0;

    switch (trackPositionTimeTypes.length) {
      case 1 -> seconds = Integer.parseInt(trackPositionTimeTypes[0]);
      case 2 -> {
        minutes = Integer.parseInt(trackPositionTimeTypes[0]);
        seconds = Integer.parseInt(trackPositionTimeTypes[1]);
      }
      case 3 -> {
        hours = Integer.parseInt(trackPositionTimeTypes[0]);
        minutes = Integer.parseInt(trackPositionTimeTypes[1]);
        seconds = Integer.parseInt(trackPositionTimeTypes[2]);
      }
      default -> ce.getChannel().sendMessage("Invalid number of parameters.").queue();
    }

    // Conversion to milliseconds
    hours = hours * 3600000;
    minutes = minutes * 60000;
    seconds = seconds * 1000;
    return hours + minutes + seconds;
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