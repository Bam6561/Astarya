package commands.audio;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import commands.audio.managers.PlayerManager;
import commands.owner.Settings;
import net.dv8tion.jda.api.entities.GuildVoiceState;

public class Swap extends Command {
  public Swap() {
    this.name = "switch";
    this.aliases = new String[]{"swap", "switch"};
    this.arguments = "[1]QueueNumber [2] QueueNumber";
    this.help = "Swaps the position of an audio track in queue with another.";
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
          swapTrack(ce);
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

  private void swapTrack(CommandEvent ce) {
    String[] args = ce.getMessage().getContentRaw().split("\\s"); // Parse message for arguments
    int arguments = args.length;
    if (arguments == 3) {
      try {
        PlayerManager.getINSTANCE().getPlaybackManager(ce.getGuild()).
            audioScheduler.swap(ce, Integer.parseInt(args[1]), Integer.parseInt(args[2]));
      } catch (NumberFormatException error) {
        ce.getChannel().sendMessage("Arguments must be integers.").queue();
      }
    } else { // Invalid arguments
      ce.getChannel().sendMessage("Invalid number of arguments.").queue();
    }
  }
}