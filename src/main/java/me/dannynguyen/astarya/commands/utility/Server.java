package me.dannynguyen.astarya.commands.utility;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import me.dannynguyen.astarya.commands.owner.Settings;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;

import java.time.format.DateTimeFormatter;

/**
 * Command invocation that provides information on the Discord server.
 *
 * @author Danny Nguyen
 * @version 1.8.5
 * @since 1.0
 */
public class Server extends Command {
  /**
   * Associates command with its properties.
   */
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

    StringBuilder description = new StringBuilder();
    description.append("**Owner:** ").append(server.getOwner().getAsMention()).append("\n");
    description.append("**Server Id:** `").append(server.getId()).append("`\n");
    description.append("**Time Created:** `").append(server.getTimeCreated().format(dtf)).append(" GMT`\n");
    description.append("**Channels:** `").append(textChannelCount + voiceChannelCount).append("` (`").append(textChannelCount).append("` Text | `").append(voiceChannelCount).append("` Voice)\n");
    description.append("**Roles:** `").append(server.getRoles().size()).append("` ");
    description.append("**Emotes:** `").append(server.getEmojis().size()).append("` ");
    description.append("**Boosts:** `").append(server.getBoostCount()).append("`");

    EmbedBuilder display = new EmbedBuilder();
    display.setAuthor("Server");
    display.setTitle(server.getName());
    display.setDescription(description);
    display.setThumbnail(server.getIconUrl());
    display.setImage(server.getBannerUrl());
    Settings.sendEmbed(ce, display);
  }
}
