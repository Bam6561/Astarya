package commands.miscellaneous;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import commands.owner.Settings;

public class Ping extends Command {
  public Ping() {
    this.name = "ping";
    this.aliases = new String[]{"ping", "ms"};
    this.help = "Response time of the bot in milliseconds.";
  }

  protected void execute(CommandEvent ce) {
    Settings.deleteInvoke(ce);
    long time = System.currentTimeMillis();
    ce.getChannel().sendMessage("Ping:").queue(response ->
        response.editMessageFormat("Ping: %d ms", System.currentTimeMillis() - time).queue());
  }
}