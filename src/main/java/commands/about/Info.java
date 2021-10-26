package commands.about;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import commands.owner.Settings;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;

import java.lang.management.ManagementFactory;
import java.time.format.DateTimeFormatter;

public class Info extends Command {
  public Info() {
    this.name = "info";
    this.aliases = new String[]{"info", "about"};
    this.help = "Provides information on the bot and its developer.";
  }

  @Override
  protected void execute(CommandEvent ce) {
    Settings.deleteInvoke(ce);
    EmbedBuilder display = new EmbedBuilder();
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm");
    JDA jda = ce.getJDA();
    display.setTitle("__Info__");
    display.setDescription("**Developer:** " + "Bam#3531"
        + "\n**Bot:** " + jda.getSelfUser().getAsMention()
        + "\n**Created:** `" + jda.getSelfUser().getTimeCreated().format(dtf) + " GMT` " +
        "\n**Version:** `1.3.7.2` \n**Language:** `Java` " +
        "\n**Source:** https://github.com/ndanny09/lucyferBot \n**Uptime:** " + getUptime());
    display.setThumbnail(jda.getSelfUser().getAvatarUrl());
    Settings.sendEmbed(ce, display);
  }

  private String getUptime() {
    // Uptime Clock
    // Credit to Almighty Alpaca - slightly modified version
    // https://github.com/Java-Discord-Bot-System/Plugin-Uptime/blob/master/src/main/java/com
    // /almightyalpaca/discord/bot/plugin/uptime/UptimePlugin.java#L28-L42
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