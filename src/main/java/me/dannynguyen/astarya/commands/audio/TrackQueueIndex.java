package me.dannynguyen.astarya.commands.audio;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

/**
 * Represents an AudioTrack with its requester.
 *
 * @param audioTrack audio track
 * @param requester  requesting user
 * @author Danny Nguyen
 * @version 1.7.8
 * @since 1.7.0
 */
public record TrackQueueIndex(AudioTrack audioTrack, String requester) {
  /**
   * Gets the audio track.
   *
   * @return audio track
   */
  public AudioTrack getAudioTrack() {
    return this.audioTrack;
  }

  /**
   * Gets the requesting user.
   *
   * @return requesting user
   */
  public String getRequester() {
    return this.requester;
  }
}
