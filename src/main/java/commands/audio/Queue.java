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

/**
 * Queue is a command invocation that provides a list
 * of tracks queued and what track is currently playing.
 *
 * @author Danny Nguyen
 * @version 1.6.6
 * @since 1.2.0
 */
public class Queue extends Command {
  private int pageRequested;
  private int numberOfPages;
  private int firstQueueIndexOnPage;

  public Queue() {
    this.name = "queue";
    this.aliases = new String[]{"queue", "q"};
    this.arguments = "[0]Queue [1]PageNumber";
    this.help = "Provides a list of tracks queued.";
  }

  /**
   * Either sends an embed containing information about the track queue with 10
   * results on each page or what track is currently playing if nothing is queued.
   * <p>
   * Users can optionally provide a queue page to be displayed with an additional parameter.
   * </p>
   *
   * @param ce object containing information about the command event
   * @throws NumberFormatException user provided non-integer value
   */
  @Override
  protected void execute(CommandEvent ce) {
    Settings.deleteInvoke(ce);

    // Parse message for parameters
    String[] parameters = ce.getMessage().getContentRaw().split("\\s");
    int numberOfParameters = parameters.length - 1;

    switch (numberOfParameters) {
      case 0 -> // First queue page
          getTrackQueuePage(ce, 0);
      case 1 -> {
        try { // Search for queue page
          getTrackQueuePage(ce, Integer.parseInt(parameters[1]) - 1);
        } catch (NumberFormatException error) {
          ce.getChannel().sendMessage("Specify an integer for queue page number.").queue();
        }
      }
      default -> ce.getChannel().sendMessage("Invalid number of parameters.").queue();
    }
  }

  /**
   * Sends an embed containing a track queue page that displays
   * what's currently playing and up to ten tracks on the page.
   *
   * @param ce            object containing information about the command event
   * @param pageRequested user provided page number to be displayed
   */
  public void getTrackQueuePage(CommandEvent ce, int pageRequested) {
    AudioScheduler audioScheduler = PlayerManager.getINSTANCE().getPlaybackManager(ce.getGuild()).audioScheduler;
    AudioPlayer audioPlayer = audioScheduler.getAudioPlayer();

    // Storage objects to access
    ArrayList<AudioTrack> trackQueue = audioScheduler.getTrackQueue();
    ArrayList<String> requesterList = audioScheduler.getRequesterList();

    if (!trackQueue.isEmpty()) {
      // Adjust page requested, get number of total pages, and find first queue index on the page
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
      display.setAuthor("Queue");
      display.setDescription(queuePageEmbedNowPlaying);
      display.addField("**Tracks:**", String.valueOf(queuePage), false);
      Settings.sendEmbed(ce, display);
    } else { // Display nowPlaying only
      nowPlaying(ce, audioScheduler, audioPlayer);
    }
  }

  /**
   * Corrects user's requested page to view and determines which
   * track queue page to display and its starting track entry.
   * <p>
   * Full pages show 10 tracks each and a partially filled page adds one to the total
   * number of pages. If the user requests more than the total number of pages, then set
   * the request to the last page. For negative page number requests, set the request to 0.
   * Multiply page requested by 10 to find the track index to display first, and subtract
   * by a page (10 tracks) to display partially filled pages if the page would be empty.
   * </p>
   *
   * @param numberOfTracksInQueue total number of tracks in track queue
   * @param pageRequested         user provided queue page to view
   */
  private void calculatePageToDisplay(int numberOfTracksInQueue, int pageRequested) {
    // Partition the queue into pages
    int numberOfPages = numberOfTracksInQueue / 10;
    boolean partiallyFilledPage = (numberOfTracksInQueue % 10) > 0;
    if (partiallyFilledPage) {
      numberOfPages += 1;
    }

    // Correct invalid user requests
    boolean requestMoreThanTotalPages = pageRequested >= numberOfPages;
    boolean requestNegativePageNumber = pageRequested < 0;
    if (requestMoreThanTotalPages) {
      pageRequested = numberOfPages - 1;
    } else if (requestNegativePageNumber) {
      pageRequested = 0;
    }

    // Decide what the first track entry to be displayed on the page is
    int firstQueueIndexOnPage = pageRequested * 10;
    if (firstQueueIndexOnPage == numberOfTracksInQueue) {
      firstQueueIndexOnPage -= 10;
    }

    setPageRequested(pageRequested);
    setNumberOfPages(numberOfPages);
    setFirstQueueIndexOnPage(firstQueueIndexOnPage);
  }

  /**
   * Populates a queue page with track entries.
   * <p>
   * For partially filled pages, calculate which comes first - the
   * next ten indices or the last track entry  in the track queue.
   * </p>
   *
   * @param queuePage     contents of the queue page
   * @param trackQueue    ArrayList containing the track queue
   * @param requesterList ArrayList containing the track requesters
   */
  private void createPage(StringBuilder queuePage, ArrayList<AudioTrack> trackQueue,
                          ArrayList<String> requesterList) {
    // Calculate last track entry to be displayed
    int numberOfTracksInQueue = trackQueue.size();
    int lastQueueIndexOnPage = Math.min((getFirstQueueIndexOnPage() + 10), numberOfTracksInQueue);

    // Build contents of queue page embed
    for (int i = firstQueueIndexOnPage; i < lastQueueIndexOnPage; i++) {
      String trackDuration = longTimeConversion(trackQueue.get(i).getDuration());
      queuePage.append("**[").append(i + 1).append("]** `").
          append(trackQueue.get(i).getInfo().title)
          .append("` {*").append(trackDuration).append("*} ").
          append(requesterList.get(i)).append("\n");
    }
  }

  /**
   * Displays the currently playing track.
   *
   * @param ce             object containing information about the command event
   * @param audioScheduler the bot's audio scheduler
   * @param audioPlayer    the bot's audio player
   */
  private void nowPlaying(CommandEvent ce, AudioScheduler audioScheduler, AudioPlayer audioPlayer) {
    StringBuilder nowPlaying = new StringBuilder();

    boolean currentlyPlayingTrack = !(audioPlayer.getPlayingTrack() == null);
    if (currentlyPlayingTrack) {
      AudioTrack audioTrack = audioPlayer.getPlayingTrack();
      String trackPosition = longTimeConversion(audioTrack.getPosition());
      String trackDuration = longTimeConversion(audioTrack.getDuration());

      // NowPlaying confirmation
      nowPlaying.append("**Now Playing:** ");
      audioPlayerIsPausedOrLoopedNotice(audioScheduler, audioPlayer, nowPlaying);
      nowPlaying.append("`").append(audioTrack.getInfo().title).
          append("` {*").append(trackPosition).append("*-*").append(trackDuration).append("*}");
    } else {
      nowPlaying.append("**Now Playing:** `Nothing`");
    }
    ce.getChannel().sendMessage(nowPlaying).queue();
  }

  /**
   * Adds conditional setting notes for the nowPlaying section.
   *
   * @param audioScheduler the bot's audio scheduler
   * @param audioPlayer    the bot's audio player
   * @param nowPlaying     String containing information about what track is currently playing
   */
  private void audioPlayerIsPausedOrLoopedNotice(AudioScheduler audioScheduler,
                                                 AudioPlayer audioPlayer, StringBuilder nowPlaying) {
    boolean audioPlayerIsPaused = audioPlayer.isPaused();
    boolean audioPlayerIsLooped = audioScheduler.getAudioPlayerLoopState();
    if (audioPlayerIsPaused) {
      nowPlaying.append("(Paused) ");
    }
    if (audioPlayerIsLooped) {
      nowPlaying.append("(Loop) ");
    }
  }

  /**
   * Converts long duration to conventional readable time.
   *
   * @param longTime duration of the track in long
   * @return readable time format
   */
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