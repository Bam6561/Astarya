package commands.audio;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import commands.audio.managers.PlayerManager;
import commands.owner.Settings;
import net.dv8tion.jda.api.entities.GuildVoiceState;

public class Remove extends Command {
  public Remove() {
    this.name = "remove";
    this.aliases = new String[]{"remove", "rm", "r"};
    this.arguments = "[1]queueNumber";
    this.help = "Removes an audio track from the queue.";
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
          removeTrack(ce);
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

  private void removeTrack(CommandEvent ce) {
    String[] args = ce.getMessage().getContentRaw().split("\\s"); // Parse message for arguments
    int arguments = args.length;
    if (arguments == 2) {
      try { // Remove queue entry
        PlayerManager.getINSTANCE().getPlaybackManager(ce.getGuild()).
            audioScheduler.removeQueueEntry(ce, Integer.parseInt(args[1]));
      } catch (NumberFormatException e) { //
        ce.getChannel().sendMessage("Argument must be an integer.").queue();
      }
    } else {// Invalid argument
      ce.getChannel().sendMessage("Invalid number of arguments.").queue();
    }
  }
}
