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
    this.aliases = new String[]{"join", "j"};
    this.help = "Bot joins the same voice channel as the user.";
  }

  // Connects bot to the same voice channel as the user
  @Override
  protected void execute(CommandEvent ce) {
    Settings.deleteInvoke(ce);

    GuildVoiceState userVoiceState = ce.getMember().getVoiceState();
    GuildVoiceState botVoiceState = ce.getGuild().getSelfMember().getVoiceState();

    boolean userInVoiceChannel = userVoiceState.inVoiceChannel();
    boolean botNotAlreadyInVoiceChannel = !botVoiceState.inVoiceChannel();
    boolean botIsAvailableToJoinSameVoiceChannel = userInVoiceChannel && botNotAlreadyInVoiceChannel;

    if (botIsAvailableToJoinSameVoiceChannel) {
      joinVoiceChannel(ce);
    } else {
      if (!userInVoiceChannel) {
        ce.getChannel().sendMessage("User not in a voice channel.").queue();
      } else if (!botNotAlreadyInVoiceChannel) {
        String alreadyConnected = "Already connected to <#" + botVoiceState.getChannel().getId() + ">";
        ce.getChannel().sendMessage(alreadyConnected).queue();
      }
    }
  }

  // Attempts to join the same voice channel as the user
  private void joinVoiceChannel(CommandEvent ce) {
    VoiceChannel voiceChannel = ce.getMember().getVoiceState().getChannel();
    AudioManager audioManager = ce.getGuild().getAudioManager();

    try {
      audioManager.openAudioConnection(voiceChannel);
      ce.getChannel().sendMessage("Connected to <#" + voiceChannel.getId() + ">").queue();
    } catch (Exception e) { // Insufficient permissions
      ce.getChannel().sendMessage("Unable to join <#" + voiceChannel.getId() + ">").queue();
    }
  }
}
