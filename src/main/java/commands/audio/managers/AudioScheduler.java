package commands.audio.managers;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

import java.util.ArrayList;

public class AudioScheduler extends AudioEventAdapter {
  private final AudioPlayer audioPlayer;
  private ArrayList<AudioTrack> queue = new ArrayList<AudioTrack>();
  private ArrayList<String> requesterList = new ArrayList<String>();

  public AudioScheduler(AudioPlayer audioPlayer) {
    this.audioPlayer = audioPlayer;
    this.queue = new ArrayList<AudioTrack>();
    this.requesterList = new ArrayList<String>();
  }

  public void queue(AudioTrack audioTrack) {
    if (!this.audioPlayer.startTrack(audioTrack, true)) {
      this.queue.add(audioTrack);
    }
  }

  public void nextTrack() {
    this.audioPlayer.startTrack(this.queue.get(0), false);
    this.queue.remove(0);
    this.requesterList.remove(0);
  }

  @Override
  public void onTrackEnd(AudioPlayer audioPlayer, AudioTrack audioTrack, AudioTrackEndReason endReason) {
    if (endReason.mayStartNext) {
      nextTrack();
    }
  }

  public void addToRequesterList(String requester) {
    this.requesterList.add(requester);
  }

  public void getQueue(CommandEvent ce) {
    StringBuilder queueString = new StringBuilder();
    for (int i = 0; i < this.queue.size(); i++) {
      queueString.append("[").append(i).append("] `").append(queue.get(i).getInfo().title)
          .append("` ").append(requesterList.get(i)).append("\n");
    }
    ce.getChannel().sendMessage(queueString).queue();
  }


}
