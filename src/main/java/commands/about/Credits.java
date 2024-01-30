package commands.about;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import commands.owner.Settings;
import net.dv8tion.jda.api.EmbedBuilder;

/**
 * Credits is a command invocation that shows a list of credits for the bot.
 *
 * @author Danny Nguyen
 * @version 1.8.1
 * @since 1.0
 */
public class Credits extends Command {
  public Credits() {
    this.name = "credits";
    this.aliases = new String[]{"credits"};
    this.help = "Shows a list of credits for Astarya.";
  }

  private enum Success {
    THANK_YOU("Thank you to the following resources used in " +
        "Astarya's development, as well as all my friends who use the bot " +
        "regularly and leave feedback so the experience is as bug-free as possible."),
    APIS("> [Discord](https://discord.com) | " +
        "[Genius](https://genius.com) | " +
        "[Spotify](https://open.spotify.com)"),
    LIBRARIES_WRAPPERS("> [JDA](https://github.com/DV8FromTheWorld/JDA) | " +
        "[JDA-Chewtils](https://github.com/Chew/JDA-Chewtils) | " +
        "[LavaPlayer](https://github.com/sedmelluq/lavaplayer) | " +
        "[Spotify Web API Java](https://github.com/spotify-web-api-java/spotify-web-api-java)"),
    REFERENCES("> [Kody Simpson](https://www.youtube.com/@KodySimpson) | " +
        "[MenuDocs](https://www.youtube.com/@MenuDocs) | " +
        "[TechToolBox](https://www.youtube.com/@TechToolboxOfficial)");
    
    public final String text;

    Success(String text) {
      this.text = text;
    }
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
    display.setDescription(Success.THANK_YOU.text);
    display.addField("APIs", Success.APIS.text, false);
    display.addField("Libraries & Wrappers", Success.LIBRARIES_WRAPPERS.text, false);
    display.addField("References", Success.REFERENCES.text, false);
    Settings.sendEmbed(ce, display);
  }
}
