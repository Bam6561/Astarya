package commands.audio;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import commands.audio.managers.AudioScheduler;
import commands.audio.managers.PlayerManager;
import commands.owner.Settings;

public class NowPlaying extends Command {
  public NowPlaying() {
    this.name = "nowplaying";
    this.aliases = new String[]{"nowplaying", "np"};
    this.help = "Shows what's currently playing.";
  }

  // Displays currently playing track
  @Override
  protected void execute(CommandEvent ce) {
    Settings.deleteInvoke(ce);

    getNowPlaying(ce);
  }

  // Displays currently playing track
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

  // Conditional setting notices for nowPlaying
  private void audioPlayerIsPausedOrLoopedNotice(AudioScheduler audioScheduler, AudioPlayer audioPlayer,
                                                 StringBuilder nowPlayingConfirmation) {
    boolean audioPlayerIsPaused = audioPlayer.isPaused();
    boolean audioPlayerIsLooped = audioScheduler.getAudioPlayerLoopState();

    if (audioPlayerIsPaused) {
      nowPlayingConfirmation.append("(Paused) ");
    }
    if (audioPlayerIsLooped) {
      nowPlayingConfirmation.append("(Loop) ");
    }
  }

  // Converts long duration to conventional readable time
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
