package commands.audio;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import commands.owner.Settings;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.managers.AudioManager;

public class Leave extends Command {
  public Leave() {
    this.name = "leave";
    this.aliases = new String[]{"leave", "disconnect", "dc", "goaway", "getout"};
    this.help = "Leaves the voice channel the bot is in.";
  }

  @Override
  protected void execute(CommandEvent ce) {
    Settings.deleteInvoke(ce);
    GuildVoiceState botVoiceState = ce.getGuild().getSelfMember().getVoiceState();
    if (botVoiceState.inVoiceChannel()) {
      AudioManager audioManager = ce.getGuild().getAudioManager();
      audioManager.closeAudioConnection();
      String leaveChannel = "Leaving <#" + botVoiceState.getChannel().getId() + ">";
      ce.getChannel().sendMessage(leaveChannel).queue();
    } else {
      ce.getChannel().sendMessage("Not in a voice channel.").queue();
    }
  }
}