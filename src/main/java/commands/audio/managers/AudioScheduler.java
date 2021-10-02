package commands.audio.managers;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import commands.owner.Settings;
import net.dv8tion.jda.api.EmbedBuilder;

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

  public void addToRequesterList(String requester) { // Track requester array
    this.requesterList.add(requester);
  }

  public void getQueue(CommandEvent ce, int queuePage) { // Track queue
    if (!this.queue.isEmpty()) { // No tracks
      int totalQueuePages = this.queue.size() / 10; // Full pages
      if ((this.queue.size() % 10) > 0) { // Partially filled pages
        totalQueuePages += 1;
      }
      if (queuePage >= totalQueuePages) { // Page number cannot exceed total pages
        queuePage = totalQueuePages - 1;
      }
      if (queuePage < 0) { // Page number cannot be below 0
        queuePage = 0;
      }
      int queuePageDisplay = queuePage * 10; // Which queue page to start
      if (queuePageDisplay == this.queue.size()) { // Don't display 0 ending first
        queuePageDisplay -= 10;
      }
      int lastQueueEntry = Math.min((queuePageDisplay + 10), this.queue.size()); // Last queue entry to display
      // Display last entries
      // Display only 10 entries at a time
      StringBuilder queueString = new StringBuilder();
      for (int i = queuePageDisplay; i < lastQueueEntry; i++) { // Queue Entries
        queueString.append("**[").append(i + 1).append("]** `").append(queue.get(i).getInfo().title)
            .append("` ").append(requesterList.get(i)).append("\n");
      }
      EmbedBuilder display = new EmbedBuilder();
      display.setTitle("__**Queue**__");
      String description = "Displaying page `" + (queuePage + 1) + "` / `" + totalQueuePages + "`";
      display.setDescription(description);
      display.addField("**Tracks:**", String.valueOf(queueString), false);
      Settings.sendEmbed(ce, display);
    } else {
      ce.getChannel().sendMessage("Queue is empty.").queue();
    }
  }
}
