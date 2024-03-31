package me.dannynguyen.astarya.commands.audio;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import me.dannynguyen.astarya.commands.audio.managers.PlayerManager;
import me.dannynguyen.astarya.commands.owner.Settings;
import me.dannynguyen.astarya.enums.BotMessage;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;

import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Command invocation that shuffles the queue.
 *
 * @author Danny Nguyen
 * @version 1.8.12
 * @since 1.2.6
 */
public class Shuffle extends Command {
  /**
   * Associates the command with its properties.
   */
  public Shuffle() {
    this.name = "shuffle";
    this.aliases = new String[]{"shuffle", "mix", "sh"};
    this.help = "Shuffles the track queue.";
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
      shuffleQueue(ce);
    } else {
      ce.getChannel().sendMessage(BotMessage.USER_NOT_IN_SAME_VC.getMessage()).queue();
    }
  }

  /**
   * Shuffles the queue.
   *
   * @param ce command event
   */
  private void shuffleQueue(CommandEvent ce) {
    List<TrackQueueIndex> trackQueue = PlayerManager.getINSTANCE().getPlaybackManager(ce.getGuild()).audioScheduler.getTrackQueue();

    Random rand = new Random();
    for (int i = 0; i < trackQueue.size(); i++) {
      int indexSwitch = rand.nextInt(trackQueue.size());
      Collections.swap(trackQueue, i, indexSwitch);
    }

    StringBuilder shuffleConfirmation = new StringBuilder();
    shuffleConfirmation.append("**Shuffle:** [").append(ce.getAuthor().getAsTag()).append("]");
    ce.getChannel().sendMessage(shuffleConfirmation).queue();
  }
}
