package commands.audio;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import commands.owner.Settings;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.managers.AudioManager;

public class Leave extends Command {
  public Leave() {
    this.name = "leave";
    this.aliases = new String[]{"leave", "l", "disconnect", "dc"};
    this.help = "Bot leaves the voice channel it is in.";
  }

  // Forces bot to leave its current voice channel
  @Override
  protected void execute(CommandEvent ce) {
    Settings.deleteInvoke(ce);

    GuildVoiceState botVoiceState = ce.getGuild().getSelfMember().getVoiceState();
    boolean botIsInVoiceChannel = botVoiceState.inVoiceChannel();

    if (botIsInVoiceChannel) {
      AudioManager audioManager = ce.getGuild().getAudioManager();
      audioManager.closeAudioConnection();

      String leaveChannel = "Leaving <#" + botVoiceState.getChannel().getId() + ">";
      ce.getChannel().sendMessage(leaveChannel).queue();
    } else {
      ce.getChannel().sendMessage("Not in a voice channel.").queue();
    }
  }
}