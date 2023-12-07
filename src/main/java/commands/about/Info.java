package commands.about;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import commands.owner.Settings;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;

import java.lang.management.ManagementFactory;
import java.time.format.DateTimeFormatter;

/**
 * Info is a command invocation that details information about the bot and its developer.
 *
 * @author Danny Nguyen
 * @version 1.6.6
 * @since 1.0
 */
public class Info extends Command {
  public Info() {
    this.name = "info";
    this.aliases = new String[]{"info"};
    this.help = "Details information about Astarya and its developer.";
  }

  /**
   * Sends an embed containing information about the bot's uptime, developer, and development.
   *
   * @param ce object containing information about the command event
   */
  @Override
  protected void execute(CommandEvent ce) {
    Settings.deleteInvoke(ce);

    JDA jda = ce.getJDA();
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm");

    EmbedBuilder display = new EmbedBuilder();
    display.setAuthor("Info");
    display.setDescription("**Developer:** Bam#3531 \n" +
        "**Developer Id:** 204448598539239424 \n" +
        "**Bot:** " + jda.getSelfUser().getAsMention() + "\n" +
        "**Created:** `" + jda.getSelfUser().getTimeCreated().format(dtf) + " GMT` \n" +
        "**Version:** `1.6.8.1` \n" +
        "**Language:** `Java` \n" +
        "**Source Code:** [GitHub](https://github.com/Bam6561/Astarya) \n" +
        "**Uptime:** " + getUptime());
    display.setThumbnail(jda.getSelfUser().getAvatarUrl());
    Settings.sendEmbed(ce, display);
  }

  /**
   * Gets the application's lifespan and converts to readable conventional time.
   * <p>
   * Uptime Clock
   * Credit to Almighty Alpaca - slightly modified version
   * https://github.com/Java-Discord-Bot-System/Plugin-Uptime/blob/master/src/main/java/com
   * /almightyalpaca/discord/bot/plugin/uptime/UptimePlugin.java#L28-L42
   * </p>
   *
   * @return uptime of the bot application
   */
  private String getUptime() {
    final long duration = ManagementFactory.getRuntimeMXBean().getUptime();
    final long years = duration / 31104000000L;
    final long months = duration / 2592000000L % 12;
    final long days = duration / 86400000L % 30;
    final long hours = duration / 3600000L % 24;
    final long minutes = duration / 60000L % 60;
    final long seconds = duration / 1000L % 60;
    return (years == 0 ? "" : years + "yr ") + (months == 0 ? "" : months + "mo ")
        + (days == 0 ? "" : days + "d ") + (hours == 0 ? "" : hours + "h ") +
        (minutes == 0 ? "" : minutes + "m ") + (seconds == 0 ? "" : seconds + "s");
  }
}