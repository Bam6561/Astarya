package me.bam6561.astarya.commands.audio;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import me.bam6561.astarya.commands.audio.managers.AudioScheduler;
import me.bam6561.astarya.commands.audio.managers.PlayerManager;
import me.bam6561.astarya.commands.owner.Settings;
import me.bam6561.astarya.enums.BotMessage;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;

/**
 * Command invocation that the state of looping the currently playing track.
 *
 * @author Danny Nguyen
 * @version 1.8.9
 * @since 1.2.6
 */
public class Loop extends Command {
  /**
   * Associates the command with its properties.
   */
  public Loop() {
    this.name = "loop";
    this.aliases = new String[]{"loop", "repeat"};
    this.help = "Loops the current track.";
  }

  /**
   * Checks if the user is in the same voice channel as the bot to read the command request.
   *
   * @param ce command event
   */
  @Override
  protected void execute(CommandEvent ce) {
    Settings.deleteInvoke(ce);

    AudioChannelUnion userChannel = ce.getMember().getVoiceState().getChannel();
    AudioChannelUnion botChannel = ce.getGuild().getSelfMember().getVoiceState().getChannel();

    if (userChannel == null) {
      ce.getChannel().sendMessage(BotMessage.USER_NOT_IN_VC.getMessage()).queue();
      return;
    }

    if (userChannel.equals(botChannel)) {
      setAudioPlayerLoop(ce);
    } else {
      ce.getChannel().sendMessage(BotMessage.USER_NOT_IN_SAME_VC.getMessage()).queue();
    }
  }

  /**
   * Sets the loop status of the audio player.
   *
   * @param ce command event
   */
  private void setAudioPlayerLoop(CommandEvent ce) {
    AudioScheduler audioScheduler = PlayerManager.getINSTANCE().getPlaybackManager(ce.getGuild()).audioScheduler;
    audioScheduler.toggleAudioPlayerLooped();

    StringBuilder loopConfirmation = new StringBuilder();
    if (audioScheduler.getAudioPlayerLooped()) {
      loopConfirmation.append("**Loop:** `ON` [").append(ce.getAuthor().getAsTag()).append("]");
    } else {
      loopConfirmation.append("**Loop:** `OFF` [").append(ce.getAuthor().getAsTag()).append("]");
    }
    ce.getChannel().sendMessage(loopConfirmation).queue();
  }
}
