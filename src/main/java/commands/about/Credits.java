package commands.about;

import astarya.BotMessage;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import commands.owner.Settings;
import net.dv8tion.jda.api.EmbedBuilder;

/**
 * Credits is a command invocation that shows a list of credits for the bot.
 *
 * @author Danny Nguyen
 * @version 1.7.15
 * @since 1.0
 */
public class Credits extends Command {
  public Credits() {
    this.name = "credits";
    this.aliases = new String[]{"credits"};
    this.help = "Shows a list of credits for Astarya.";
  }

  /**
   * Sends an embed containing a list of acknowledgements for the bot.
   *
   * @param ce command event
   */
  @Override
  protected void execute(CommandEvent ce) {
    Settings.deleteInvoke(ce);

    EmbedBuilder display = new EmbedBuilder();
    display.setAuthor("Credits");
    display.setThumbnail(ce.getSelfUser().getAvatarUrl());
    display.setDescription(BotMessage.Success.CREDITS_THANK_YOU.text);
    display.addField("APIs", BotMessage.Success.CREDITS_APIS.text, false);
    display.addField("Libraries & Wrappers", BotMessage.Success.CREDITS_LIBRARIES_WRAPPERS.text, false);
    display.addField("References", BotMessage.Success.CREDITS_REFERENCES.text, false);
    Settings.sendEmbed(ce, display);
  }
}
