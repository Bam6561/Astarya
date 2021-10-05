package commands.audio;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import commands.audio.managers.PlayerManager;
import commands.owner.Settings;
import net.dv8tion.jda.api.entities.GuildVoiceState;

public class Pause extends Command {
  public Pause() {
    this.name = "pause";
    this.aliases = new String[]{"pause", "stop", "freeze"};
    this.arguments = "[0]pause";
    this.help = "Pauses the audio player.";
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
          setPauseState(ce);
        } else { // User not in same voice channel as bot
          ce.getChannel().sendMessage("User not in same voice channel.").queue();
        }
      } else { // Bot not in any voice channel
        ce.getChannel().sendMessage("Not in a voice channel.").queue();
      }
    } else { // User not in any voice channel
      ce.getChannel().sendMessage("User not in a voice channel.").queue();
    }
  }

  private void setPauseState(CommandEvent ce) {
    if (PlayerManager.getINSTANCE().getPlaybackManager(ce.getGuild()).audioPlayer.isPaused()) {
      PlayerManager.getINSTANCE().getPlaybackManager(ce.getGuild()).audioPlayer.setPaused(false);
      ce.getChannel().sendMessage("Audio player resumed.").queue();
    } else {
      PlayerManager.getINSTANCE().getPlaybackManager(ce.getGuild()).audioPlayer.setPaused(true);
      ce.getChannel().sendMessage("Audio player paused.").queue();
    }
  }
}
