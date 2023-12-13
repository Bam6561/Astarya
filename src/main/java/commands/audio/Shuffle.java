package commands.audio;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import commands.audio.managers.PlayerManager;
import commands.audio.objects.TrackQueueIndex;
import commands.owner.Settings;
import net.dv8tion.jda.api.entities.GuildVoiceState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/**
 * Shuffle is a command invocation that shuffles the track queue.
 *
 * @author Danny Nguyen
 * @version 1.7.2
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
   * @param ce object containing information about the command event
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
        ce.getChannel().sendMessage("User not in the same voice channel.").queue();
      }
    } catch (NullPointerException e) {
      ce.getChannel().sendMessage("User not in a voice channel.").queue();
    }
  }

  /**
   * Shuffles the track queue.
   *
   * @param ce object containing information about the command event
   */
  private void shuffleQueue(CommandEvent ce) {
    ArrayList<TrackQueueIndex> trackQueue = PlayerManager.getINSTANCE().getPlaybackManager(ce.getGuild()).audioScheduler.getTrackQueue();

    Random rand = new Random();
    for (int i = 0; i < trackQueue.size(); i++) {
      int indexSwitch = rand.nextInt(trackQueue.size());
      Collections.swap(trackQueue, i, indexSwitch);
    }
  }

  /**
   * Sends confirmation the track queue was shuffled.
   *
   * @param ce object containing information about the command evnet
   */
  private void sendShuffleConfirmation(CommandEvent ce) {
    StringBuilder shuffleConfirmation = new StringBuilder();
    shuffleConfirmation.append("**Shuffle:** [").append(ce.getAuthor().getAsTag()).append("]");
    ce.getChannel().sendMessage(shuffleConfirmation).queue();
  }
}
