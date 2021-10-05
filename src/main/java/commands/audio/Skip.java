package commands.audio;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import commands.audio.managers.PlayerManager;
import commands.owner.Settings;
import net.dv8tion.jda.api.entities.GuildVoiceState;

public class Skip extends Command {
  public Skip() {
    this.name = "skip";
    this.aliases = new String[]{"skip", "s", "next", "ff"};
    this.arguments = "[0]skip";
    this.help = "Skips the current audio track in the player.";
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
          skipTrack(ce);
        } else { // User not in same voice channel as bot
          ce.getChannel().sendMessage("User not in the same voice channel.").queue();
        }
      } else { // Bot not in any voice channel
        ce.getChannel().sendMessage("Not in a voice channel.").queue();
      }
    } else { // User not in any voice channel
      ce.getChannel().sendMessage("User not in a voice channel.").queue();
    }
  }

  private void skipTrack(CommandEvent ce) {
    PlayerManager.getINSTANCE().getPlaybackManager((ce.getGuild())).audioScheduler.skipTrack();
    StringBuilder skipConfirmation = new StringBuilder();
    skipConfirmation.append("**Skip:** [").append(ce.getAuthor().getAsTag()).append("]");
    ce.getChannel().sendMessage(skipConfirmation).queue();
  }
}
