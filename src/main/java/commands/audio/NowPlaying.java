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
      nowPlaying.append("**Now Playing:** `").append(audioTrack.getInfo().title).
          append("` ").append(audioTrack.getPosition()).append("/").append(audioTrack.getDuration());
    }
    ce.getChannel().sendMessage(nowPlaying).queue();
  }
}
