package commands.audio;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import commands.audio.managers.PlayerManager;
import commands.owner.Settings;

public class NowPlaying extends Command {
  public NowPlaying() {
    this.name = "nowplaying";
    this.aliases = new String[]{"nowplaying", "np", "now"};
    this.arguments = "[0]NowPlaying";
    this.help = "Shows the user what's currently playing in the player.";
  }

  @Override
  protected void execute(CommandEvent ce) {
    Settings.deleteInvoke(ce);
    StringBuilder nowPlaying = new StringBuilder();
    AudioPlayer audioPlayer = PlayerManager.getINSTANCE().getPlaybackManager(ce.getGuild()).audioPlayer;
    if (audioPlayer.getPlayingTrack() == null) {
      nowPlaying.append("**Now Playing:** `Nothing`");
    } else {
      AudioTrack audioTrack = audioPlayer.getPlayingTrack();
      long trackPositionLong = audioTrack.getPosition();
      long trackDurationLong = audioTrack.getDuration();
      String trackPosition = floatTimeConversion(trackPositionLong);
      String trackDuration = floatTimeConversion(trackDurationLong);
      nowPlaying.append("**Now Playing:** `").append(audioTrack.getInfo().title).
          append("` {*").append(trackPosition).append("*-*").append(trackDuration).append("*}");
    }
    ce.getChannel().sendMessage(nowPlaying).queue();
  }

  private String floatTimeConversion(long floatTime) {
    long days = floatTime / 86400000 % 30;
    long hours = floatTime / 3600000 % 24;
    long minutes = floatTime / 60000 % 60;
    long seconds = floatTime / 1000 % 60;
    return (days == 0 ? "" : days < 10 ? "0" + days + ":" : days + ":") +
        (hours == 0 ? "" : hours < 10 ? "0" + hours + ":" : hours + ":") +
        (minutes == 0 ? "00:" : minutes < 10 ? "0" + minutes + ":" : minutes + ":") +
        (seconds == 0 ? "00" : seconds < 10 ? "0" + seconds : seconds + "");
  }
}
