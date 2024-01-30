package commands.audio;

import astarya.BotMessage;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import commands.audio.managers.PlayerManager;
import commands.audio.objects.TrackQueueIndex;
import commands.audio.utility.TrackTime;
import commands.owner.Settings;
import net.dv8tion.jda.api.entities.GuildVoiceState;

import java.util.List;

/**
 * PlayNext is a command invocation that sets the next track to be played in the queue.
 *
 * @author Danny Nguyen
 * @version 1.7.16
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
        ce.getChannel().sendMessage(BotMessage.Failure.USER_NOT_IN_SAME_VC.text).queue();
      }
    } catch (NullPointerException e) {
      ce.getChannel().sendMessage(BotMessage.Failure.USER_NOT_IN_VC.text).queue();
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
        ce.getChannel().sendMessage(BotMessage.Failure.PLAYNEXT_SPECIFY.text).queue();
      }
    } else {
      ce.getChannel().sendMessage(BotMessage.Failure.INVALID_NUMBER_OF_PARAMETERS.text).queue();
    }
  }

  /**
   * Checks if the playNext request is within queue range bounds before setting
   * a track to immediately play after the currently playing track.
   *
   * @param ce          command event
   * @param queueNumber track in queue to be played next
   * @throws IndexOutOfBoundsException user provided an index out of range of the queue
   */
  private void processPlayNextRequest(CommandEvent ce, int queueNumber) {
    try {
      List<TrackQueueIndex> trackQueue =
          PlayerManager.getINSTANCE().getPlaybackManager(ce.getGuild()).audioScheduler.getTrackQueue();
      AudioTrack audioTrack = trackQueue.get(queueNumber).getAudioTrack();
      String trackDuration = TrackTime.convertLong(audioTrack.getDuration());

      // Displayed indices to users are different from data index so subtract 1
      queueNumber = queueNumber - 1;

      playNext(ce, queueNumber, trackQueue, audioTrack);
      sendPlayNextConfirmation(ce, queueNumber, trackQueue, audioTrack, trackDuration);
    } catch (IndexOutOfBoundsException e) {
      ce.getChannel().sendMessage(BotMessage.Failure.INVALID_QUEUE_NUMBER.text).queue();
    }
  }

  /**
   * Set the position of the chosen track to the top of the queue.
   *
   * @param ce          command event
   * @param queueNumber queue index
   * @param trackQueue  queue
   * @param audioTrack  chosen track
   */
  private void playNext(CommandEvent ce, int queueNumber,
                        List<TrackQueueIndex> trackQueue, AudioTrack audioTrack) {
    trackQueue.remove(queueNumber);
    String requester = "[" + ce.getAuthor().getAsTag() + "]";
    trackQueue.add(0, new TrackQueueIndex(audioTrack, requester));
  }


  /**
   * Sends a confirmation that the chosen track's position was set to the top of the queue.
   *
   * @param ce            command event
   * @param queueNumber   queue index
   * @param trackQueue    queue
   * @param audioTrack    chosen track
   * @param trackDuration chosen track's duration
   */
  private void sendPlayNextConfirmation(CommandEvent ce, int queueNumber, List<TrackQueueIndex> trackQueue,
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