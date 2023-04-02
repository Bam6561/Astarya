package commands.about;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import commands.owner.Settings;
import net.dv8tion.jda.api.EmbedBuilder;

/**
 * Credits is a command invocation that shows a list of credits for the bot.
 *
 * @author Danny Nguyen
 * @version 1.6.1
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
   * @param ce object containing information about the command event
   */
  @Override
  protected void execute(CommandEvent ce) {
    Settings.deleteInvoke(ce);

    EmbedBuilder display = new EmbedBuilder();
    display.setTitle("__Credits__");
    display.setThumbnail(ce.getSelfUser().getAvatarUrl());
    display.setDescription("Thank you to the following resources used in " +
        "Astarya's development, as well as all my friends who use the bot " +
        "regularly and leave feedback so the experience is as bug-free as possible.");
    display.addField("APIs", "> [Discord](https://discord.com) | " +
        "[Spotify](https://open.spotify.com)", false);
    display.addField("Libraries & Wrappers", "> [JDA](https://github.com/DV8FromTheWorld/JDA) | " +
        "[JDA-Chewtils](https://github.com/Chew/JDA-Chewtils) | " +
        "[LavaPlayer](https://github.com/sedmelluq/lavaplayer) | " +
        "[Spotify Web API Java](https://github.com/spotify-web-api-java/spotify-web-api-java)", false);
    display.addField("References", "> [Kody Simpson](https://www.youtube.com/@KodySimpson) | " +
        "[MenuDocs](https://www.youtube.com/@MenuDocs) | " +
        "[TechToolBox](https://www.youtube.com/@TechToolboxOfficial)", false);
    Settings.sendEmbed(ce, display);
  }
}
