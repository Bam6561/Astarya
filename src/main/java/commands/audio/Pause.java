package commands.audio;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import commands.audio.managers.AudioScheduler;
import commands.audio.managers.PlayerManager;
import commands.owner.Settings;
import astarya.Astarya;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.GuildVoiceState;

/**
 * Pause is a command invocation that pauses the audio player.
 *
 * @author Danny Nguyen
 * @version 1.6.1
 * @since 1.2.5
 */
public class Pause extends Command {
  public Pause() {
    this.name = "pause";
    this.aliases = new String[]{"pause", "stop"};
    this.help = "Pauses the audio player.";
  }

  /**
   * Determines whether the user is in the same voice channel as the bot to process a pause command request.
   *
   * @param ce object containing information about the command event
   */
  @Override
  protected void execute(CommandEvent ce) {
    Settings.deleteInvoke(ce);

    GuildVoiceState userVoiceState = ce.getMember().getVoiceState();
    GuildVoiceState botVoiceState = ce.getGuild().getSelfMember().getVoiceState();

    try {
      boolean userInSameVoiceChannel = userVoiceState.getChannel().equals(botVoiceState.getChannel());
      if (userInSameVoiceChannel) {
        setAudioPlayerPause(ce);
      } else {
        ce.getChannel().sendMessage("User not in the same voice channel.").queue();
      }
    } catch (NullPointerException e) {
      ce.getChannel().sendMessage("User not in a voice channel.").queue();
    }
  }

  /**
   * Pauses the audio player, and sets the bot's presence according
   * to whether it's paused, playing music, or not playing anything.
   *
   * @param ce object containing information about the command event
   */
  private void setAudioPlayerPause(CommandEvent ce) {
    AudioScheduler audioScheduler = PlayerManager.getINSTANCE().getPlaybackManager(ce.getGuild()).audioScheduler;
    AudioPlayer audioPlayer = audioScheduler.getAudioPlayer();
    Astarya Astarya = new Astarya();

    boolean audioPlayerNotPaused = !audioPlayer.isPaused();
    if (audioPlayerNotPaused) { // Update presence when Paused - Idle Yellow
      audioPlayer.setPaused(true);
      Astarya.getApi().getPresence().setActivity(Activity.listening("Paused"));
      Astarya.getApi().getPresence().setStatus(OnlineStatus.IDLE);
      ce.getChannel().sendMessage("Audio player paused.").queue();
    } else { // Update presence when Playing Music - Online Green || Not playing audio - Do Not Disturb Red
      audioPlayer.setPaused(false);
      try {
        Astarya.getApi().getPresence().setActivity(Activity.listening(audioPlayer.getPlayingTrack().getInfo().title));
        Astarya.getApi().getPresence().setStatus(OnlineStatus.ONLINE);
      } catch (NullPointerException e) { // No track currently playing
        Astarya.getApi().getPresence().setActivity(Activity.listening("Nothing"));
        Astarya.getApi().getPresence().setStatus(OnlineStatus.DO_NOT_DISTURB);
      }
      ce.getChannel().sendMessage("Audio player resumed.").queue();
    }
  }
}
