package commands.audio.managers;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import commands.owner.Settings;
import net.dv8tion.jda.api.EmbedBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class AudioScheduler extends AudioEventAdapter {
  private final AudioPlayer audioPlayer;
  private ArrayList<AudioTrack> queueList;
  private ArrayList<String> requesterList;
  private Boolean loop = false;

  public AudioScheduler(AudioPlayer audioPlayer) {
    this.audioPlayer = audioPlayer;
    this.queueList = new ArrayList<AudioTrack>();
    this.requesterList = new ArrayList<String>();
  }

  public void queue(AudioTrack audioTrack) {
    if(this.audioPlayer.getPlayingTrack()==null){ // Play request immediately if nothing playing
      this.audioPlayer.startTrack(audioTrack,true);
      // Only remove requesters when track requested is not immediately playing
      if(!this.requesterList.isEmpty()) {
        this.requesterList.remove(0);
      }
    } else this.queueList.add(audioTrack); // Add to queue
  }

  public void nextTrack() {
    if (!this.queueList.isEmpty()) {
      this.audioPlayer.startTrack(this.queueList.get(0), false);
      this.queueList.remove(0);
      this.requesterList.remove(0);
    } else {
      this.audioPlayer.stopTrack();
    }
  }

  @Override
  public void onTrackStart(AudioPlayer audioPlayer, AudioTrack audioTrack) {
  }

  @Override
  public void onTrackEnd(AudioPlayer audioPlayer, AudioTrack audioTrack, AudioTrackEndReason endReason) {
    if (endReason.mayStartNext) {
      if (this.loop) {  // Loop
        this.audioPlayer.startTrack(audioTrack.makeClone(), false);
        return;
      }
      nextTrack();
    }
  }

  public void addToRequesterList(String requester) { // Track requester array
    this.requesterList.add(requester);
  }

  public void clearQueue(CommandEvent ce) { // ClearQueue
    this.queueList.clear();
    this.requesterList.clear();
    StringBuilder clearQueueConfirmation = new StringBuilder();
    clearQueueConfirmation.append("**Queue Clear:** [").append(ce.getAuthor().getAsTag()).append("]");
    ce.getChannel().sendMessage(clearQueueConfirmation).queue();
  }

  public void setLoopState(CommandEvent ce) { // Loop
    StringBuilder loopConfirmation = new StringBuilder();
    if (this.loop) { // Loop On -> Off
      this.loop = false;
      loopConfirmation.append("**Loop:** `OFF` [").append(ce.getAuthor().getAsTag()).append("]");
    } else { // Loop Off -> On
      this.loop = true;
      loopConfirmation.append("**Loop:** `ON` [").append(ce.getAuthor().getAsTag()).append("]");
    }
    ce.getChannel().sendMessage(loopConfirmation).queue();
  }

  public void getNowPlaying(CommandEvent ce) { // NowPlaying
    StringBuilder nowPlayingConfirmation = new StringBuilder();
    if (this.audioPlayer.getPlayingTrack() == null) { // Not playing anything
      nowPlayingConfirmation.append("**Now Playing:** `Nothing`");
    } else { // Current track playing
      // Duration
      AudioTrack audioTrack = this.audioPlayer.getPlayingTrack();
      long trackPositionLong = audioTrack.getPosition();
      long trackDurationLong = audioTrack.getDuration();
      String trackPosition = floatTimeConversion(trackPositionLong);
      String trackDuration = floatTimeConversion(trackDurationLong);
      nowPlayingConfirmation.append("**Now Playing:** ");
      if (this.audioPlayer.isPaused()) { // Paused
        nowPlayingConfirmation.append("(Paused) ");
      }
      if (this.loop) { // Looped
        nowPlayingConfirmation.append("(Loop) ");
      }
      nowPlayingConfirmation.append("`").append(audioTrack.getInfo().title).
          append("` {*").append(trackPosition).append("*-*").append(trackDuration).append("*}");
    }
    ce.getChannel().sendMessage(nowPlayingConfirmation).queue();
  }

  public void setPauseState(CommandEvent ce) { // Pause
    if (!this.audioPlayer.isPaused()) { // Paused Off -> On
      this.audioPlayer.setPaused(true);
      ce.getChannel().sendMessage("Audio player paused.").queue();
    } else { // Paused On -> Off
      this.audioPlayer.setPaused(false);
      ce.getChannel().sendMessage("Audio player resumed.").queue();
    }
  }

  public void playNext(CommandEvent ce, int entryNumber) { // PlayNext
    try { // Move track to first
      entryNumber = entryNumber - 1; // Human count to machine count
      AudioTrack audioTrack = this.queueList.get(entryNumber);
      long trackDurationLong = audioTrack.getDuration();
      String trackDuration = floatTimeConversion(trackDurationLong);
      StringBuilder playNextConfirmation = new StringBuilder();
      playNextConfirmation.append("**Play Next:** **[").append(entryNumber + 1).
          append("]** `").append(audioTrack.getInfo().title).
          append("` {*").append(trackDuration).append("*} ").
          append(this.requesterList.get(entryNumber)).append(" [").
          append(ce.getAuthor().getAsTag()).append("]");
      this.queueList.remove(entryNumber);
      this.queueList.add(0, audioTrack);
      ce.getChannel().sendMessage(playNextConfirmation).queue();
    } catch (IndexOutOfBoundsException error) { // Track number out of bounds
      ce.getChannel().sendMessage("Queue number does not exist.").queue();
    }
  }

  public void getQueue(CommandEvent ce, int queuePage) { // Queue
    if (!this.queueList.isEmpty()) { // No tracks
      int queueListSize = this.queueList.size();
      int totalQueuePages = queueListSize / 10; // Full pages
      if ((queueListSize % 10) > 0) { // Partially filled pages
        totalQueuePages += 1;
      }
      if (queuePage >= totalQueuePages) { // Page number cannot exceed total pages
        queuePage = totalQueuePages - 1;
      }
      if (queuePage < 0) { // Page number cannot be below 0
        queuePage = 0;
      }
      int queuePageDisplay = queuePage * 10; // Which queue page to start
      if (queuePageDisplay == queueListSize) { // Don't display 0 ending first
        queuePageDisplay -= 10;
      }
      int lastQueueEntry = Math.min((queuePageDisplay + 10), queueListSize); // Last queue entry to display
      // Display last entries
      // Display only 10 entries at a time
      StringBuilder queueEntry = new StringBuilder();
      for (int i = queuePageDisplay; i < lastQueueEntry; i++) { // Queue Entries
        long trackDurationLong = this.queueList.get(i).getDuration();
        String trackDuration = floatTimeConversion(trackDurationLong);
        queueEntry.append("**[").append(i + 1).append("]** `").
            append(this.queueList.get(i).getInfo().title)
            .append("` {*").append(trackDuration).append("*} ").
            append(this.requesterList.get(i)).append("\n");
      }
      // Duration
      AudioTrack audioTrack = this.audioPlayer.getPlayingTrack();
      long trackPositionLong = audioTrack.getPosition();
      long trackDurationLong = audioTrack.getDuration();
      String trackPosition = floatTimeConversion(trackPositionLong);
      String trackDuration = floatTimeConversion(trackDurationLong);
      EmbedBuilder display = new EmbedBuilder();
      display.setTitle("__**Queue**__");
      StringBuilder queueDisplay = new StringBuilder();
      queueDisplay.append("**Now Playing:** ");
      if (this.audioPlayer.isPaused()) { // Paused
        queueDisplay.append("(Paused) ");
      }
      if (this.loop) { // Looped
        queueDisplay.append("(Loop) ");
      }
      queueDisplay.append("`").append(audioTrack.getInfo().title).
          append("` {*").append(trackPosition).append("*-*").
          append(trackDuration).append("*}\nPage `").append(queuePage + 1).
          append("` / `").append(totalQueuePages).append("`");
      display.setDescription(queueDisplay);
      display.addField("**Tracks:**", String.valueOf(queueEntry), false);
      Settings.sendEmbed(ce, display);
    } else {
      ce.getChannel().sendMessage("Queue is empty.").queue();
    }
  }

  public void removeQueueEntry(CommandEvent ce, int entryNumber) { // Remove
    try {
      entryNumber = entryNumber - 1; // Human count to machine count
      StringBuilder removeQueueEntryConfirmation = new StringBuilder();
      removeQueueEntryConfirmation.append("**Removed:** **[").append(entryNumber + 1).append("]** `")
          .append(this.queueList.get(entryNumber).getInfo().title).append("`")
          .append(this.requesterList.get(entryNumber))
          .append(" *[").append(ce.getAuthor().getAsTag()).append("]*");
      this.queueList.remove(entryNumber);
      this.requesterList.remove(entryNumber);
      ce.getChannel().sendMessage(removeQueueEntryConfirmation).queue();
    } catch (IndexOutOfBoundsException error) {
      ce.getChannel().sendMessage("Queue entry number does not exist.").queue();
    }
  }

  public void setPosition(CommandEvent ce, String positionString) { // SetPosition
    if (!(this.audioPlayer.getPlayingTrack() == null)) { // Track exists
      String[] positionTimeType = positionString.split(":");
      long seconds = 0;
      long minutes = 0;
      long hours = 0;
      switch (positionTimeType.length) {
        case 1 -> // Seconds
            seconds = Integer.parseInt(positionTimeType[0]);
        case 2 -> { // Minutes, Seconds
          minutes = Integer.parseInt(positionTimeType[0]);
          seconds = Integer.parseInt(positionTimeType[1]);
        }
        case 3 -> { // Hours, Minutes, Seconds
          hours = Integer.parseInt(positionTimeType[0]);
          minutes = Integer.parseInt(positionTimeType[1]);
          seconds = Integer.parseInt(positionTimeType[2]);
        }
        default -> // Invalid argument
            ce.getChannel().sendMessage("Invalid number of arguments.").queue();
      }
      // Conversion to milliseconds
      hours = hours * 3600000;
      minutes = minutes * 60000;
      seconds = seconds * 1000;
      long totalPosition = hours + minutes + seconds;
      // Requested time is smaller than total track length
      if (this.audioPlayer.getPlayingTrack().getDuration() > totalPosition) {
        this.audioPlayer.getPlayingTrack().setPosition(totalPosition);
        String positionSet = floatTimeConversion(totalPosition);
        StringBuilder setPositionConfirmation = new StringBuilder();
        setPositionConfirmation.append("**Set Position:** {*").append(positionSet).
            append("*} [").append(ce.getAuthor().getAsTag()).append("]");
        ce.getChannel().sendMessage(setPositionConfirmation).queue();
      } else { // Requested time exceeds track length
        ce.getChannel().sendMessage("Requested position exceeds track length.").queue();
      }
    } else { // No track currently playing
      ce.getChannel().sendMessage("Nothing is currently playing.").queue();
    }
  }

  public void shuffleQueue(CommandEvent ce) { // Shuffle
    Random rand = new Random();
    for (int i = 0; i < this.queueList.size(); i++) {
      int indexSwitch = rand.nextInt(this.queueList.size());
      Collections.swap(queueList, i, indexSwitch);
      Collections.swap(requesterList, i, indexSwitch);
    }
    StringBuilder shuffleConfirmation = new StringBuilder();
    shuffleConfirmation.append("**Shuffle:** [").append(ce.getAuthor().getAsTag()).append("]");
    ce.getChannel().sendMessage(shuffleConfirmation).queue();
  }

  public void skipTrack(CommandEvent ce) { // Skip
    if (!(this.audioPlayer.getPlayingTrack() == null)) {
      nextTrack();
      StringBuilder skipTrackConfirmation = new StringBuilder();
      skipTrackConfirmation.append("**Skip:** [").append(ce.getAuthor().getAsTag()).append("]");
      ce.getChannel().sendMessage(skipTrackConfirmation).queue();
    } else {
      ce.getChannel().sendMessage("Nothing to skip.").queue();
    }
  }

  public void swap(CommandEvent ce, int originalQueue, int swapQueue) {
    try {
      originalQueue = originalQueue - 1; // Human count to machine count
      swapQueue = swapQueue - 1;
      AudioTrack originalTrack = this.queueList.get(originalQueue);
      AudioTrack swapTrack = this.queueList.get(swapQueue);
      long originalTrackDurationLong = originalTrack.getDuration();
      long swapTrackDurationLong = swapTrack.getDuration();
      String originalTrackDuration = floatTimeConversion(originalTrackDurationLong);
      String swapTrackDuration = floatTimeConversion(swapTrackDurationLong);
      StringBuilder swapConfirmation = new StringBuilder();
      swapConfirmation.append("**Swap:** ").append(" [").
          append(ce.getAuthor().getAsTag()).append("]\n**[").append(originalQueue + 1).
          append("]** `").append(originalTrack.getInfo().title).
          append("` {*").append(originalTrackDuration).append("*} ").
          append(this.requesterList.get(originalQueue)).append("\n**[").
          append(swapQueue + 1).append("]** `").append(swapTrack.getInfo().title).
          append("` {*").append(swapTrackDuration).append("*} ").
          append(this.requesterList.get(swapQueue));
      Collections.swap(this.queueList, originalQueue, swapQueue);
      ce.getChannel().sendMessage(swapConfirmation).queue();
    } catch (IndexOutOfBoundsException error) {
      ce.getChannel().sendMessage("Queue entry number does not exist.").queue();
    }
  }

  private String floatTimeConversion(long floatTime) {
    long days = floatTime / 86400000 % 30;
    long hours = floatTime / 3600000 % 24;
    long minutes = floatTime / 60000 % 60;
    long seconds = floatTime / 1000 % 60;
    return (days == 0 ? "" : days < 10 ? "0" + days + ":" : days + ":") +
        (hours == 0 ? "" : hours < 10 ? "0" + hours + ":" : hours + ":") +
        (minutes == 0 ? "00:" : minutes < 10 ? "0" + minutes + ":" : minutes + ":") +
        (seconds == 0 ? "00" : seconds < 10 ? "0" + seconds : seconds + "");
  }
}
