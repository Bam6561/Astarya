package commands.about;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import commands.owner.Settings;

/**
 * Ping responds with the response time of the bot in milliseconds.
 *
 * @author Danny Nguyen
 * @version 1.5.4
 * @since 1.0
 */
public class Ping extends Command {
  public Ping() {
    this.name = "ping";
    this.aliases = new String[]{"ping", "ms"};
    this.help = "Responds with the response time of the bot in milliseconds.";
  }

  /**
   * Sends a message in chat and edits the message with the API response time.
   *
   * @param ce object containing information about the command event
   */
  protected void execute(CommandEvent ce) {
    Settings.deleteInvoke(ce);

    long time = System.currentTimeMillis();
    ce.getChannel().sendMessage("Ping:").queue(response ->
        response.editMessageFormat("Ping: %d ms", System.currentTimeMillis() - time).queue());
  }
}