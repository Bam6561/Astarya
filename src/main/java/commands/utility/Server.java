package commands.utility;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import commands.owner.Settings;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;

import java.time.format.DateTimeFormatter;

public class Server extends Command {
  public Server() {
    this.name = "serverinfo";
    this.aliases = new String[]{"server", "serverinfo"};
    this.help = "Provides information on the server.";
  }

  // Sends an embed containing information about the Discord server
  @Override
  protected void execute(CommandEvent ce) {
    Settings.deleteInvoke(ce);

    Guild server = ce.getGuild();
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm");
    int textChannelCount = server.getTextChannels().size();
    int voiceChannelCount = server.getVoiceChannels().size();

    EmbedBuilder display = new EmbedBuilder();
    display.setTitle("__" + server.getName() + "__");
    display.setDescription("**Owner:** " + server.getOwner().getAsMention()
        + "\n**Server ID:** `" + server.getId()
        + "`\n**Time Created:** `" + server.getTimeCreated().format(dtf) + " GMT`"
        + "\n**Channels:** `" + (textChannelCount + voiceChannelCount)
        + "` (`" + textChannelCount + "` Text | `" + voiceChannelCount + "` Voice)"
        + "\n**Roles:** `" + server.getRoles().size()
        + "` **Emotes:** `" + server.getEmotes().size()
        + "` **Boosts:** `" + server.getBoostCount() + "`");
    display.setThumbnail(server.getIconUrl());
    display.setImage(server.getBannerUrl());

    Settings.sendEmbed(ce, display);
  }
}
