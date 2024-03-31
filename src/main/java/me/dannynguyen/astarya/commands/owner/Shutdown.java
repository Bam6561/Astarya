package me.dannynguyen.astarya.commands.owner;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;

/**
 * Command invocation that shuts the bot down.
 *
 * @author Danny Nguyen
 * @version 1.8.14
 * @since 1.0
 */
public class Shutdown extends Command {
  /**
   * Associates the command with its properties.
   */
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

    EmbedBuilder embed = new EmbedBuilder();
    embed.setAuthor("Shutdown");
    embed.setDescription("Well, it was fun while it lasted. Change the world... my final message. Goodbye. **Astarya is shutting down.**");
    Settings.sendEmbed(ce, embed);

    ce.getJDA().shutdown();
  }
}