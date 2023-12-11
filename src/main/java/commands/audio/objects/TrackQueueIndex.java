package commands.audio.objects;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

/**
 * TrackQueueIndex is an object relating an AudioTrack to its requester.
 *
 * @author Danny Nguyen
 * @version 1.7.0
 * @since 1.7.0
 */
public class TrackQueueIndex {
  private AudioTrack audioTrack;
  private String requester;

  public TrackQueueIndex(AudioTrack audioTrack, String requester) {
    this.audioTrack = audioTrack;
    this.requester = requester;
  }

  public AudioTrack getAudioTrack() {
    return this.audioTrack;
  }

  public String getRequester() {
    return this.requester;
  }

  private void setAudioTrack(AudioTrack audioTrack) {
    this.audioTrack = audioTrack;
  }

  private void setRequester(String requester) {
    this.requester = requester;
  }
}
