package me.dannynguyen.astarya.commands.audio;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import me.dannynguyen.astarya.commands.audio.managers.AudioScheduler;
import me.dannynguyen.astarya.commands.audio.managers.PlayerManager;
import me.dannynguyen.astarya.commands.owner.Settings;
import me.dannynguyen.astarya.enums.BotMessage;
import net.dv8tion.jda.api.entities.GuildVoiceState;

/**
 * ClearQueue is a command invocation that clears the queue.
 *
 * @author Danny Nguyen
 * @version 1.7.12
 * @since 1.2.2
 */
public class ClearQueue extends Command {
  public ClearQueue() {
    this.name = "clearqueue";
    this.aliases = new String[]{"clearqueue", "clear"};
    this.help = "Clears the track queue.";
  }

  /**
   * Checks if the user is in the same voice channel as the bot to read a clearQueue command request.
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
        clearTrackQueue(ce);
      } else {
        ce.getChannel().sendMessage(BotMessage.USER_NOT_IN_SAME_VC.getMessage()).queue();
      }
    } catch (NullPointerException e) {
      ce.getChannel().sendMessage(BotMessage.USER_NOT_IN_VC.getMessage()).queue();
    }
  }

  /**
   * Clears the queue and its associated requesters.
   *
   * @param ce command event
   */
  private void clearTrackQueue(CommandEvent ce) {
    AudioScheduler audioScheduler = PlayerManager.getINSTANCE().getPlaybackManager(ce.getGuild()).audioScheduler;

    audioScheduler.getTrackQueue().clear();
    sendClearQueueConfirmation(ce);
  }

  /**
   * Sends a confirmation the queue was cleared.
   *
   * @param ce command event
   */
  private void sendClearQueueConfirmation(CommandEvent ce) {
    StringBuilder clearQueueConfirmation = new StringBuilder();
    clearQueueConfirmation.append("**Queue Clear:** [").append(ce.getAuthor().getAsTag()).append("]");
    ce.getChannel().sendMessage(clearQueueConfirmation).queue();
  }
}