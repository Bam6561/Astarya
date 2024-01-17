package commands.audio;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import commands.owner.Settings;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.managers.AudioManager;

/**
 * Leave is a command invocation that makes the bot leave the voice channel it's currently in.
 *
 * @author Danny Nguyen
 * @version 1.7.8
 * @since 1.1.0
 */
public class Leave extends Command {
  public Leave() {
    this.name = "leave";
    this.aliases = new String[]{"leave", "l", "disconnect", "dc"};
    this.help = "Leaves the voice channel it's in.";
  }

  /**
   * Forces the bot to leave the voice channel it's currently connected to.
   *
   * @param ce command event
   */
  @Override
  protected void execute(CommandEvent ce) {
    Settings.deleteInvoke(ce);

    GuildVoiceState botVoiceState = ce.getGuild().getSelfMember().getVoiceState();
    boolean botIsInVoiceChannel = botVoiceState.inAudioChannel();

    if (botIsInVoiceChannel) {
      AudioManager audioManager = ce.getGuild().getAudioManager();
      audioManager.closeAudioConnection();

      String leaveChannel = "Leaving <#" + botVoiceState.getChannel().getId() + ">";
      ce.getChannel().sendMessage(leaveChannel).queue();
    } else {
      ce.getChannel().sendMessage("Not in a voice channel.").queue();
    }
  }
}