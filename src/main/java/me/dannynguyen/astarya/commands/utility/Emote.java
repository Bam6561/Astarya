package me.dannynguyen.astarya.commands.utility;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import me.dannynguyen.astarya.commands.owner.Settings;
import net.dv8tion.jda.api.EmbedBuilder;

/**
 * Command invocation that provides a mentioned emote as an embed.
 *
 * @author Danny Nguyen
 * @version 1.8.15
 * @since 1.0
 */
public class Emote extends Command {
  /**
   * Associates the command with its properties.
   */
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
      EmbedBuilder embed = new EmbedBuilder();
      embed.setAuthor(ce.getMessage().getMentions().getCustomEmojis().get(0).getName());
      embed.setImage(ce.getMessage().getMentions().getCustomEmojis().get(0).getImageUrl());
      Settings.sendEmbed(ce, embed);
    } else {
      ce.getChannel().sendMessage("Provide an emote.").queue();
    }
  }
}