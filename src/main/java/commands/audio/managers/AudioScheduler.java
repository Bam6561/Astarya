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
  private ArrayList<AudioTrack> queue;
  private ArrayList<String> requesterList;
  private ArrayList<String> nowPlaying;

  public AudioScheduler(AudioPlayer audioPlayer) {
    this.audioPlayer = audioPlayer;
    this.queue = new ArrayList<AudioTrack>();
    this.requesterList = new ArrayList<String>();
    this.nowPlaying = new ArrayList<String>();
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
    this.nowPlaying.remove(0);
  }

  @Override
  public void onTrackStart(AudioPlayer audioPlayer, AudioTrack audioTrack) {
    nowPlaying.add(audioTrack.getInfo().title);
  }

  @Override
  public void onTrackEnd(AudioPlayer audioPlayer, AudioTrack audioTrack, AudioTrackEndReason endReason) {
    if (endReason.mayStartNext) {
      nextTrack();
    }
  }

  public void getNowPlaying(CommandEvent ce) {
    StringBuilder nowPlayingString = new StringBuilder();
    nowPlayingString.append("**Now Playing:** `").append(this.nowPlaying.get(0)).append("` ")
        .append(this.requesterList.get(0));
    ce.getChannel().sendMessage(String.valueOf(nowPlayingString)).queue();
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

  public void removeQueueEntry(CommandEvent ce, int entryNumber) {
    try {
      entryNumber = entryNumber - 1;
      StringBuilder removeQueueEntryConfirmation = new StringBuilder();
      removeQueueEntryConfirmation.append("**Removed:** **[").append(entryNumber + 1).append("]** `")
          .append(this.queue.get(entryNumber).getInfo().title).append("`")
          .append(this.requesterList.get(entryNumber))
          .append(" *[").append(ce.getAuthor().getAsTag()).append("]*");
      this.queue.remove(entryNumber);
      this.requesterList.remove(entryNumber);
      ce.getChannel().sendMessage(removeQueueEntryConfirmation).queue();
    } catch (NullPointerException error) {
      ce.getChannel().sendMessage("Queue entry number does not exist.").queue();
    }
  }

  public void clearQueue(CommandEvent ce) {
    this.queue.clear();
    this.requesterList.clear();
    StringBuilder queueClearConfirmation = new StringBuilder();
    queueClearConfirmation.append("**Queue Clear:** [").append(ce.getAuthor().getAsTag()).append("]");
    ce.getChannel().sendMessage(queueClearConfirmation).queue();
  }
}
