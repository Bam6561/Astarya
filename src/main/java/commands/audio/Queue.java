package commands.audio;

import astarya.BotMessage;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import commands.audio.managers.AudioScheduler;
import commands.audio.managers.PlayerManager;
import commands.audio.objects.TrackQueueIndex;
import commands.audio.utility.TimeConversion;
import commands.owner.Settings;
import net.dv8tion.jda.api.EmbedBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Queue is a command invocation that provides a list
 * of tracks queued and what track is currently playing.
 *
 * @author Danny Nguyen
 * @version 1.7.13
 * @since 1.2.0
 */
public class Queue extends Command {
  private int pageRequested;
  private int numberOfPages;
  private int firstTrackQueueIndexOnPage;

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
   * @param ce command event
   * @throws NumberFormatException user provided non-integer value
   */
  @Override
  protected void execute(CommandEvent ce) {
    Settings.deleteInvoke(ce);

    String[] parameters = ce.getMessage().getContentRaw().split("\\s");
    int numberOfParameters = parameters.length - 1;

    switch (numberOfParameters) {
      case 0 -> interpretQueuePage(ce, 0); // First queue page
      case 1 -> { // Search for queue page
        try {
          interpretQueuePage(ce, Integer.parseInt(parameters[1]) - 1);
        } catch (NumberFormatException e) {
          ce.getChannel().sendMessage(BotMessage.Failure.QUEUE_SPECIFY.text).queue();
        }
      }
      default -> ce.getChannel().sendMessage(BotMessage.Failure.INVALID_NUMBER_OF_PARAMETERS.text).queue();
    }
  }

  /**
   * Either sends an embed containing a track queue page or what track is currently playing.
   *
   * @param ce            command event
   * @param pageRequested user provided page number to be displayed
   */
  public void interpretQueuePage(CommandEvent ce, int pageRequested) {
    AudioScheduler audioScheduler = PlayerManager.getINSTANCE().getPlaybackManager(ce.getGuild()).audioScheduler;
    AudioPlayer audioPlayer = audioScheduler.getAudioPlayer();

    List<TrackQueueIndex> trackQueue = audioScheduler.getTrackQueue();

    if (!trackQueue.isEmpty()) {
      calculatePageToDisplay(trackQueue.size(), pageRequested);
      sendQueuePage(ce, audioScheduler, audioPlayer, trackQueue);
    } else { // Display nowPlaying only
      sendNowPlaying(ce, audioScheduler, audioPlayer);
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
    setFirstTrackQueueIndexOnPage(firstQueueIndexOnPage);
  }

  /**
   * Sends the track queue page and the audio player's currently playing track.
   *
   * @param ce             command event
   * @param audioScheduler bot's audio scheduler
   * @param audioPlayer    bot's audio player
   * @param trackQueue     arraylist containing the tracks
   */
  private void sendQueuePage(CommandEvent ce, AudioScheduler audioScheduler,
                             AudioPlayer audioPlayer, List<TrackQueueIndex> trackQueue) {
    EmbedBuilder display = new EmbedBuilder();
    display.setAuthor("Queue");
    display.setDescription(createNowPlayingQueuePage(audioScheduler, audioPlayer));
    display.addField("**Tracks:**", createQueuePage(trackQueue), false);
    Settings.sendEmbed(ce, display);
  }

  /**
   * Sends the currently playing track.
   *
   * @param ce             command event
   * @param audioScheduler the bot's audio scheduler
   * @param audioPlayer    the bot's audio player
   */
  private void sendNowPlaying(CommandEvent ce, AudioScheduler audioScheduler, AudioPlayer audioPlayer) {
    StringBuilder nowPlaying = new StringBuilder();

    boolean currentlyPlayingTrack = !(audioPlayer.getPlayingTrack() == null);
    if (currentlyPlayingTrack) {
      AudioTrack audioTrack = audioPlayer.getPlayingTrack();
      String trackPosition = TimeConversion.convert(audioTrack.getPosition());
      String trackDuration = TimeConversion.convert(audioTrack.getDuration());

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
   * Builds the nowPlaying component to the track queue page.
   *
   * @param audioScheduler bot's audio scheduler
   * @param audioPlayer    bot's audio player
   * @return nowPlaying component to track queue page
   */
  private String createNowPlayingQueuePage(AudioScheduler audioScheduler, AudioPlayer audioPlayer) {
    StringBuilder nowPlaying = new StringBuilder();
    nowPlaying.append("**Now Playing:** ");
    audioPlayerIsPausedOrLoopedNotice(audioScheduler, audioPlayer, nowPlaying);

    boolean currentlyPlayingTrack = !(audioPlayer.getPlayingTrack() == null);
    if (currentlyPlayingTrack) {
      AudioTrack audioTrack = audioPlayer.getPlayingTrack();
      String trackPosition = TimeConversion.convert(audioTrack.getPosition());
      String trackDuration = TimeConversion.convert(audioTrack.getDuration());

      nowPlaying.append("`").append(audioTrack.getInfo().title).
          append("` {*").append(trackPosition).append("*-*").
          append(trackDuration).append("*}\nPage `").append(getPageRequested() + 1).
          append("` / `").append(getNumberOfPages()).append("`");
    } else {
      nowPlaying.append("`").append("Nothing")
          .append("`\nPage `").append(getPageRequested() + 1).
          append("` / `").append(getNumberOfPages()).append("`");
    }
    return nowPlaying.toString();
  }

  /**
   * Populates a queue page with track entries.
   * <p>
   * For partially filled pages, calculate which comes first - the
   * next ten indices or the last track entry  in the track queue.
   * </p>
   *
   * @param trackQueue tracks in the queue
   * @return formatted text representing the tracks queue
   */
  private String createQueuePage(List<TrackQueueIndex> trackQueue) {
    StringBuilder queuePage = new StringBuilder();

    // Calculate last track entry to be displayed
    int numberOfTracksInQueue = trackQueue.size();
    int lastQueueIndexOnPage = Math.min((getFirstTrackQueueIndexOnPage() + 10), numberOfTracksInQueue);

    // Build contents of queue page embed
    for (int i = firstTrackQueueIndexOnPage; i < lastQueueIndexOnPage; i++) {
      String trackDuration = TimeConversion.convert(trackQueue.get(i).getAudioTrack().getDuration());
      queuePage.append("**[").append(i + 1).append("]** `").
          append(trackQueue.get(i).getAudioTrack().getInfo().title)
          .append("` {*").append(trackDuration).append("*} ").
          append(trackQueue.get(i).getRequester()).append("\n");
    }
    return queuePage.toString();
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
    boolean audioPlayerIsLooped = audioScheduler.getAudioPlayerLooped();
    if (audioPlayerIsPaused) {
      nowPlaying.append("(Paused) ");
    }
    if (audioPlayerIsLooped) {
      nowPlaying.append("(Loop) ");
    }
  }

  private int getPageRequested() {
    return this.pageRequested;
  }

  private int getNumberOfPages() {
    return this.numberOfPages;
  }

  private int getFirstTrackQueueIndexOnPage() {
    return this.firstTrackQueueIndexOnPage;
  }

  private void setPageRequested(int pageRequested) {
    this.pageRequested = pageRequested;
  }

  private void setNumberOfPages(int numberOfPages) {
    this.numberOfPages = numberOfPages;
  }

  private void setFirstTrackQueueIndexOnPage(int firstTrackQueueIndexOnPage) {
    this.firstTrackQueueIndexOnPage = firstTrackQueueIndexOnPage;
  }
}