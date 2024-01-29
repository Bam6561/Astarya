package commands.audio;

import astarya.BotMessage;
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
 * @version 1.7.12
 * @since 1.1.0
 */
public class Join extends Command {
  public Join() {
    this.name = "join";
    this.aliases = new String[]{"join", "j"};
    this.help = "Joins the same voice channel as the user.";
  }

  /**
   * Checks if the bot is available to join the same voice channel as the user by checking
   * that the user is in a voice channel and that the bot is not already in a voice channel.
   *
   * @param ce command event
   */
  @Override
  protected void execute(CommandEvent ce) {
    Settings.deleteInvoke(ce);

    GuildVoiceState userVoiceState = ce.getMember().getVoiceState();
    GuildVoiceState botVoiceState = ce.getGuild().getSelfMember().getVoiceState();

    boolean userInVoiceChannel = userVoiceState.inAudioChannel();
    boolean botAlreadyInVoiceChannel = botVoiceState.inAudioChannel();
    boolean botIsAvailableToJoinSameVoiceChannel = userInVoiceChannel && !botAlreadyInVoiceChannel;

    if (botIsAvailableToJoinSameVoiceChannel) {
      joinVoiceChannel(ce);
    } else {
      if (!userInVoiceChannel) {
        ce.getChannel().sendMessage(BotMessage.Failure.USER_NOT_IN_VC.text).queue();
      } else if (botAlreadyInVoiceChannel) {
        String alreadyConnected = "Already connected to <#" + botVoiceState.getChannel().getId() + ">";
        ce.getChannel().sendMessage(alreadyConnected).queue();
      }
    }
  }

  /**
   * Attempts to connect the bot to the same voice channel as the user.
   *
   * @param ce command event
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
