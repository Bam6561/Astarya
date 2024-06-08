package me.bam6561.astarya.commands.audio;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an AudioTrack with its requester.
 *
 * @param audioTrack audio track
 * @param requester  requesting user
 * @author Danny Nguyen
 * @version 1.7.8
 * @since 1.7.0
 */
public record TrackQueueIndex(@NotNull AudioTrack audioTrack, @NotNull String requester) {
  /**
   * Gets the audio track.
   *
   * @return audio track
   */
  @NotNull
  public AudioTrack getAudioTrack() {
    return this.audioTrack;
  }

  /**
   * Gets the requesting user.
   *
   * @return requesting user
   */
  @NotNull
  public String getRequester() {
    return this.requester;
  }
}
