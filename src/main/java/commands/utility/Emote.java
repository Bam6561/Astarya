package commands.utility;

import astarya.BotMessage;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import commands.owner.Settings;
import net.dv8tion.jda.api.EmbedBuilder;

/**
 * Emote is a command invocation that provides a mentioned emote as an embed.
 *
 * @author Danny Nguyen
 * @version 1.7.12
 * @since 1.0
 */
public class Emote extends Command {
  public Emote() {
    this.name = "emote";
    this.aliases = new String[]{"emote", "emoji"};
    this.arguments = "[1]Emote";
    this.help = "Provides the mentioned emote as a file.";
  }

  /**
   * Sends an embed containing information about an emote.
   *
   * @param ce command event
   */
  @Override
  protected void execute(CommandEvent ce) {
    Settings.deleteInvoke(ce);

    boolean messageHasEmote = !ce.getMessage().getMentions().getCustomEmojis().isEmpty();
    if (messageHasEmote) {
      EmbedBuilder display = new EmbedBuilder();
      display.setAuthor(ce.getMessage().getMentions().getCustomEmojis().get(0).getName());
      display.setImage(ce.getMessage().getMentions().getCustomEmojis().get(0).getImageUrl());
      Settings.sendEmbed(ce, display);
    } else {
      ce.getChannel().sendMessage(BotMessage.Failure.EMOTE_SPECIFY_EMOTE.text).queue();
    }
  }
}