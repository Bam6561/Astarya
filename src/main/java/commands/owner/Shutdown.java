package commands.owner;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;

/**
 * Shutdown is a command invocation that shuts the bot down.
 *
 * @author Danny Nguyen
 * @version 1.8.0
 * @since 1.0
 */
public class Shutdown extends Command {
  public Shutdown() {
    this.name = "shutdown";
    this.aliases = new String[]{"shutdown"};
    this.help = "Shuts Astarya down.";
    this.ownerCommand = true;
  }

  private enum Success {
    SHUTDOWN("Well, it was fun while it lasted. Change the world... " +
        "my final message. Goodbye. **Astarya is shutting down.**");

    public final String text;

    Success(String text) {
      this.text = text;
    }
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
    display.setDescription(Success.SHUTDOWN.text);
    Settings.sendEmbed(ce, display);

    ce.getJDA().shutdown();
  }
}