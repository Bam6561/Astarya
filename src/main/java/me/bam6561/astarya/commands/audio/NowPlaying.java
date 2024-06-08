package me.bam6561.astarya.commands.audio;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import me.bam6561.astarya.commands.audio.managers.AudioScheduler;
import me.bam6561.astarya.commands.audio.managers.PlayerManager;
import me.bam6561.astarya.commands.owner.Settings;

/**
 * Command invocation that shows what track is currently playing.
 *
 * @author Danny Nguyen
 * @version 1.8.10
 * @since 1.2.3
 */
public class NowPlaying extends Command {
  /**
   * Associates the command with its properties.
   */
  public NowPlaying() {
    this.name = "nowplaying";
    this.aliases = new String[]{"nowplaying", "np"};
    this.help = "Shows what track is currently playing.";
  }

  /**
   * Ignores all parameters and sends an embed that displays the currently playing track.
   * <p>
   * The embed will also display if the audio player is paused or looped.
   *
   * @param ce command event
   */
  @Override
  protected void execute(CommandEvent ce) {
    Settings.deleteInvoke(ce);

    AudioScheduler audioScheduler = PlayerManager.getINSTANCE().getPlaybackManager(ce.getGuild()).audioScheduler;
    AudioPlayer audioPlayer = audioScheduler.getAudioPlayer();

    StringBuilder nowPlaying = new StringBuilder("**Now Playing:** ");
    if (audioPlayer.getPlayingTrack() != null) {
      AudioTrack audioTrack = audioPlayer.getPlayingTrack();
      String trackPosition = TrackTime.convertLong(audioTrack.getPosition());
      String trackDuration = TrackTime.convertLong(audioTrack.getDuration());

      if (audioPlayer.isPaused()) {
        nowPlaying.append("(Paused) ");
      }
      if (audioScheduler.getAudioPlayerLooped()) {
        nowPlaying.append("(Loop) ");
      }
      nowPlaying.append("`").append(audioTrack.getInfo().title).append("` {*").append(trackPosition).append("*-*").append(trackDuration).append("*}");
    } else {
      nowPlaying.append("`Nothing`");
    }
    ce.getChannel().sendMessage(nowPlaying).queue();
  }
}
