package commands.utility;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import commands.owner.Settings;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;

import java.time.format.DateTimeFormatter;

public class ServerInfo extends Command {
  public ServerInfo() {
    this.name = "serverinfo";
    this.aliases = new String[]{"serverinfo", "server"};
    this.help = "Provides information on the server.";
  }

  @Override
  protected void execute(CommandEvent ce) {
    Settings.deleteInvoke(ce);
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm");
    Guild guild = ce.getGuild();
    int textChannelCount = guild.getTextChannels().size();
    int voiceChannelCount = guild.getVoiceChannels().size();
    EmbedBuilder display = new EmbedBuilder();
    display.setTitle("__" + guild.getName() + "__");
    display.setDescription("**Owner:** " + guild.getOwner().getAsMention() + "\n**Server ID:** `" + guild.getId()
        + "`\n**Time Created:** `" + guild.getTimeCreated().format(dtf) + " GMT`\n**Region:** `"
        + guild.getRegion() + "`\n**Channels:** `" + (textChannelCount + voiceChannelCount) + "` (`"
        + textChannelCount + "` Text | `" + voiceChannelCount + "` Voice)"
        + "\n**Roles:** `" + guild.getRoles().size() + "` **Emotes:** `"
        + guild.getEmotes().size() + "` **Boosts:** `" + guild.getBoostCount()
        + "`");
    display.setThumbnail(guild.getIconUrl());
    display.setImage(guild.getBannerUrl());
    Settings.sendEmbed(ce, display);
  }
}
