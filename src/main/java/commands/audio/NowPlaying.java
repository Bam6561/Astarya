package commands.audio;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import commands.audio.managers.AudioScheduler;
import commands.audio.managers.PlayerManager;
import commands.owner.Settings;

/**
 * NowPlaying is a command invocation that shows what track is currently playing.
 *
 * @author Danny Nguyen
 * @version 1.7.0
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
   * @param ce object containing information about the command event
   */
  @Override
  protected void execute(CommandEvent ce) {
    Settings.deleteInvoke(ce);

    getNowPlaying(ce);
  }

  /**
   * Displays the currently playing track.
   *
   * @param ce object containing information about the command event
   */
  private void getNowPlaying(CommandEvent ce) { // NowPlaying
    AudioScheduler audioScheduler = PlayerManager.getINSTANCE().getPlaybackManager(ce.getGuild()).audioScheduler;
    AudioPlayer audioPlayer = audioScheduler.getAudioPlayer();

    StringBuilder nowPlayingConfirmation = new StringBuilder();

    boolean currentlyPlayingTrack = !(audioPlayer.getPlayingTrack() == null);
    if (currentlyPlayingTrack) {
      AudioTrack audioTrack = audioPlayer.getPlayingTrack();
      String trackPosition = longTimeConversion(audioTrack.getPosition());
      String trackDuration = longTimeConversion(audioTrack.getDuration());

      // nowPlaying Confirmation
      nowPlayingConfirmation.append("**Now Playing:** ");
      audioPlayerIsPausedOrLoopedNotice(audioScheduler, audioPlayer, nowPlayingConfirmation);
      nowPlayingConfirmation.append("`").append(audioTrack.getInfo().title).
          append("` {*").append(trackPosition).append("*-*").append(trackDuration).append("*}");
    } else {
      nowPlayingConfirmation.append("**Now Playing:** `Nothing`");
    }
    ce.getChannel().sendMessage(nowPlayingConfirmation).queue();
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

  /**
   * Converts long duration to conventional readable time.
   *
   * @param longTime duration of the track in long
   * @return readable time format
   */
  private String longTimeConversion(long longTime) {
    long days = longTime / 86400000 % 30;
    long hours = longTime / 3600000 % 24;
    long minutes = longTime / 60000 % 60;
    long seconds = longTime / 1000 % 60;
    return (days == 0 ? "" : days < 10 ? "0" + days + ":" : days + ":") +
        (hours == 0 ? "" : hours < 10 ? "0" + hours + ":" : hours + ":") +
        (minutes == 0 ? "00:" : minutes < 10 ? "0" + minutes + ":" : minutes + ":") +
        (seconds == 0 ? "00" : seconds < 10 ? "0" + seconds : seconds + "");
  }
}
