package commands.audio;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import commands.audio.managers.PlayerManager;
import commands.owner.Settings;
import net.dv8tion.jda.api.entities.GuildVoiceState;

public class SetPosition extends Command {
  public SetPosition() {
    this.name = "setPosition";
    this.aliases = new String[]{"setposition", "setpos", "goto", "songtime"};
    this.arguments = "[1]timeString";
    this.help = "Sets the position of the currently playing track.";
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
          setPosition(ce);
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

  private void setPosition(CommandEvent ce) {
    String[] args = ce.getMessage().getContentRaw().split("\\s"); // Parse message for arguments
    int arguments = args.length;
    switch (arguments) {
      case 1 -> {
        ce.getChannel().sendMessage("Invalid number of arguments.").queue();
      }
      case 2 -> {
        try {
          PlayerManager.getINSTANCE().getPlaybackManager(ce.getGuild()).audioScheduler.setPosition(ce, args);
        } catch (NumberFormatException error) {
          ce.getChannel().sendMessage("Argument is invalid.").queue();
        }
      }
      default -> {
        ce.getChannel().sendMessage("Invalid number of arguments.").queue();
      }
    }
  }
}