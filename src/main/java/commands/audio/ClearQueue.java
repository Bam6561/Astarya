package commands.audio;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import commands.audio.managers.AudioScheduler;
import commands.audio.managers.PlayerManager;
import commands.owner.Settings;
import net.dv8tion.jda.api.entities.GuildVoiceState;

/**
 * ClearQueue is a command invocation that clears the track queue.
 *
 * @author Danny Nguyen
 * @version 1.7.0
 * @since 1.2.2
 */
public class ClearQueue extends Command {
  public ClearQueue() {
    this.name = "clearqueue";
    this.aliases = new String[]{"clearqueue", "clear"};
    this.help = "Clears the track queue.";
  }

  /**
   * Determines whether the user is in the same voice channel as the bot to process a clearQueue command request.
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
        clearTrackQueue(ce);
      } else {
        ce.getChannel().sendMessage("User not in the same voice channel.").queue();
      }
    } catch (NullPointerException e) {
      ce.getChannel().sendMessage("User not in a voice channel.").queue();
    }
  }

  /**
   * Clears the track queue and its associated requester list.
   *
   * @param ce object containing information about the command event
   */
  private void clearTrackQueue(CommandEvent ce) {
    AudioScheduler audioScheduler = PlayerManager.getINSTANCE().getPlaybackManager(ce.getGuild()).audioScheduler;

    audioScheduler.getTrackQueue().clear();

    StringBuilder clearQueueConfirmation = new StringBuilder();
    clearQueueConfirmation.append("**Queue Clear:** [").append(ce.getAuthor().getAsTag()).append("]");
    ce.getChannel().sendMessage(clearQueueConfirmation).queue();
  }
}