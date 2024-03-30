package me.dannynguyen.astarya.commands.audio.managers;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import me.dannynguyen.astarya.Bot;
import me.dannynguyen.astarya.commands.audio.objects.TrackQueueIndex;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.managers.Presence;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Represents the component of LavaPlayer that handles the audio
 * player's functionality related to playing tracks and track order.
 *
 * @author Danny Nguyen
 * @version 1.8.7
 * @since 1.1.0
 */
public class AudioScheduler extends AudioEventAdapter {
  /**
   * Audio player.
   */
  private final AudioPlayer audioPlayer;

  /**
   * Track queue.
   */
  private final List<TrackQueueIndex> trackQueue;

  /**
   * Skipped tracks.
   */
  private final LinkedList<TrackQueueIndex> skippedTracks;

  /**
   * If the audio player is looped.
   */
  private boolean audioPlayerLooped = false;

  /**
   * Associates an audio player with its audioplayer, track queue and skipped tracks.
   *
   * @param audioPlayer audio player
   */
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
      Presence presence = Bot.getApi().getPresence();
      presence.setActivity(Activity.listening(currentlyPlaying.getInfo().title));
      presence.setStatus(OnlineStatus.ONLINE);
    }
  }

  /**
   * Queues a copy of the currently playing track if the audio player is looped.
   *
   * @param audioPlayer audio player
   * @param loopedTrack currently looped track
   * @param endReason   if the audio player can continue playing the next track
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
   * Adds a track to the queue.
   * <p>
   * If the audio player isn't currently playing anything, play the track immediately.
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
   * Goes to the next track in the queue.
   * <p>
   * If the audio player has finished its queue, update the bot's presence and activity.
   */
  public void nextTrack() {
    if (!trackQueue.isEmpty()) {
      audioPlayer.startTrack(trackQueue.get(0).getAudioTrack(), false);
      trackQueue.remove(0);
    } else if (!audioPlayerLooped) {
      audioPlayer.stopTrack();

      Presence presence = Bot.getApi().getPresence();
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
   *
   * @param skippedTrack {@link TrackQueueIndex}
   */
  public void addToSkippedTracks(TrackQueueIndex skippedTrack) {
    skippedTracks.addFirst(skippedTrack);
    if (skippedTracks.size() > 10) {
      skippedTracks.removeLast();
    }
  }

  /**
   * Gets the audio player.
   *
   * @return audio player
   */
  public AudioPlayer getAudioPlayer() {
    return this.audioPlayer;
  }

  /**
   * Gets the track queue.
   *
   * @return track queue
   */
  public List<TrackQueueIndex> getTrackQueue() {
    return this.trackQueue;
  }

  /**
   * Gets the skipped tracks.
   *
   * @return skipped tracks
   */
  public LinkedList<TrackQueueIndex> getSkippedTracks() {
    return this.skippedTracks;
  }

  /**
   * Gets if the audio player is looped.
   *
   * @return if the audio player is looped
   */
  public boolean getAudioPlayerLooped() {
    return this.audioPlayerLooped;
  }

  /**
   * Toggles the state of the audio player.
   */
  public void toggleAudioPlayerLooped() {
    audioPlayerLooped = !audioPlayerLooped;
  }
}
