package commands.audio.managers;

import astarya.Astarya;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import commands.audio.objects.TrackQueueIndex;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.managers.Presence;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * AudioScheduler is a component of LavaPlayer that handles the audio
 * player's functionality related to playing tracks and track order.
 *
 * @author Danny Nguyen
 * @version 1.7.16
 * @since 1.1.0
 */

public class AudioScheduler extends AudioEventAdapter {
  private final AudioPlayer audioPlayer;
  private final List<TrackQueueIndex> trackQueue;
  private final LinkedList<TrackQueueIndex> skippedTracks;
  private boolean audioPlayerLooped = false;

  public AudioScheduler(AudioPlayer audioPlayer) {
    this.audioPlayer = audioPlayer;
    this.trackQueue = new ArrayList<>();
    this.skippedTracks = new LinkedList<>();
  }

  /**
   * Updates the bot's presence when playing a new track if the audio player isn't looped.
   *
   * @param audioPlayer      audio player
   * @param currentlyPlaying currently playing track
   */
  @Override
  public void onTrackStart(AudioPlayer audioPlayer, AudioTrack currentlyPlaying) {
    if (!audioPlayerLooped) {
      Presence presence = Astarya.getApi().getPresence();
      presence.setActivity(Activity.listening(currentlyPlaying.getInfo().title));
      presence.setStatus(OnlineStatus.ONLINE);
    }
  }

  /**
   * Queues a copy of the currently playing track if the audio player is looped.
   *
   * @param audioPlayer audio player
   * @param loopedTrack currently looped track
   * @param endReason   whether the audio player can continue playing the next track
   */
  @Override
  public void onTrackEnd(AudioPlayer audioPlayer, AudioTrack loopedTrack, AudioTrackEndReason endReason) {
    if (endReason.mayStartNext) {
      if (audioPlayerLooped) {
        audioPlayer.startTrack(loopedTrack.makeClone(), false);
      }
      nextTrack();
    }
  }

  /**
   * Adds a track to the queue, and if the audio player isn't
   * currently playing anything, then play the track immediately.
   *
   * @param track track to be added to the queue
   */
  public void queue(AudioTrack track, String requester) {
    if (audioPlayer.getPlayingTrack() == null) {
      audioPlayer.startTrack(track, true);
    } else {
      trackQueue.add(new TrackQueueIndex(track, requester));
    }
  }

  /**
   * Goes to the next track in the queue. If the audio player has
   * finished its queue, then update the bot's presence and activity.
   */
  public void nextTrack() {
    if (!trackQueue.isEmpty()) {
      audioPlayer.startTrack(trackQueue.get(0).getAudioTrack(), false);
      trackQueue.remove(0);
    } else if (audioPlayerLooped) {
    } else { // Update presence when not playing audio
      audioPlayer.stopTrack();

      Presence presence = Astarya.getApi().getPresence();
      presence.setStatus(OnlineStatus.DO_NOT_DISTURB);
      presence.setActivity(Activity.listening("Nothing"));
    }
  }

  /**
   * Adds the recently skipped track.
   * <p>
   * Recently skipped tracks go to the top and increment all existing track
   * indices by 1. The maximum number of skipped tracks is 10,
   * after which adding a new skipped track removes the least recent.
   * </p>
   *
   * @param skippedTrack recently skipped track
   */
  public void addToSkippedTracks(TrackQueueIndex skippedTrack) {
    skippedTracks.addFirst(skippedTrack);
    if (skippedTracks.size() > 10) {
      skippedTracks.removeLast();
    }
  }

  public AudioPlayer getAudioPlayer() {
    return this.audioPlayer;
  }

  public List<TrackQueueIndex> getTrackQueue() {
    return this.trackQueue;
  }

  public LinkedList<TrackQueueIndex> getSkippedTracks() {
    return this.skippedTracks;
  }

  public boolean getAudioPlayerLooped() {
    return this.audioPlayerLooped;
  }

  public void setAudioPlayerLooped(boolean audioPlayerLooped) {
    this.audioPlayerLooped = audioPlayerLooped;
  }
}
