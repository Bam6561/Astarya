package me.dannynguyen.astarya.commands.audio.managers;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Represents a LavaPlayer component that associates all LavaPlayer
 * components together into one referencable instance object.
 *
 * @author Danny Nguyen
 * @version 1.8.7
 * @since 1.1.0s
 */
public class PlaybackManager {
  /**
   * Audio player.
   */
  public final AudioPlayer audioPlayer;

  /**
   * {@link AudioScheduler}
   */
  public final AudioScheduler audioScheduler;

  /**
   * {@link AudioPlayerSendHandler}
   */
  private static AudioPlayerSendHandler sendHandler;

  /**
   * Associates the audio player manager with its audio player,
   * {@link AudioScheduler}, and {@link AudioPlayerSendHandler}.
   *
   * @param audioPlayerManager audio player manager
   */
  public PlaybackManager(@NotNull AudioPlayerManager audioPlayerManager) {
    this.audioPlayer = Objects.requireNonNull(audioPlayerManager, "Null audio player manager").createPlayer();
    this.audioScheduler = new AudioScheduler(this.audioPlayer);
    this.audioPlayer.addListener(this.audioScheduler);
    sendHandler = new AudioPlayerSendHandler(this.audioPlayer);
  }

  /**
   * Gets the {@link AudioPlayerSendHandler}.
   *
   * @return {@link AudioPlayerSendHandler}
   */
  @NotNull
  public static AudioPlayerSendHandler getSendHandler() {
    return sendHandler;
  }
}
