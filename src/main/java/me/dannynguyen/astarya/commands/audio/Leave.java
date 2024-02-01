package me.dannynguyen.astarya.commands.audio;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import me.dannynguyen.astarya.commands.owner.Settings;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.managers.AudioManager;

/**
 * Leave is a command invocation that makes the bot leave the voice channel it's currently in.
 *
 * @author Danny Nguyen
 * @version 1.8.0
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

      ce.getChannel().sendMessage("Leaving <#" + botVoiceState.getChannel().getId() + ">").queue();
    } else {
      ce.getChannel().sendMessage(Failure.BOT_NOT_IN_VC.text).queue();
    }
  }

  private enum Failure {
    BOT_NOT_IN_VC("Not in a voice channel.");

    public final String text;

    Failure(String text) {
      this.text = text;
    }
  }
}