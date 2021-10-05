package commands.audio;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import commands.audio.managers.PlayerManager;
import commands.owner.Settings;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.managers.AudioManager;

public class Play extends Command {

  public Play() {
    this.name = "play";
    this.aliases = new String[]{"play", "p", "add"};
    this.help = "Adds an audio track to the queue.";
    this.arguments = ("[1]URL [2++]YouTubeQuery");
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
          playRequest(ce);
        } else { // User not in same voice channel as bot
          String alreadyConnected = "User not in the same voice channel.";
          ce.getChannel().sendMessage(alreadyConnected).queue();
        }
      } else { // Bot not in any voice channel
        joinVoiceChannel(ce);
        playRequest(ce);
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

  private void playRequest(CommandEvent ce) {
    String[] args = ce.getMessage().getContentRaw().split("\\s"); // Parse message for arguments
    int arguments = args.length;
    switch (arguments) {
      case 1 -> ce.getChannel().sendMessage("Invalid number of arguments.").queue(); // Invalid argument
      case 2 -> PlayerManager.getINSTANCE().createAudioTrack(ce, args[1]); // Track or playlist
      default -> { // Search query
        StringBuilder searchQuery = new StringBuilder();
        for (int i = 1; i < arguments; i++) {
          searchQuery.append(args[i]);
        }
        String youtubeSearchQuery = "ytsearch:" + String.join(" ", searchQuery);
        PlayerManager.getINSTANCE().createAudioTrack(ce, youtubeSearchQuery);
      }
    }
  }
}