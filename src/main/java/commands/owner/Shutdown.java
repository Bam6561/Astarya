package commands.owner;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;

/**
 * Shutdown is a command invocation that shuts the bot down.
 *
 * @author Danny Nguyen
 * @version 1.6.1
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
   * @param ce object containing information about the command event
   */
  @Override
  protected void execute(CommandEvent ce) {
    Settings.deleteInvoke(ce);

    EmbedBuilder display = new EmbedBuilder();
    display.setTitle("__Shutdown__");
    display.setDescription("Well, it was fun while it lasted. Change the world... " +
        "my final message. Goodbye. **Astarya is shutting down.**");
    Settings.sendEmbed(ce, display);

    ce.getJDA().shutdown();
  }
}