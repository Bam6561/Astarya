package commands.audio;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import commands.audio.managers.AudioScheduler;
import commands.audio.managers.PlayerManager;
import commands.audio.utility.TimeConversion;
import commands.owner.Settings;

/**
 * NowPlaying is a command invocation that shows what track is currently playing.
 *
 * @author Danny Nguyen
 * @version 1.7.8
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
      String trackPosition = TimeConversion.convert(audioTrack.getPosition());
      String trackDuration = TimeConversion.convert(audioTrack.getDuration());

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
   * @param audioScheduler the bot's audio scheduler
   * @param audioPlayer    the bot's audio player
   * @param nowPlaying     String containing information about what track is currently playing
   */
  private void audioPlayerIsPausedOrLoopedNotice(AudioScheduler audioScheduler, AudioPlayer audioPlayer,
                                                 StringBuilder nowPlaying) {
    boolean audioPlayerIsPaused = audioPlayer.isPaused();
    boolean audioPlayerIsLooped = audioScheduler.getAudioPlayerLooped();

    if (audioPlayerIsPaused) {
      nowPlaying.append("(Paused) ");
    }
    if (audioPlayerIsLooped) {
      nowPlaying.append("(Loop) ");
    }
  }
}
