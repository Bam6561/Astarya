package me.dannynguyen.astarya.commands.audio.managers;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;

/**
 * PlaybackManager is a LavaPlayer component that associates all
 * LavaPlayer components together into one referencable instance object.
 *
 * @author Danny Nguyen
 * @version 1.5.4
 * @since 1.1.0s
 */

public class PlaybackManager {
  public final AudioPlayer audioPlayer;
  public final AudioScheduler audioScheduler;
  private static AudioPlayerSendHandler sendHandler;

  public PlaybackManager(AudioPlayerManager audioPlayerManager) {
    this.audioPlayer = audioPlayerManager.createPlayer();
    this.audioScheduler = new AudioScheduler(this.audioPlayer);
    this.audioPlayer.addListener(this.audioScheduler);
    sendHandler = new AudioPlayerSendHandler(this.audioPlayer);
  }

  public static AudioPlayerSendHandler getSendHandler() {
    return sendHandler;
  }
}
