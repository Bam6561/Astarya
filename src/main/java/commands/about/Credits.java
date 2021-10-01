package commands.about;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import commands.owner.Settings;
import net.dv8tion.jda.api.EmbedBuilder;

public class Credits extends Command {
  public Credits() {
    this.name = "credits";
    this.aliases = new String[]{"credits", "thankyou"};
    this.help = "Provides user with a list of credits for the bot.";
  }

  @Override
  protected void execute(CommandEvent ce) {
    Settings.deleteInvoke(ce);
    EmbedBuilder display = new EmbedBuilder();
    display.setTitle("__Credits__");
    display.setDescription("I'm grateful for the following resources for their assistance in the bot's development. " +
        "Most importantly, however, I also can't forget my friends who use the bot regularly and leave feedback " +
        "so that the bot is as bug-free as possible.");
    display.addField("**APIs, Libraries:**", "JDA, JDA-Utilities", false);
    display.addField("**References:**", "Kody Simpson, MenuDocs, TechToolBox", false);
    Settings.sendEmbed(ce, display);
  }
}
