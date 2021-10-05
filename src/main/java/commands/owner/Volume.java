package commands.owner;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import commands.audio.managers.PlayerManager;
import net.dv8tion.jda.api.entities.GuildVoiceState;

public class Volume extends Command {
  public Volume() {
    this.name = "volume";
    this.aliases = new String[]{"volume"};
    this.arguments = "[0]volume [1]0-100";
    this.help = "Sets the volume of the audio player.";
    this.ownerCommand = true;
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
          setVolume(ce);
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

  private void setVolume(CommandEvent ce) {
    String[] args = ce.getMessage().getContentRaw().split("\\s"); // Parse message for arguments
    int arguments = args.length;
    if (arguments == 2) {
      try {
        int volume = Integer.parseInt(args[1]);
        if (volume >= 0 && volume <= 200) {
          PlayerManager.getINSTANCE().getPlaybackManager(ce.getGuild()).audioPlayer.setVolume(volume);
          String volumeConfirmation = "Audio player volume set to `" + volume + "`%.";
          ce.getChannel().sendMessage(volumeConfirmation).queue();
        } else {
          ce.getChannel().sendMessage("Argument must be an integer from 0 - 200.");
        }
      } catch (NumberFormatException error) {
        ce.getChannel().sendMessage("Argument must be an integer from 0 - 200.");
      }
    } else {
      ce.getChannel().sendMessage("Invalid number of arguments.");
    }
  }
}