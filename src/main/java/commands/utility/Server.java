package commands.utility;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import commands.owner.Settings;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;

import java.time.format.DateTimeFormatter;

/**
 * Server is a command invocation that provides information on the Discord server.
 *
 * @author Danny Nguyen
 * @version 1.6.6
 * @since 1.0
 */
public class Server extends Command {
  public Server() {
    this.name = "serverinfo";
    this.aliases = new String[]{"server", "serverinfo"};
    this.help = "Provides information on the Discord server.";
  }

  /**
   * Sends an embed containing information about the Discord server.
   *
   * @param ce command event
   */
  @Override
  protected void execute(CommandEvent ce) {
    Settings.deleteInvoke(ce);

    Guild server = ce.getGuild();
    int textChannelCount = server.getTextChannels().size();
    int voiceChannelCount = server.getVoiceChannels().size();
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm");

    EmbedBuilder display = new EmbedBuilder();
    display.setAuthor("Server");
    display.setTitle(server.getName());
    display.setDescription("**Owner:** " + server.getOwner().getAsMention()
        + "\n**Server Id:** `" + server.getId()
        + "`\n**Time Created:** `" + server.getTimeCreated().format(dtf) + " GMT`"
        + "\n**Channels:** `" + (textChannelCount + voiceChannelCount)
        + "` (`" + textChannelCount + "` Text | `" + voiceChannelCount + "` Voice)"
        + "\n**Roles:** `" + server.getRoles().size()
        + "` **Emotes:** `" + server.getEmojis().size()
        + "` **Boosts:** `" + server.getBoostCount() + "`");
    display.setThumbnail(server.getIconUrl());
    display.setImage(server.getBannerUrl());
    Settings.sendEmbed(ce, display);
  }
}
