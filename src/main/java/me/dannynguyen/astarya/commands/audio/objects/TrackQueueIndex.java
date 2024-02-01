package me.dannynguyen.astarya.commands.audio.objects;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

/**
 * TrackQueueIndex is an object relating an AudioTrack to its requester.
 *
 * @author Danny Nguyen
 * @version 1.7.8
 * @since 1.7.0
 */
public record TrackQueueIndex(AudioTrack audioTrack, String requester) {

  public AudioTrack getAudioTrack() {
    return this.audioTrack;
  }

  public String getRequester() {
    return this.requester;
  }
}
