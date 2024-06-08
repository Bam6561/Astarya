package me.bam6561.astarya.commands.audio;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import me.bam6561.astarya.commands.audio.managers.PlayerManager;
import me.bam6561.astarya.commands.owner.Settings;
import me.bam6561.astarya.enums.BotMessage;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;

/**
 * Command invocation that sets the position of the currently playing track.
 *
 * @author Danny Nguyen
 * @version 1.9.4
 * @since 1.2.11
 */
public class SetPosition extends Command {
  /**
   * Associates the command with its properties.
   */
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

    AudioChannelUnion userChannel = ce.getMember().getVoiceState().getChannel();
    AudioChannelUnion botChannel = ce.getGuild().getSelfMember().getVoiceState().getChannel();

    if (userChannel == null) {
      ce.getChannel().sendMessage(BotMessage.USER_NOT_IN_VC.getMessage()).queue();
      return;
    }

    if (userChannel.equals(botChannel)) {
      new SetPositionRequest(ce).readRequest();
    } else {
      ce.getChannel().sendMessage(BotMessage.USER_NOT_IN_SAME_VC.getMessage()).queue();
    }
  }

  /**
   * Represents a track set position query.
   *
   * @param ce command event
   * @author Danny Nguyen
   * @version 1.8.12
   * @since 1.8.12
   */
  private record SetPositionRequest(CommandEvent ce) {
    /**
     * Checks if the command request was formatted correctly
     * before setting the currently playing track's position.
     */
    private void readRequest() {
      String[] parameters = ce.getMessage().getContentRaw().split("\\s");
      int numberOfParameters = parameters.length - 1;

      if (numberOfParameters != 1) {
        ce.getChannel().sendMessage(BotMessage.INVALID_NUMBER_OF_PARAMETERS.getMessage()).queue();
        return;
      }
      AudioPlayer audioPlayer = PlayerManager.getINSTANCE().getPlaybackManager(ce.getGuild()).audioScheduler.getAudioPlayer();
      if (audioPlayer.getPlayingTrack() == null) {
        ce.getChannel().sendMessage("Nothing is currently playing.").queue();
        return;
      }
      long trackPositionToSet = convertTimeToMilliseconds(parameters[1]);
      if (trackPositionToSet == -1) {
        ce.getChannel().sendMessage("Invalid time frame. Provide hh:mm:ss.").queue();
        return;
      }
      if (audioPlayer.getPlayingTrack().getDuration() > trackPositionToSet) {
        ce.getChannel().sendMessage("Requested position exceeds track length.").queue();
        return;
      }

      audioPlayer.getPlayingTrack().setPosition(trackPositionToSet);

      StringBuilder setPositionConfirmation = new StringBuilder();
      setPositionConfirmation.append("**Set Position:** {*").append(TrackTime.convertLong(trackPositionToSet)).append("*} [").append(ce.getAuthor().getAsTag()).append("]");
      ce.getChannel().sendMessage(setPositionConfirmation).queue();
    }

    /**
     * Converts user provided hh:mm:ss format to milliseconds.
     *
     * @param trackPositionString user provided position of the track to be set to
     * @return position of the track to be set to in milliseconds
     */
    private long convertTimeToMilliseconds(String trackPositionString) {
      try {
        String[] trackPositionTimeTypes = trackPositionString.split(":");
        long hours = 0;
        long minutes = 0;
        long seconds = 0;

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

        hours = hours * 3600000;
        minutes = minutes * 60000;
        seconds = seconds * 1000;
        return hours + minutes + seconds;
      } catch (NumberFormatException e) {
        return -1;
      }
    }
  }
}