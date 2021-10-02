package commands.audio.managers;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import java.util.ArrayList;

public class AudioScheduler extends AudioEventAdapter {
  private final AudioPlayer audioPlayer;
  private ArrayList<AudioTrack> queue = new ArrayList<>();

  public AudioScheduler(AudioPlayer audioPlayer) {
    this.audioPlayer = audioPlayer;
  }

  public void queue(AudioTrack track) {
    if (!audioPlayer.startTrack(track, true)) {
      this.queue.add(track);
    }
  }
}
