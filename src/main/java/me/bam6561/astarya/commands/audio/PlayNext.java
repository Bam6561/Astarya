package me.bam6561.astarya.commands.audio;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import me.bam6561.astarya.commands.audio.managers.AudioScheduler;
import me.bam6561.astarya.commands.audio.managers.PlayerManager;
import me.bam6561.astarya.commands.owner.Settings;
import me.bam6561.astarya.enums.BotMessage;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;

import java.util.List;

/**
 * Command invocation that sets the next track to be played in the {@link AudioScheduler#getTrackQueue() queue}.
 *
 * @author Danny Nguyen
 * @version 1.8.10
 * @since 1.2.13
 */
public class PlayNext extends Command {
  /**
   * Associates the command with its properties.
   */
  public PlayNext() {
    this.name = "playnext";
    this.aliases = new String[]{"playnext", "pn"};
    this.arguments = "[1]QueueNumber";
    this.help = "Sets the next track to be played track in the track queue.";
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
      new PlayNextRequest(ce).interpretRequest();
    } else {
      ce.getChannel().sendMessage(BotMessage.USER_NOT_IN_SAME_VC.getMessage()).queue();
    }
  }

  /**
   * Represents a track to be played next query.
   *
   * @param ce command event
   * @author Danny Nguyen
   * @version 1.8.10
   * @since 1.8.10
   */
  private record PlayNextRequest(CommandEvent ce) {
    /**
     * Checks if the command request was formatted correctly
     * before changing the position of the chosen track.
     */
    private void interpretRequest() {
      String[] parameters = ce.getMessage().getContentRaw().split("\\s");
      int numberOfParameters = parameters.length;

      if (numberOfParameters == 2) {
        try {
          processPlayNextRequest(Integer.parseInt(parameters[1]));
        } catch (NumberFormatException e) {
          ce.getChannel().sendMessage("Provide next track number.").queue();
        }
      } else {
        ce.getChannel().sendMessage(BotMessage.INVALID_NUMBER_OF_PARAMETERS.getMessage()).queue();
      }
    }

    /**
     * Checks if the command request is within queue range bounds before
     * setting a track to immediately play after the currently playing track.
     *
     * @param queueNumber track in queue to be played next
     */
    private void processPlayNextRequest(int queueNumber) {
      try {
        List<TrackQueueIndex> trackQueue = PlayerManager.getINSTANCE().getPlaybackManager(ce.getGuild()).audioScheduler.getTrackQueue();
        AudioTrack audioTrack = trackQueue.get(queueNumber).getAudioTrack();
        String trackDuration = TrackTime.convertLong(audioTrack.getDuration());

        // Displayed indices to users are different from data index so subtract 1
        queueNumber = queueNumber - 1;

        trackQueue.remove(queueNumber);
        String requester = "[" + ce.getAuthor().getAsTag() + "]";
        trackQueue.add(0, new TrackQueueIndex(audioTrack, requester));

        StringBuilder playNextConfirmation = new StringBuilder();
        playNextConfirmation.append("**Play Next:** **[").append(queueNumber + 1).append("]** `")
            .append(audioTrack.getInfo().title).append("` {*").append(trackDuration).append("*} ")
            .append(trackQueue.get(queueNumber).getRequester()).append(" [").append(ce.getAuthor().getAsTag()).append("]");
        ce.getChannel().sendMessage(playNextConfirmation).queue();
      } catch (IndexOutOfBoundsException e) {
        ce.getChannel().sendMessage(BotMessage.INVALID_QUEUE_NUMBER.getMessage()).queue();
      }
    }
  }
}