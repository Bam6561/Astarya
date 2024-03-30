package me.dannynguyen.astarya.commands.audio;

import me.dannynguyen.astarya.enums.BotMessage;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import me.dannynguyen.astarya.commands.audio.managers.PlayerManager;
import me.dannynguyen.astarya.commands.audio.objects.TrackQueueIndex;
import me.dannynguyen.astarya.commands.owner.Settings;
import net.dv8tion.jda.api.entities.GuildVoiceState;

import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Shuffle is a command invocation that shuffles the queue.
 *
 * @author Danny Nguyen
 * @version 1.7.13
 * @since 1.2.6
 */
public class Shuffle extends Command {
  public Shuffle() {
    this.name = "shuffle";
    this.aliases = new String[]{"shuffle", "mix", "sh"};
    this.help = "Shuffles the track queue.";
  }

  /**
   * Checks if the user is in the same voice channel as the bot to read a shuffle command request.
   *
   * @param ce command event
   * @throws NullPointerException user not in the same voice channel
   */
  @Override
  protected void execute(CommandEvent ce) {
    Settings.deleteInvoke(ce);

    GuildVoiceState userVoiceState = ce.getMember().getVoiceState();
    GuildVoiceState botVoiceState = ce.getGuild().getSelfMember().getVoiceState();

    try {
      boolean userInSameVoiceChannel = userVoiceState.getChannel().equals(botVoiceState.getChannel());
      if (userInSameVoiceChannel) {
        shuffleQueue(ce);
        sendShuffleConfirmation(ce);
      } else {
        ce.getChannel().sendMessage(BotMessage.USER_NOT_IN_SAME_VC.getMessage()).queue();
      }
    } catch (NullPointerException e) {
      ce.getChannel().sendMessage(BotMessage.USER_NOT_IN_VC.getMessage()).queue();
    }
  }

  /**
   * Shuffles the queue.
   *
   * @param ce command event
   */
  private void shuffleQueue(CommandEvent ce) {
    List<TrackQueueIndex> trackQueue =
        PlayerManager.getINSTANCE().getPlaybackManager(ce.getGuild()).audioScheduler.getTrackQueue();

    Random rand = new Random();
    for (int i = 0; i < trackQueue.size(); i++) {
      int indexSwitch = rand.nextInt(trackQueue.size());
      Collections.swap(trackQueue, i, indexSwitch);
    }
  }

  /**
   * Sends confirmation the queue was shuffled.
   *
   * @param ce object containing information about the command event
   */
  private void sendShuffleConfirmation(CommandEvent ce) {
    StringBuilder shuffleConfirmation = new StringBuilder();
    shuffleConfirmation.append("**Shuffle:** [").append(ce.getAuthor().getAsTag()).append("]");
    ce.getChannel().sendMessage(shuffleConfirmation).queue();
  }
}
