package me.dannynguyen.astarya.commands.audio;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import me.dannynguyen.astarya.commands.audio.managers.AudioScheduler;
import me.dannynguyen.astarya.commands.audio.managers.PlayerManager;
import me.dannynguyen.astarya.commands.owner.Settings;

/**
 * NowPlaying is a command invocation that shows what track is currently playing.
 *
 * @author Danny Nguyen
 * @version 1.7.16
 * @since 1.2.3
 */
public class NowPlaying extends Command {
  public NowPlaying() {
    this.name = "nowplaying";
    this.aliases = new String[]{"nowplaying", "np"};
    this.help = "Shows what track is currently playing.";
  }

  /**
   * Ignores all parameters and displays the currently playing track.
   *
   * @param ce command event
   */
  @Override
  protected void execute(CommandEvent ce) {
    Settings.deleteInvoke(ce);

    getNowPlaying(ce);
  }

  /**
   * Displays the currently playing track.
   *
   * @param ce command event
   */
  private void getNowPlaying(CommandEvent ce) { // NowPlaying
    AudioScheduler audioScheduler = PlayerManager.getINSTANCE().getPlaybackManager(ce.getGuild()).audioScheduler;
    AudioPlayer audioPlayer = audioScheduler.getAudioPlayer();

    StringBuilder nowPlaying = new StringBuilder("**Now Playing:** ");

    boolean currentlyPlayingTrack = !(audioPlayer.getPlayingTrack() == null);
    if (currentlyPlayingTrack) {
      AudioTrack audioTrack = audioPlayer.getPlayingTrack();
      String trackPosition = TrackTime.convertLong(audioTrack.getPosition());
      String trackDuration = TrackTime.convertLong(audioTrack.getDuration());

      audioPlayerIsPausedOrLoopedNotice(audioScheduler, audioPlayer, nowPlaying);
      nowPlaying.append("`").append(audioTrack.getInfo().title).
          append("` {*").append(trackPosition).append("*-*").append(trackDuration).append("*}");
    } else {
      nowPlaying.append("`Nothing`");
    }
    ce.getChannel().sendMessage(nowPlaying).queue();
  }

  /**
   * Adds conditional setting notes for the nowPlaying section.
   *
   * @param audioScheduler audio scheduler
   * @param audioPlayer    audio player
   * @param nowPlaying     information about what track is currently playing
   */
  private void audioPlayerIsPausedOrLoopedNotice(AudioScheduler audioScheduler, AudioPlayer audioPlayer,
                                                 StringBuilder nowPlaying) {
    if (audioPlayer.isPaused()) {
      nowPlaying.append("(Paused) ");
    }
    if (audioScheduler.getAudioPlayerLooped()) {
      nowPlaying.append("(Loop) ");
    }
  }
}
