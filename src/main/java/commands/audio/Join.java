package commands.audio;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import commands.owner.Settings;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.managers.AudioManager;

/**
 * Join is a command invocation that makes the bot join the same voice channel as the user.
 *
 * @author Danny Nguyen
 * @version 1.6
 * @since 1.1.0
 */
public class Join extends Command {
  public Join() {
    this.name = "join";
    this.aliases = new String[]{"join", "j"};
    this.help = "Joins the same voice channel as the user.";
  }

  /**
   * Determines whether the bot is available to join the same voice channel as the user by checking
   * that the user is in a voice channel and that the bot is not already in a voice channel.
   *
   * @param ce object that contains information about the command event
   */
  @Override
  protected void execute(CommandEvent ce) {
    Settings.deleteInvoke(ce);

    GuildVoiceState userVoiceState = ce.getMember().getVoiceState();
    GuildVoiceState botVoiceState = ce.getGuild().getSelfMember().getVoiceState();

    boolean userInVoiceChannel = userVoiceState.inAudioChannel();
    boolean botNotAlreadyInVoiceChannel = !botVoiceState.inAudioChannel();
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

  /**
   * Attempts to connect the bot to the same voice channel as the user.
   *
   * @param ce object that contains information about the command event
   */
  private void joinVoiceChannel(CommandEvent ce) {
    AudioChannel audioChannel = ce.getMember().getVoiceState().getChannel();
    AudioManager audioManager = ce.getGuild().getAudioManager();

    try {
      audioManager.openAudioConnection(audioChannel);
      ce.getChannel().sendMessage("Connected to <#" + audioChannel.getId() + ">").queue();
    } catch (Exception e) { // Insufficient permissions
      ce.getChannel().sendMessage("Unable to join <#" + audioChannel.getId() + ">").queue();
    }
  }
}
