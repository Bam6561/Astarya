package commands.audio.managers;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;

public class PlaybackManager {
  public final AudioPlayer audioPlayer;
  public final AudioScheduler audioScheduler;
  private static AudioPlayerSendHandler sendHandler;

  public PlaybackManager(AudioPlayerManager manager) {
    this.audioPlayer = manager.createPlayer();
    this.audioScheduler = new AudioScheduler(this.audioPlayer);
    this.audioPlayer.addListener(this.audioScheduler);
    sendHandler = new AudioPlayerSendHandler(this.audioPlayer);
  }

  public static AudioPlayerSendHandler getSendHandler() {
    return sendHandler;
  }
}
