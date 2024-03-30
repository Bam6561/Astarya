package me.dannynguyen.astarya.commands.about;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import me.dannynguyen.astarya.commands.owner.Settings;
import net.dv8tion.jda.api.EmbedBuilder;

/**
 * Command invocation that shows a list of credits for the bot.
 *
 * @author Danny Nguyen
 * @version 1.8.6
 * @since 1.0
 */
public class Credits extends Command {
  /**
   * Associates command with its properties.
   */
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

    EmbedBuilder embed = new EmbedBuilder();
    embed.setAuthor("Credits");
    embed.setThumbnail(ce.getSelfUser().getAvatarUrl());
    embed.setDescription("Thank you to the following resources used in " +
        "Astarya's development, as well as all my friends who use the bot " +
        "regularly and leave feedback so the experience is as bug-free as possible.");
    embed.addField("APIs", "> [Discord](https://discord.com) | " +
        "[Genius](https://genius.com) | " +
        "[Spotify](https://open.spotify.com)", false);
    embed.addField("Libraries & Wrappers", "> [JDA](https://github.com/DV8FromTheWorld/JDA) | " +
        "[JDA-Chewtils](https://github.com/Chew/JDA-Chewtils) | " +
        "[LavaPlayer](https://github.com/sedmelluq/lavaplayer) | " +
        "[Spotify Web API Java](https://github.com/spotify-web-api-java/spotify-web-api-java)", false);
    embed.addField("References", "> [Kody Simpson](https://www.youtube.com/@KodySimpson) | " +
        "[MenuDocs](https://www.youtube.com/@MenuDocs) | " +
        "[TechToolBox](https://www.youtube.com/@TechToolboxOfficial)", false);
    Settings.sendEmbed(ce, embed);
  }
}
