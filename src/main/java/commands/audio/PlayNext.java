package commands.audio;

import astarya.Text;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import commands.audio.managers.PlayerManager;
import commands.audio.objects.TrackQueueIndex;
import commands.audio.utility.TimeConversion;
import commands.owner.Settings;
import net.dv8tion.jda.api.entities.GuildVoiceState;

import java.util.ArrayList;

/**
 * PlayNext is a command invocation that sets the next track to be played in the track queue.
 *
 * @author Danny Nguyen
 * @version 1.7.8
 * @since 1.2.13
 */
public class PlayNext extends Command {
  public PlayNext() {
    this.name = "playnext";
    this.aliases = new String[]{"playnext", "pn"};
    this.arguments = "[1]QueueNumber";
    this.help = "Sets the next track to be played track in the track queue.";
  }

  /**
   * Checks if the user is in the same voice channel as the bot to read a playNext command request.
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
        readPlayNextRequest(ce);
      } else {
        ce.getChannel().sendMessage(Text.NOT_IN_SAME_VC.value()).queue();
      }
    } catch (NullPointerException e) {
      ce.getChannel().sendMessage(Text.NOT_IN_VC.value()).queue();
    }
  }

  /**
   * Checks if the playNext command request was formatted
   * correctly before changing the position of the chosen track.
   *
   * @param ce command event
   * @throws NumberFormatException user provided non-integer value
   */
  private void readPlayNextRequest(CommandEvent ce) {
    String[] parameters = ce.getMessage().getContentRaw().split("\\s");
    int numberOfParameters = parameters.length;

    boolean validNumberOfParameters = numberOfParameters == 2;
    if (validNumberOfParameters) {
      try {
        processPlayNextRequest(ce, Integer.parseInt(parameters[1]));
      } catch (NumberFormatException e) {
        ce.getChannel().sendMessage("Specify an integer to play the next track number.").queue();
      }
    } else {
      ce.getChannel().sendMessage("Invalid number of parameters.").queue();
    }
  }

  /**
   * Checks if the playNext request is within queue range bounds before setting
   * a track to immediately play after the currently playing track.
   *
   * @param ce          command event
   * @param queueNumber track in the track queue to be played next
   * @throws IndexOutOfBoundsException user provided an index out of range of the track queue
   */
  private void processPlayNextRequest(CommandEvent ce, int queueNumber) {
    try {
      ArrayList<TrackQueueIndex> trackQueue = PlayerManager.getINSTANCE().getPlaybackManager(ce.getGuild()).audioScheduler.getTrackQueue();

      // Displayed index to users are different from data index so subtract 1
      queueNumber = queueNumber - 1;

      AudioTrack audioTrack = trackQueue.get(queueNumber).getAudioTrack();
      String trackDuration = TimeConversion.convert(audioTrack.getDuration());

      playNext(ce, queueNumber, trackQueue, audioTrack);
      sendPlayNextConfirmation(ce, queueNumber, trackQueue, audioTrack, trackDuration);
    } catch (IndexOutOfBoundsException e) {
      ce.getChannel().sendMessage(Text.INVALID_QUEUE_NUMBER.value()).queue();
    }
  }

  /**
   * Set the position of the chosen track to the top of the track queue.
   *
   * @param ce          command event
   * @param queueNumber track queue index
   * @param trackQueue  array list containing the tracks
   * @param audioTrack  chosen track
   */
  private void playNext(CommandEvent ce, int queueNumber,
                        ArrayList<TrackQueueIndex> trackQueue, AudioTrack audioTrack) {
    trackQueue.remove(queueNumber);
    String requester = "[" + ce.getAuthor().getAsTag() + "]";
    trackQueue.add(0, new TrackQueueIndex(audioTrack, requester));
  }


  /**
   * Sends a confirmation that the chosen track's position was set to the top of the track queue.
   *
   * @param ce            command event
   * @param queueNumber   track queue index
   * @param trackQueue    array list containing the tracks
   * @param audioTrack    chosen track
   * @param trackDuration chosen track's duration
   */
  private void sendPlayNextConfirmation(CommandEvent ce, int queueNumber, ArrayList<TrackQueueIndex> trackQueue,
                                        AudioTrack audioTrack, String trackDuration) {
    StringBuilder playNextConfirmation = new StringBuilder();
    playNextConfirmation.append("**Play Next:** **[").append(queueNumber + 1).
        append("]** `").append(audioTrack.getInfo().title).
        append("` {*").append(trackDuration).append("*} ").
        append(trackQueue.get(queueNumber).getRequester()).append(" [").
        append(ce.getAuthor().getAsTag()).append("]");
    ce.getChannel().sendMessage(playNextConfirmation).queue();
  }
}