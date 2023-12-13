package commands.audio.managers;

import astarya.Astarya;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import commands.audio.objects.TrackQueueIndex;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;

import java.util.ArrayList;

/**
 * AudioScheduler is a component of LavaPlayer that handles the audio
 * player's functionality related to playing tracks and track order.
 *
 * @author Danny Nguyen
 * @version 1.7.0
 * @since 1.1.0
 */

public class AudioScheduler extends AudioEventAdapter {
  private final AudioPlayer audioPlayer;
  private ArrayList<TrackQueueIndex> trackQueue;
  private ArrayList<TrackQueueIndex> skippedTracksStack;
  private Boolean audioPlayerLooped = false;

  public AudioScheduler(AudioPlayer audioPlayer) {
    this.audioPlayer = audioPlayer;
    this.trackQueue = new ArrayList<>();
    this.skippedTracksStack = new ArrayList<>();
  }

  /**
   * Adds a track to the track queue, and if the audio player isn't
   * currently playing anything, then play the track immediately.
   *
   * @param track track to be added to the track queue
   */
  public void queue(AudioTrack track, String requester) {
    if (this.audioPlayer.getPlayingTrack() == null) {
      this.audioPlayer.startTrack(track, true);
    } else {
      this.trackQueue.add(new TrackQueueIndex(track, requester));
    }
  }

  /**
   * Goes to the next track in the track queue and removes the associated track requester. If
   * the audio player has finished its track queue, then update the bot's presence and activity.
   */
  public void nextTrack() {
    if (!this.trackQueue.isEmpty()) {
      this.audioPlayer.startTrack(this.trackQueue.get(0).getAudioTrack(), false);
      this.trackQueue.remove(0);
    } else if (this.audioPlayerLooped) {
    } else { // Update presence when not playing audio
      this.audioPlayer.stopTrack();
      Astarya Astarya = new Astarya();
      Astarya.getApi().getPresence().setStatus(OnlineStatus.DO_NOT_DISTURB);
      Astarya.getApi().getPresence().setActivity(Activity.listening("Nothing"));
    }
  }

  /**
   * Updates the bot's presence when playing a new track if the audio player isn't looped.
   *
   * @param audioPlayer           bot's audio player
   * @param currentlyPlayingTrack track that is currently playing
   */
  @Override
  public void onTrackStart(AudioPlayer audioPlayer, AudioTrack currentlyPlayingTrack) {
    if (!this.audioPlayerLooped) {
      Astarya Astarya = new Astarya();
      Astarya.getApi().getPresence().setActivity(Activity.listening(currentlyPlayingTrack.getInfo().title));
      Astarya.getApi().getPresence().setStatus(OnlineStatus.ONLINE);
    }
  }

  /**
   * Queues a copy of the currently playing track if the audio player is looped.
   *
   * @param audioPlayer bot's audio player
   * @param loopedTrack track that is currently looped
   * @param endReason   whether the audio player can continue playing the next track
   */
  @Override
  public void onTrackEnd(AudioPlayer audioPlayer, AudioTrack loopedTrack, AudioTrackEndReason endReason) {
    if (endReason.mayStartNext) {
      if (this.audioPlayerLooped) {
        this.audioPlayer.startTrack(loopedTrack.makeClone(), false);
      }
      nextTrack();
    }
  }

  /**
   * Adds a recently skipped track to the skipped track stack.
   * <p>
   * Recently skipped tracks go to the top and increment all existing track
   * indices by 1. The maximum number of skipped tracks is the stack is 10,
   * after which adding a new skipped track removes the least recent in the stack.
   * </p>
   *
   * @param skippedTrack track that was recently skipped
   */
  public void addToSkippedTracksStack(TrackQueueIndex skippedTrack) {
    this.skippedTracksStack.add(skippedTrack);
    boolean skippedTracksStackOverLimit = skippedTracksStack.size() > 10;
    if (skippedTracksStackOverLimit) {
      skippedTracksStack.remove(9);
    }
  }

  public AudioPlayer getAudioPlayer() {
    return this.audioPlayer;
  }

  public ArrayList<TrackQueueIndex> getTrackQueue() {
    return this.trackQueue;
  }

  public ArrayList<TrackQueueIndex> getSkippedTracksStack() {
    return this.skippedTracksStack;
  }

  public boolean getAudioPlayerLooped() {
    return this.audioPlayerLooped;
  }

  public void setAudioPlayerLooped(boolean audioPlayerLooped) {
    this.audioPlayerLooped = audioPlayerLooped;
  }
}
