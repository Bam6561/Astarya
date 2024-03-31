package me.dannynguyen.astarya.commands.audio;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import me.dannynguyen.astarya.commands.audio.managers.AudioScheduler;
import me.dannynguyen.astarya.commands.audio.managers.PlayerManager;
import me.dannynguyen.astarya.commands.owner.Settings;
import me.dannynguyen.astarya.enums.BotMessage;
import net.dv8tion.jda.api.EmbedBuilder;

import java.util.List;

/**
 * Command invocation that provides a list of tracks queued and what track is currently playing.
 *
 * @author Danny Nguyen
 * @version 1.8.12
 * @since 1.2.0
 */
public class Queue extends Command {
  /**
   * Associates the command with its properties.
   */
  public Queue() {
    this.name = "queue";
    this.aliases = new String[]{"queue", "q"};
    this.arguments = "[0]Queue [1]PageNumber";
    this.help = "Provides a list of tracks queued.";
  }

  /**
   * Either sends an embed containing information about the
   * {@link AudioScheduler#getTrackQueue() queue} with 10 results
   * on each page or what track is currently playing if nothing is queued.
   * <p>
   * Users can optionally provide a queue page to be displayed with an additional parameter.
   *
   * @param ce command event
   */
  @Override
  protected void execute(CommandEvent ce) {
    Settings.deleteInvoke(ce);

    String[] parameters = ce.getMessage().getContentRaw().split("\\s");
    int numberOfParameters = parameters.length - 1;

    switch (numberOfParameters) {
      case 0 -> new QueueRequest(ce, 0).interpretQueuePage(); // First queue page
      case 1 -> { // Search for queue page
        try {
          new QueueRequest(ce, Integer.parseInt(parameters[1]) - 1).interpretQueuePage();
        } catch (NumberFormatException e) {
          ce.getChannel().sendMessage("Provide queue page number.").queue();
        }
      }
      default -> ce.getChannel().sendMessage(BotMessage.INVALID_NUMBER_OF_PARAMETERS.getMessage()).queue();
    }
  }

  /**
   * Represents a track queue query.
   *
   * @author Danny Nguyen
   * @version 1.8.12
   * @since 1.8.12
   */
  private static class QueueRequest {
    /**
     * Command event.
     */
    private final CommandEvent ce;

    /**
     * Page requested.
     * <p>
     * Will be set on constructor and validated in {@link #setQueueRequest(int)}.
     */
    private int pageRequested;

    /**
     * Number of queue pages.
     * <p>
     * Set in {@link #setQueueRequest(int)}.
     */
    private int numberOfPages;

    /**
     * First queue index on page.
     * <p>
     * Set in {@link #setQueueRequest(int)}.
     */
    private int firstTrackQueueIndexOnPage;

    /**
     * {@link AudioScheduler}
     */
    private final AudioScheduler audioScheduler;

    /**
     * Audio player.
     */
    private final AudioPlayer audioPlayer;

    /**
     * {@link AudioScheduler#getTrackQueue() queue}
     */
    private final List<TrackQueueIndex> trackQueue;

    /**
     * Associates a queue request with its command event and page requested.
     *
     * @param ce            command event
     * @param pageRequested page requested
     */
    QueueRequest(CommandEvent ce, int pageRequested) {
      this.ce = ce;
      this.pageRequested = pageRequested;
      this.audioScheduler = PlayerManager.getINSTANCE().getPlaybackManager(ce.getGuild()).audioScheduler;
      this.audioPlayer = audioScheduler.getAudioPlayer();
      this.trackQueue = audioScheduler.getTrackQueue();
    }

    /**
     * Either sends an embed containing a queue page or what track is currently playing.
     */
    private void interpretQueuePage() {
      if (!trackQueue.isEmpty()) {
        setQueueRequest(trackQueue.size());
        sendQueuePage();
      } else { // Display nowPlaying only
        sendNowPlaying();
      }
    }

    /**
     * Corrects user's requested page to view and determines which
     * queue page to display and its starting track entry.
     * <p>
     * Full pages show 10 tracks each and a partially filled page adds one to the total number of pages.
     * <p>
     * Multiply page requested by 10 to find the track index to display first, and subtract
     * by a page (10 tracks) to display partially filled pages if the page would be empty.
     * <p>
     * If the user requests more than the total number of pages, then set the request to the last page.
     * <p>
     * For negative page number requests, set the request to 0.
     *
     * @param numberOfTracksInQueue total number of tracks in queue
     */
    private void setQueueRequest(int numberOfTracksInQueue) {
      // Partition the queue into pages
      numberOfPages = numberOfTracksInQueue / 10;
      boolean partiallyFilledPage = (numberOfTracksInQueue % 10) > 0;
      if (partiallyFilledPage) {
        numberOfPages += 1;
      }

      // Correct invalid user requests
      if (pageRequested >= numberOfPages) {
        pageRequested = numberOfPages - 1;
      } else if (pageRequested < 0) {
        pageRequested = 0;
      }

      // Decide what the first track entry to be displayed on the page is
      firstTrackQueueIndexOnPage = pageRequested * 10;
      if (firstTrackQueueIndexOnPage == numberOfTracksInQueue) {
        firstTrackQueueIndexOnPage -= 10;
      }
    }

    /**
     * Sends the {@link AudioScheduler#getTrackQueue() queue} page and the audio player's currently playing track.
     */
    private void sendQueuePage() {
      EmbedBuilder embed = new EmbedBuilder();
      embed.setAuthor("Queue");
      embed.setDescription(createNowPlayingComponent());
      embed.addField("**Tracks:**", createQueuePage(), false);
      Settings.sendEmbed(ce, embed);
    }

    /**
     * Sends the currently playing track.
     */
    private void sendNowPlaying() {
      StringBuilder nowPlaying = new StringBuilder();

      if (audioPlayer.getPlayingTrack() != null) {
        AudioTrack audioTrack = audioPlayer.getPlayingTrack();
        String trackPosition = TrackTime.convertLong(audioTrack.getPosition());
        String trackDuration = TrackTime.convertLong(audioTrack.getDuration());

        nowPlaying.append("**Now Playing:** ");
        addAudioPlayerIsPausedOrLoopedComponent(nowPlaying);
        nowPlaying.append("`").append(audioTrack.getInfo().title).append("` {*").append(trackPosition).append("*-*").append(trackDuration).append("*}");
      } else {
        nowPlaying.append("**Now Playing:** `Nothing`");
      }
      ce.getChannel().sendMessage(nowPlaying).queue();
    }

    /**
     * Builds the now playing component to the {@link AudioScheduler#getTrackQueue() queue} page.
     *
     * @return now playing component to queue page
     */
    private String createNowPlayingComponent() {
      StringBuilder nowPlaying = new StringBuilder();
      nowPlaying.append("**Now Playing:** ");
      addAudioPlayerIsPausedOrLoopedComponent(nowPlaying);

      if (audioPlayer.getPlayingTrack() != null) {
        AudioTrack audioTrack = audioPlayer.getPlayingTrack();
        String trackPosition = TrackTime.convertLong(audioTrack.getPosition());
        String trackDuration = TrackTime.convertLong(audioTrack.getDuration());

        nowPlaying.append("`").append(audioTrack.getInfo().title)
            .append("` {*").append(trackPosition).append("*-*")
            .append(trackDuration).append("*}\nPage `").append(pageRequested + 1)
            .append("` / `").append(numberOfPages).append("`");
      } else {
        nowPlaying.append("`").append("Nothing")
            .append("`\nPage `").append(pageRequested + 1)
            .append("` / `").append(numberOfPages).append("`");
      }
      return nowPlaying.toString();
    }

    /**
     * Populates a queue page with track entries.
     * <p>
     * For partially filled pages, calculate which comes first - the next ten
     * indices or the last track entry in the {@link AudioScheduler#getTrackQueue() queue}.
     *
     * @return formatted text representing the tracks queue
     */
    private String createQueuePage() {
      StringBuilder queuePage = new StringBuilder();

      // Calculate last track entry to be displayed
      int numberOfTracksInQueue = trackQueue.size();
      int lastQueueIndexOnPage = Math.min((firstTrackQueueIndexOnPage + 10), numberOfTracksInQueue);

      // Build contents of queue page embed
      for (int i = firstTrackQueueIndexOnPage; i < lastQueueIndexOnPage; i++) {
        String trackDuration = TrackTime.convertLong(trackQueue.get(i).getAudioTrack().getDuration());
        queuePage.append("**[").append(i + 1).append("]** `")
            .append(trackQueue.get(i).getAudioTrack().getInfo().title)
            .append("` {*").append(trackDuration).append("*} ")
            .append(trackQueue.get(i).getRequester()).append("\n");
      }
      return queuePage.toString();
    }

    /**
     * Adds conditional setting notes for the now playing section.
     *
     * @param nowPlaying information about what track is currently playing
     */
    private void addAudioPlayerIsPausedOrLoopedComponent(StringBuilder nowPlaying) {
      if (audioPlayer.isPaused()) {
        nowPlaying.append("(Paused) ");
      }
      if (audioScheduler.getAudioPlayerLooped()) {
        nowPlaying.append("(Loop) ");
      }
    }
  }
}