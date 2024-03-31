package me.dannynguyen.astarya.commands.audio;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import me.dannynguyen.astarya.commands.audio.managers.PlayerManager;
import me.dannynguyen.astarya.commands.owner.Settings;
import me.dannynguyen.astarya.enums.BotMessage;
import net.dv8tion.jda.api.entities.GuildVoiceState;

/**
 * Command invocation that sets the position of the currently playing track.
 *
 * @author Danny Nguyen
 * @version 1.8.1
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
   * Checks if the user is in the same voice channel as the bot to read the command request.
   *
   * @param ce command event
   */
  @Override
  protected void execute(CommandEvent ce) {
    Settings.deleteInvoke(ce);

    GuildVoiceState userVoiceState = ce.getMember().getVoiceState();
    GuildVoiceState botVoiceState = ce.getGuild().getSelfMember().getVoiceState();

    try {
      boolean userInSameVoiceChannel = userVoiceState.getChannel().equals(botVoiceState.getChannel());
      if (userInSameVoiceChannel) {
        readSetPositionRequest(ce);
      } else {
        ce.getChannel().sendMessage(BotMessage.USER_NOT_IN_SAME_VC.getMessage()).queue();
      }
    } catch (NullPointerException e) {
      ce.getChannel().sendMessage(BotMessage.USER_NOT_IN_VC.getMessage()).queue();
    }
  }

  /**
   * Checks if the command request was formatted correctly
   * before setting the currently playing track's position.
   *
   * @param ce command event
   */
  private void readSetPositionRequest(CommandEvent ce) {
    String[] parameters = ce.getMessage().getContentRaw().split("\\s");
    int numberOfParameters = parameters.length - 1;

    boolean validNumberOfParameters = numberOfParameters == 1;
    if (validNumberOfParameters) {
      try {
        setCurrentlyPlayingTrackPosition(ce, parameters[1]);
      } catch (NumberFormatException e) {
        ce.getChannel().sendMessage(Failure.INVALID_TIME.text).queue();
      }
    } else {
      ce.getChannel().sendMessage(BotMessage.INVALID_NUMBER_OF_PARAMETERS.getMessage()).queue();
    }
  }

  /**
   * Checks if there is a track currently playing to set the position of.
   *
   * @param ce                  command event
   * @param trackPositionString user provided position of the track to be set to
   */
  private void setCurrentlyPlayingTrackPosition(CommandEvent ce, String trackPositionString) {
    AudioPlayer audioPlayer =
        PlayerManager.getINSTANCE().getPlaybackManager(ce.getGuild()).audioScheduler.getAudioPlayer();

    boolean currentlyPlayingTrack = !(audioPlayer.getPlayingTrack() == null);
    if (currentlyPlayingTrack) {
      setTrackPosition(ce, trackPositionString, audioPlayer);
    } else {
      ce.getChannel().sendMessage(Failure.NOTHING_PLAYING.text).queue();
    }
  }

  /**
   * Sets the currently playing track's position.
   *
   * @param ce                  command event
   * @param trackPositionString user provided position of the track to be set to
   * @param audioPlayer         audio player
   */
  private void setTrackPosition(CommandEvent ce, String trackPositionString,
                                AudioPlayer audioPlayer) {
    long trackPositionToSet = convertTimeToLong(ce, trackPositionString);

    boolean requestedTrackPositionCanBeSet = audioPlayer.getPlayingTrack().getDuration() > trackPositionToSet;
    if (requestedTrackPositionCanBeSet) {
      audioPlayer.getPlayingTrack().setPosition(trackPositionToSet);
      sendSetPositionConfirmation(ce, trackPositionToSet);
    } else {
      ce.getChannel().sendMessage(Failure.EXCEED_TRACK_LENGTH.text).queue();
    }
  }

  /**
   * Converts user provided hh:mm:ss format to long data type.
   *
   * @param ce                  command event
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
      default -> ce.getChannel().sendMessage(BotMessage.INVALID_NUMBER_OF_PARAMETERS.getMessage()).queue();
    }

    // Conversion to milliseconds
    hours = hours * 3600000;
    minutes = minutes * 60000;
    seconds = seconds * 1000;
    return hours + minutes + seconds;
  }

  /**
   * Sends confirmation the track was set to the position.
   *
   * @param ce                 command event
   * @param trackPositionToSet user provided position of the track to be set to
   */
  private void sendSetPositionConfirmation(CommandEvent ce, Long trackPositionToSet) {
    String positionSet = TrackTime.convertLong(trackPositionToSet);
    StringBuilder setPositionConfirmation = new StringBuilder();
    setPositionConfirmation.append("**Set Position:** {*").append(positionSet).
        append("*} [").append(ce.getAuthor().getAsTag()).append("]");
    ce.getChannel().sendMessage(setPositionConfirmation).queue();
  }

  private enum Failure {
    NOTHING_PLAYING("Nothing is currently playing."),
    INVALID_TIME("Invalid time frame. Provide hh:mm:ss."),
    EXCEED_TRACK_LENGTH("Requested position exceeds track length.");

    public final String text;

    Failure(String text) {
      this.text = text;
    }
  }
}