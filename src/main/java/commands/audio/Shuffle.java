package commands.audio;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import commands.audio.managers.PlayerManager;
import commands.owner.Settings;
import net.dv8tion.jda.api.entities.GuildVoiceState;

public class Shuffle extends Command {
  public Shuffle() {
    this.name = "shuffle";
    this.aliases = new String[]{"shuffle", "mix"};
    this.arguments = "[0]shuffle";
    this.help = "Shuffles audio tracks in the player.";
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
          shuffleQueue(ce);
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

  private void shuffleQueue(CommandEvent ce) {
    PlayerManager.getINSTANCE().getPlaybackManager(ce.getGuild()).audioScheduler.shuffleQueue();
    StringBuilder shuffleConfirmation = new StringBuilder();
    shuffleConfirmation.append("**SHUFFLE:** [").append(ce.getAuthor().getAsTag()).append("]");
    ce.getChannel().sendMessage(shuffleConfirmation).queue();
  }
}
