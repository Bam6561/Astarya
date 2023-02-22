package commands.audio.managers;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import lucyfer.LucyferBot;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;

import java.util.ArrayList;

public class AudioScheduler extends AudioEventAdapter {
  private final AudioPlayer audioPlayer;
  private ArrayList<AudioTrack> trackQueue;
  private ArrayList<String> requesterList;
  private ArrayList<AudioTrack> skippedStack;
  private Boolean audioPlayerLoopState = false;

  public AudioScheduler(AudioPlayer audioPlayer) {
    this.audioPlayer = audioPlayer;
    this.trackQueue = new ArrayList<AudioTrack>();
    this.requesterList = new ArrayList<String>();
    this.skippedStack = new ArrayList<AudioTrack>();
  }

  // Adds track to track queue
  public void queue(AudioTrack audioTrack) {
    if (this.audioPlayer.getPlayingTrack() == null) { // Play request immediately if nothing playing
      this.audioPlayer.startTrack(audioTrack, true);
    } else {
      this.trackQueue.add(audioTrack); // Add to queue
    }
  }

  // Goes to next track in track queue
  public void nextTrack() {
    if (!this.trackQueue.isEmpty()) {
      this.audioPlayer.startTrack(this.trackQueue.get(0), false);
      this.trackQueue.remove(0);
      if (!this.requesterList.isEmpty()) {
        this.requesterList.remove(0);
      }
    } else { // Update presence when not playing audio
      this.audioPlayer.stopTrack();
      LucyferBot lucyferBot = new LucyferBot();
      lucyferBot.getApi().getPresence().setStatus(OnlineStatus.DO_NOT_DISTURB);
      lucyferBot.getApi().getPresence().setActivity(Activity.listening("Nothing"));
    }
  }

  // Update presence when playing new track if not looped
  @Override
  public void onTrackStart(AudioPlayer audioPlayer, AudioTrack audioTrack) {
    if (!this.audioPlayerLoopState) {
      LucyferBot lucyferBot = new LucyferBot();
      lucyferBot.getApi().getPresence().setStatus(OnlineStatus.ONLINE);
      lucyferBot.getApi().getPresence().setActivity(Activity.listening(audioTrack.getInfo().title));
    }
  }

  // Loops track if looped
  @Override
  public void onTrackEnd(AudioPlayer audioPlayer, AudioTrack audioTrack, AudioTrackEndReason endReason) {
    if (endReason.mayStartNext) {
      if (this.audioPlayerLoopState) {  // Loop
        this.audioPlayer.startTrack(audioTrack.makeClone(), false);
        return;
      }
      nextTrack();
    }
  }

  // Access and add variables outside this class
  public void addToRequesterList(String requester) { // Track requester array
    this.requesterList.add(requester);
  }

  // Recently skipped songs go to the top, increment all existing track indices by 1
  public void addToSkippedStack(AudioTrack skippedTrack) {
    this.skippedStack.add(0, skippedTrack);
    boolean skippedStackOverLimit = skippedStack.size() > 10;
    if (skippedStackOverLimit) {
      skippedStack.remove(9);
    }
  }

  // Get and set various variables outside this class
  public AudioPlayer getAudioPlayer() {
    return this.audioPlayer;
  }

  public ArrayList<AudioTrack> getTrackQueue() {
    return this.trackQueue;
  }

  public ArrayList<String> getRequesterList() {
    return this.requesterList;
  }

  public ArrayList<AudioTrack> getSkippedStack() {
    return this.skippedStack;
  }

  public boolean getAudioPlayerLoopState() {
    return this.audioPlayerLoopState;
  }

  public void setAudioPlayerLoopState(boolean audioPlayerLoopState) {
    this.audioPlayerLoopState = audioPlayerLoopState;
  }
}
