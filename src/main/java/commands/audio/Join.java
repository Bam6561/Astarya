package commands.audio;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import commands.owner.Settings;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.managers.AudioManager;

public class Join extends Command {
  public Join() {
    this.name = "join";
    this.aliases = new String[]{"join", "j", "comein", "getinhere"};
    this.help = "Bot joins the same voice channel as the user.";
  }

  @Override
  protected void execute(CommandEvent ce) {
    Settings.deleteInvoke(ce);
    VoiceChannel vc = ce.getMember().getVoiceState().getChannel();
    AudioManager audioManager = ce.getGuild().getAudioManager();
    try { // Join voice channel
      audioManager.openAudioConnection(vc);
      ce.getChannel().sendMessage("Connected to <#" + vc.getId() + ">.").queue();
    } catch (Exception e) { // Insufficient permissions
      ce.getChannel().sendMessage("Unable to join <#" + vc.getId() + ">.").queue();
    }
  }
}
