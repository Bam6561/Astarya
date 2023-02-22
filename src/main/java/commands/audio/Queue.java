package commands.audio;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import commands.audio.managers.AudioScheduler;
import commands.audio.managers.PlayerManager;
import commands.owner.Settings;
import net.dv8tion.jda.api.EmbedBuilder;

import java.util.ArrayList;

public class Queue extends Command {
  private int pageRequested;
  private int numberOfPages;
  private int firstQueueIndexOnPage;


  public Queue() {
    this.name = "queue";
    this.aliases = new String[]{"queue", "q"};
    this.arguments = "[0]Queue, [1]PageNumber";
    this.help = "Provides a list of audio tracks queued.";
  }

  /*
  Sends an embed containing information about the track queue
  with 10 results on each page or now playing if no queue
   */
  @Override
  protected void execute(CommandEvent ce) {
    Settings.deleteInvoke(ce);

    // Parse message for arguments
    String[] arguments = ce.getMessage().getContentRaw().split("\\s");
    int numberOfArguments = arguments.length - 1;

    switch (numberOfArguments) {
      case 0 -> // First queue page
          getTrackQueuePage(ce, 0);
      case 1 -> {
        try { // Search for queue page
          getTrackQueuePage(ce, Integer.parseInt(arguments[1]) - 1);
        } catch (NumberFormatException error) {
          ce.getChannel().sendMessage("Specify an integer for queue page number.").queue();
        }
      }
      default -> ce.getChannel().sendMessage("Invalid number of arguments.").queue();
    }
  }

  // Sends track queue pages
  public void getTrackQueuePage(CommandEvent ce, int pageRequested) {
    AudioScheduler audioScheduler = PlayerManager.getINSTANCE().getPlaybackManager(ce.getGuild()).audioScheduler;
    AudioPlayer audioPlayer = audioScheduler.getAudioPlayer();

    // Storage objects to access
    ArrayList<AudioTrack> trackQueue = audioScheduler.getTrackQueue();
    ArrayList<String> requesterList = audioScheduler.getRequesterList();

    boolean trackQueueIsNotEmpty = !trackQueue.isEmpty();
    if (trackQueueIsNotEmpty) {
      // Adjust page requested, get number of pages, find first queue index on page
      calculatePageToDisplay(trackQueue.size(), pageRequested);

      // Populate page with track entries
      StringBuilder queuePage = new StringBuilder();
      createPage(queuePage, trackQueue, requesterList);

      // Add nowPlaying on queue page
      StringBuilder queuePageEmbedNowPlaying = new StringBuilder();
      queuePageEmbedNowPlaying.append("**Now Playing:** ");
      audioPlayerIsPausedOrLoopedNotice(audioScheduler, audioPlayer, queuePageEmbedNowPlaying);

      boolean currentlyPlayingTrack = !(audioPlayer.getPlayingTrack() == null);
      if (currentlyPlayingTrack) {
        AudioTrack audioTrack = audioPlayer.getPlayingTrack();
        String trackPosition = longTimeConversion(audioTrack.getPosition());
        String trackDuration = longTimeConversion(audioTrack.getDuration());

        queuePageEmbedNowPlaying.append("`").append(audioTrack.getInfo().title).
            append("` {*").append(trackPosition).append("*-*").
            append(trackDuration).append("*}\nPage `").append(getPageRequested() + 1).
            append("` / `").append(getNumberOfPages()).append("`");
      } else {
        queuePageEmbedNowPlaying.append("`").append("Nothing")
            .append("`\nPage `").append(getPageRequested() + 1).
            append("` / `").append(getNumberOfPages()).append("`");
      }

      // Display queue page
      EmbedBuilder display = new EmbedBuilder();
      display.setTitle("__**Queue**__");
      display.setDescription(queuePageEmbedNowPlaying);
      display.addField("**Tracks:**", String.valueOf(queuePage), false);

      Settings.sendEmbed(ce, display);
    } else { // Display nowPlaying only
      nowPlaying(ce, audioScheduler, audioPlayer);
    }
  }

  // Corrects user's requested page to view and determines which track queue page to display
  private void calculatePageToDisplay(int numberOfTracksInQueue, int pageRequested) {
    // Full pages show 10 tracks each and a partially filled page adds one page
    int numberOfPages = numberOfTracksInQueue / 10;
    boolean partiallyFilledPage = (numberOfTracksInQueue % 10) > 0;
    if (partiallyFilledPage) {
      numberOfPages += 1;
    }

    /*
    If user requests more than total pages, set the request to last page
    Displayed index to users different from data index so subtract 1
     */
    boolean requestMoreThanTotalPages = pageRequested >= numberOfPages;
    boolean requestNegativePageNumber = pageRequested < 0;
    if (requestMoreThanTotalPages) {
      pageRequested = numberOfPages - 1;
    } else if (requestNegativePageNumber) {
      pageRequested = 0;
    }

    /*
    Multiply page requested by 10 to find the track index to display first
    Subtract a page (10 tracks) to display partially filled pages if the next page would be empty
     */
    int firstQueueIndexOnPage = pageRequested * 10;
    if (firstQueueIndexOnPage == numberOfTracksInQueue) {
      firstQueueIndexOnPage -= 10;
    }

    setPageRequested(pageRequested);
    setNumberOfPages(numberOfPages);
    setFirstQueueIndexOnPage(firstQueueIndexOnPage);
  }

  // Populates page with track entries
  private void createPage(StringBuilder trackQueuePage, ArrayList<AudioTrack> trackQueue,
                          ArrayList<String> requesterList) {
    /*
    Calculate which comes first, the next ten indices or the last track in the queue
    Used for partially filled queue pages
     */
    int numberOfTracksInQueue = trackQueue.size();
    int lastQueueIndexOnPage = Math.min((getFirstQueueIndexOnPage() + 10), numberOfTracksInQueue);

    for (int i = firstQueueIndexOnPage; i < lastQueueIndexOnPage; i++) {
      String trackDuration = longTimeConversion(trackQueue.get(i).getDuration());
      trackQueuePage.append("**[").append(i + 1).append("]** `").
          append(trackQueue.get(i).getInfo().title)
          .append("` {*").append(trackDuration).append("*} ").
          append(requesterList.get(i)).append("\n");
    }
  }

  // Displays currently playing track
  private void nowPlaying(CommandEvent ce, AudioScheduler audioScheduler, AudioPlayer audioPlayer) {
    StringBuilder nowPlayingConfirmation = new StringBuilder();

    boolean currentlyPlayingTrack = !(audioPlayer.getPlayingTrack() == null);
    if (currentlyPlayingTrack) {
      AudioTrack audioTrack = audioPlayer.getPlayingTrack();
      String trackPosition = longTimeConversion(audioTrack.getPosition());
      String trackDuration = longTimeConversion(audioTrack.getDuration());

      // nowPlaying Confirmation
      nowPlayingConfirmation.append("**Now Playing:** ");
      audioPlayerIsPausedOrLoopedNotice(audioScheduler, audioPlayer, nowPlayingConfirmation);
      nowPlayingConfirmation.append("`").append(audioTrack.getInfo().title).
          append("` {*").append(trackPosition).append("*-*").append(trackDuration).append("*}");
    } else {
      nowPlayingConfirmation.append("**Now Playing:** `Nothing`");
    }
    ce.getChannel().sendMessage(nowPlayingConfirmation).queue();
  }

  // Conditional setting notices for nowPlaying
  private void audioPlayerIsPausedOrLoopedNotice(AudioScheduler audioScheduler,
                                                 AudioPlayer audioPlayer, StringBuilder nowPlayingConfirmation) {
    boolean audioPlayerIsPaused = audioPlayer.isPaused();
    boolean audioPlayerIsLooped = audioScheduler.getAudioPlayerLoopState();
    if (audioPlayerIsPaused) {
      nowPlayingConfirmation.append("(Paused) ");
    }
    if (audioPlayerIsLooped) {
      nowPlayingConfirmation.append("(Loop) ");
    }
  }

  // Converts long duration to conventional readable time
  private String longTimeConversion(long longTime) {
    long days = longTime / 86400000 % 30;
    long hours = longTime / 3600000 % 24;
    long minutes = longTime / 60000 % 60;
    long seconds = longTime / 1000 % 60;
    return (days == 0 ? "" : days < 10 ? "0" + days + ":" : days + ":") +
        (hours == 0 ? "" : hours < 10 ? "0" + hours + ":" : hours + ":") +
        (minutes == 0 ? "00:" : minutes < 10 ? "0" + minutes + ":" : minutes + ":") +
        (seconds == 0 ? "00" : seconds < 10 ? "0" + seconds : seconds + "");
  }

  // Get and set various variables
  private int getPageRequested() {
    return this.pageRequested;
  }

  private int getNumberOfPages() {
    return this.numberOfPages;
  }

  private int getFirstQueueIndexOnPage() {
    return this.firstQueueIndexOnPage;
  }

  private void setPageRequested(int pageRequested) {
    this.pageRequested = pageRequested;
  }

  private void setNumberOfPages(int numberOfPages) {
    this.numberOfPages = numberOfPages;
  }

  private void setFirstQueueIndexOnPage(int firstQueueIndexOnPage) {
    this.firstQueueIndexOnPage = firstQueueIndexOnPage;
  }
}