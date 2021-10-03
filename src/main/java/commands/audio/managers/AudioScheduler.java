package commands.audio.managers;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import commands.owner.Settings;
import net.dv8tion.jda.api.EmbedBuilder;

import java.util.ArrayList;
import java.util.Random;

public class AudioScheduler extends AudioEventAdapter {
  private final AudioPlayer audioPlayer;
  private ArrayList<AudioTrack> queueList;
  private ArrayList<String> requesterList;
  private ArrayList<String> nowPlaying;
  private Boolean looped = false;

  public AudioScheduler(AudioPlayer audioPlayer) {
    this.audioPlayer = audioPlayer;
    this.queueList = new ArrayList<AudioTrack>();
    this.requesterList = new ArrayList<String>();
    this.nowPlaying = new ArrayList<String>();
  }

  public void queue(AudioTrack audioTrack) {
    if (!this.audioPlayer.startTrack(audioTrack, true)) {
      this.queueList.add(audioTrack);
    }
  }

  public void nextTrack() {
    this.audioPlayer.startTrack(this.queueList.get(0), false);
    this.queueList.remove(0);
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
      if (this.looped) {
        this.audioPlayer.startTrack(audioTrack.makeClone(), false);
        return;
      }
      nextTrack();
    }
  }

  public void setLooped(CommandEvent ce) {
    StringBuilder loopedConfirmation = new StringBuilder();
    if (this.looped) {
      this.looped = false;
      loopedConfirmation.append("**LOOPED:** Loop turned off. [").append(ce.getAuthor().getAsTag()).append("]");
      ce.getChannel().sendMessage(loopedConfirmation).queue();
    } else {
      this.looped = true;
      loopedConfirmation.append("**LOOPED:** Loop turned on. [").append(ce.getAuthor().getAsTag()).append("]");
      ce.getChannel().sendMessage(loopedConfirmation).queue();
    }
  }

  public void getNowPlaying(CommandEvent ce) {
    String nowPlayingString = "**Now Playing:** `" + this.nowPlaying.get(0) + "` " +
        this.requesterList.get(0);
    ce.getChannel().sendMessage(String.valueOf(nowPlayingString)).queue();
  }

  public AudioPlayer getAudioPlayer() {
    return audioPlayer;
  }

  public String getRequesterListName() {
    return this.requesterList.get(0);
  }

  public String getQueueListTitle() {
    return this.queueList.get(0).getInfo().title;
  }

  public void addToRequesterList(String requester) { // Track requester array
    this.requesterList.add(requester);
  }

  public void forceSkip() {
    nextTrack();
  }

  public void Shuffle() {
    Random rand = new Random();
    for (int i = 0; i < queueList.size(); i++) {
      int indexSwitch = rand.nextInt(queueList.size());
      AudioTrack audioTrackTemp = queueList.get(i);
      String stringTemp = requesterList.get(i);
      queueList.set(i, queueList.get(indexSwitch));
      queueList.set(indexSwitch, audioTrackTemp);
      requesterList.set(i, requesterList.get(indexSwitch));
      requesterList.set(indexSwitch, stringTemp);
    }
  }

  public void getQueue(CommandEvent ce, int queuePage) { // Track queue
    if (!this.queueList.isEmpty()) { // No tracks
      int totalQueuePages = this.queueList.size() / 10; // Full pages
      if ((this.queueList.size() % 10) > 0) { // Partially filled pages
        totalQueuePages += 1;
      }
      if (queuePage >= totalQueuePages) { // Page number cannot exceed total pages
        queuePage = totalQueuePages - 1;
      }
      if (queuePage < 0) { // Page number cannot be below 0
        queuePage = 0;
      }
      int queuePageDisplay = queuePage * 10; // Which queue page to start
      if (queuePageDisplay == this.queueList.size()) { // Don't display 0 ending first
        queuePageDisplay -= 10;
      }
      int lastQueueEntry = Math.min((queuePageDisplay + 10), this.queueList.size()); // Last queue entry to display
      // Display last entries
      // Display only 10 entries at a time
      StringBuilder queueString = new StringBuilder();
      for (int i = queuePageDisplay; i < lastQueueEntry; i++) { // Queue Entries
        queueString.append("**[").append(i + 1).append("]** `").append(queueList.get(i).getInfo().title)
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
          .append(this.queueList.get(entryNumber).getInfo().title).append("`")
          .append(this.requesterList.get(entryNumber))
          .append(" *[").append(ce.getAuthor().getAsTag()).append("]*");
      this.queueList.remove(entryNumber);
      this.requesterList.remove(entryNumber);
      ce.getChannel().sendMessage(removeQueueEntryConfirmation).queue();
    } catch (NullPointerException error) {
      ce.getChannel().sendMessage("Queue entry number does not exist.").queue();
    }
  }

  public void clearQueue(CommandEvent ce) {
    this.queueList.clear();
    this.requesterList.clear();
    StringBuilder queueClearConfirmation = new StringBuilder();
    queueClearConfirmation.append("**Queue Clear:** [").append(ce.getAuthor().getAsTag()).append("]");
    ce.getChannel().sendMessage(queueClearConfirmation).queue();
  }
}
