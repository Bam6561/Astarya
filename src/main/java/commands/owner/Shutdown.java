package commands.owner;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;

public class Shutdown extends Command {
  public Shutdown() {
    this.name = "shutdown";
    this.aliases = new String[]{"shutdown", "turnoff", "terminate"};
    this.help = "Shuts the bot down.";
    this.ownerCommand = true;
  }

  @Override
  protected void execute(CommandEvent ce) {
    Settings.deleteInvoke(ce);
    EmbedBuilder display = new EmbedBuilder();
    display.setTitle("__Shutdown__");
    display.setDescription("Well, it was fun while it lasted. Change the world.... my final message. Goodbye. "
        + "\n**Lucyfer is shutting down.**");
    display
        .setImage("https://cdn.discordapp.com/attachments/761839761928355840/761839840538394634/lucyferShutdown.gif");
    Settings.sendEmbed(ce, display);
    ce.getJDA().shutdown();
  }
}