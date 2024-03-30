package me.dannynguyen.astarya.commands.audio;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import me.dannynguyen.astarya.commands.owner.Settings;
import me.dannynguyen.astarya.enums.BotMessage;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;

/**
 * Command invocation that makes the bot join the same voice channel as the user.
 *
 * @author Danny Nguyen
 * @version 1.8.9
 * @since 1.1.0
 */
public class Join extends Command {
  /**
   * Associates the command with its properties.
   */
  public Join() {
    this.name = "join";
    this.aliases = new String[]{"join", "j"};
    this.help = "Joins the same voice channel as the user.";
  }

  /**
   * Checks if the bot is available to join the same voice channel as the user.
   *
   * @param ce command event
   */
  @Override
  protected void execute(CommandEvent ce) {
    Settings.deleteInvoke(ce);

    GuildVoiceState botVoiceState = ce.getGuild().getSelfMember().getVoiceState();

    if (!ce.getMember().getVoiceState().inAudioChannel()) {
      ce.getChannel().sendMessage(BotMessage.USER_NOT_IN_VC.getMessage()).queue();
      return;
    }
    if (botVoiceState.inAudioChannel()) {
      String alreadyConnected = "Already connected to <#" + botVoiceState.getChannel().getId() + ">";
      ce.getChannel().sendMessage(alreadyConnected).queue();
      return;
    }

    joinVoiceChannel(ce);
  }

  /**
   * Attempts to connect the bot to the same voice channel as the user.
   *
   * @param ce command event
   */
  private void joinVoiceChannel(CommandEvent ce) {
    AudioChannel audioChannel = ce.getMember().getVoiceState().getChannel();
    try {
      ce.getGuild().getAudioManager().openAudioConnection(audioChannel);
      ce.getChannel().sendMessage("Connected to <#" + audioChannel.getId() + ">").queue();
    } catch (InsufficientPermissionException ex) {
      ce.getChannel().sendMessage("Unable to join <#" + audioChannel.getId() + ">").queue();
    }
  }
}
