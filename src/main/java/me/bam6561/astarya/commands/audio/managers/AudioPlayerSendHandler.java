package me.bam6561.astarya.commands.audio.managers;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.playback.MutableAudioFrame;
import net.dv8tion.jda.api.audio.AudioSendHandler;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.util.Objects;

/**
 * Represents the component of LavaPlayer that handles the
 * bot's ability to play tracks in connected voice channels.
 *
 * @author Danny Nguyen
 * @version 1.9.3
 * @since 1.1.0
 */
public class AudioPlayerSendHandler implements AudioSendHandler {
  /**
   * Audio player.
   */
  private final AudioPlayer audioPlayer;

  /**
   * Byte buffer.
   */
  private final ByteBuffer buffer = ByteBuffer.allocate(512); // Allocated memory per 20ms

  /**
   * Audio frame.
   */
  private final MutableAudioFrame frame = new MutableAudioFrame();

  /**
   * Associates the audio player with its audio player, buffer, and frame.
   *
   * @param audioPlayer audio player
   */
  public AudioPlayerSendHandler(@NotNull AudioPlayer audioPlayer) {
    this.audioPlayer = Objects.requireNonNull(audioPlayer, "Null audio player");
    this.frame.setBuffer(buffer);
  }

  /**
   * Gets if the audio player can play an audio frame.
   *
   * @return if the audio player can play an audio frame
   */
  @Override
  public boolean canProvide() {
    return audioPlayer.provide(this.frame);
  }

  /**
   * Gets the bytes representing the audio.
   *
   * @return bytes representing the audio
   */
  @NotNull
  @Override
  public ByteBuffer provide20MsAudio() {
    return this.buffer.flip();
  }

  /**
   * Gets if the audio being played is formatted in the Opus codec.
   *
   * @return if the audio bing played is formatted in Opus codec
   */
  @Override
  public boolean isOpus() {
    return true;
  }
}