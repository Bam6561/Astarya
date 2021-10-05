package commands.audio;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import commands.owner.Settings;
import net.dv8tion.jda.api.entities.GuildVoiceState;
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
    GuildVoiceState userVoiceState = ce.getMember().getVoiceState();
    GuildVoiceState botVoiceState = ce.getGuild().getSelfMember().getVoiceState();
    if (userVoiceState.inVoiceChannel()) { // User in any voice channel
      if (botVoiceState.inVoiceChannel()) { // Bot already in voice channel
        if (userVoiceState.getChannel()
            .equals(botVoiceState.getChannel())) { // User in same voice channel as bot
          ce.getChannel().sendMessage("Already in the same voice channel.").queue();
        } else { // User not in same voice channel as bot
          String alreadyConnected = "Already connected to <#"
              + botVoiceState.getChannel().getId() + ">";
          ce.getChannel().sendMessage(alreadyConnected).queue();
        }
      } else { // Bot not in any voice channel
        joinVoiceChannel(ce);
      }
    } else { // User not in any voice channel
      ce.getChannel().sendMessage("User not in a voice channel.").queue();
    }
  }

  private void joinVoiceChannel(CommandEvent ce) {
    VoiceChannel voiceChannel = ce.getMember().getVoiceState().getChannel();
    AudioManager audioManager = ce.getGuild().getAudioManager();
    try { // Join voice channel
      audioManager.openAudioConnection(voiceChannel);
      ce.getChannel().sendMessage("Connected to <#" + voiceChannel.getId() + ">").queue();
    } catch (Exception e) { // Insufficient permissions
      ce.getChannel().sendMessage("Unable to join <#" + voiceChannel.getId() + ">").queue();
    }
  }
}
