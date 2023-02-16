package commands.owner;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;

public class Shutdown extends Command {
  public Shutdown() {
    this.name = "shutdown";
    this.aliases = new String[]{"shutdown", "nuke"};
    this.help = "Shuts the bot down.";
    this.ownerCommand = true;
  }

  // Kills the bot application
  @Override
  protected void execute(CommandEvent ce) {
    Settings.deleteInvoke(ce);

    EmbedBuilder display = new EmbedBuilder();
    display.setTitle("__Shutdown__");
    display.setDescription("Well, it was fun while it lasted. Change the world... " +
        "my final message. Goodbye. **Lucyfer is shutting down.**");

    Settings.sendEmbed(ce, display);
    ce.getJDA().shutdown();
  }
}