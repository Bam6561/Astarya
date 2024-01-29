package commands.owner;

import astarya.BotMessage;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;

/**
 * Shutdown is a command invocation that shuts the bot down.
 *
 * @author Danny Nguyen
 * @version 1.7.15
 * @since 1.0
 */
public class Shutdown extends Command {
  public Shutdown() {
    this.name = "shutdown";
    this.aliases = new String[]{"shutdown"};
    this.help = "Shuts Astarya down.";
    this.ownerCommand = true;
  }

  /**
   * Kills the Java application.
   *
   * @param ce command event
   */
  @Override
  protected void execute(CommandEvent ce) {
    Settings.deleteInvoke(ce);

    EmbedBuilder display = new EmbedBuilder();
    display.setAuthor("Shutdown");
    display.setDescription(BotMessage.Success.SHUTDOWN.text);
    Settings.sendEmbed(ce, display);

    ce.getJDA().shutdown();
  }
}