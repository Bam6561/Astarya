package me.bam6561.astarya.commands.audio;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import me.bam6561.astarya.Bot;
import me.bam6561.astarya.commands.audio.managers.PlayerManager;
import me.bam6561.astarya.commands.owner.Settings;
import me.bam6561.astarya.enums.BotMessage;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.managers.Presence;

/**
 * Command invocation that pauses the audio player.
 *
 * @author Danny Nguyen
 * @version 1.8.10
 * @since 1.2.5
 */
public class Pause extends Command {
  /**
   * Associates the command with its properties.
   */
  public Pause() {
    this.name = "pause";
    this.aliases = new String[]{"pause", "stop"};
    this.help = "Pauses the audio player.";
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
      setAudioPlayerPause(ce);
    } else {
      ce.getChannel().sendMessage(BotMessage.USER_NOT_IN_SAME_VC.getMessage()).queue();
    }
  }

  /**
   * Pauses the audio player.
   * <p>
   * Sets the bot's presence according to if it's paused, playing music, or not playing anything.
   *
   * @param ce command event
   */
  private void setAudioPlayerPause(CommandEvent ce) {
    AudioPlayer audioPlayer = PlayerManager.getINSTANCE().getPlaybackManager(ce.getGuild()).audioScheduler.getAudioPlayer();
    Presence presence = Bot.getApi().getPresence();

    if (!audioPlayer.isPaused()) { // Paused - Idle Yellow
      audioPlayer.setPaused(true);
      presence.setActivity(Activity.listening("Paused"));
      presence.setStatus(OnlineStatus.IDLE);
      ce.getChannel().sendMessage("Audio player paused.").queue();
    } else { // Playing Music - Online Green || Not playing audio - Do Not Disturb Red
      audioPlayer.setPaused(false);
      try {
        presence.setActivity(Activity.listening(audioPlayer.getPlayingTrack().getInfo().title));
        presence.setStatus(OnlineStatus.ONLINE);
      } catch (NullPointerException e) { // No track currently playing
        presence.setActivity(Activity.listening("Nothing"));
        presence.setStatus(OnlineStatus.DO_NOT_DISTURB);
      }
      ce.getChannel().sendMessage("Audio player resumed.").queue();
    }
  }
}
