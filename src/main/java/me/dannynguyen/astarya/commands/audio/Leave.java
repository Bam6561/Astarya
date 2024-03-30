package me.dannynguyen.astarya.commands.audio;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import me.dannynguyen.astarya.commands.owner.Settings;
import net.dv8tion.jda.api.entities.GuildVoiceState;

/**
 * Command invocation that makes the bot leave the voice channel it's currently in.
 *
 * @author Danny Nguyen
 * @version 1.8.9
 * @since 1.1.0
 */
public class Leave extends Command {
  /**
   * Associates the command with its properties.
   */
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

    if (!botVoiceState.inAudioChannel()) {
      ce.getChannel().sendMessage("Not in a voice channel.").queue();
      return;
    }

    ce.getGuild().getAudioManager().closeAudioConnection();
    ce.getChannel().sendMessage("Leaving <#" + botVoiceState.getChannel().getId() + ">").queue();
  }
}