package me.dannynguyen.astarya.commands.audio.managers;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import me.dannynguyen.astarya.commands.audio.utility.TrackTime;
import me.dannynguyen.astarya.commands.owner.Settings;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a LavaPlayer component that converts files and
 * search queries into playable tracks for the {@link AudioScheduler}.
 *
 * @author Danny Nguyen
 * @version 1.8.7
 * @since 1.1.0
 */
public class PlayerManager {
  /**
   * Instance of the player manager.
   */
  private static PlayerManager INSTANCE;

  /**
   * {@link PlaybackManager}
   */
  private final Map<Long, PlaybackManager> musicManagers;

  /**
   * Audio player manager.
   */
  private final AudioPlayerManager audioPlayerManager;

  /**
   * Search results from {@link me.dannynguyen.astarya.commands.audio.SearchTrack}.
   */
  private final AudioTrack[] searchTrackResults;

  /**
   * Associates player manager with its music managers, audio
   * player managers, audio source managers, and search track results.
   */
  public PlayerManager() {
    this.musicManagers = new HashMap<>();
    this.audioPlayerManager = new DefaultAudioPlayerManager();
    AudioSourceManagers.registerRemoteSources(this.audioPlayerManager);
    AudioSourceManagers.registerLocalSource(this.audioPlayerManager);
    this.searchTrackResults = new AudioTrack[5];
  }

  /**
   * Creates an AudioTrack based on user provided parameters
   * after the play command request to put in the queue.
   * <p>
   * YouTube video links and media files are handled by the trackLoaded method.
   * <p>
   * YouTube playlist links and search queries are handled by the playlistLoaded method.
   *
   * @param ce       command event
   * @param trackUrl either a direct url link to the track(s) requested or a YouTube search query
   * @param isSilent if to send a confirmation in the text channel
   */
  public void createAudioTrack(CommandEvent ce, String trackUrl, boolean isSilent) {
    final PlaybackManager playbackManager = this.getPlaybackManager(ce.getGuild());
    AudioScheduler audioScheduler = playbackManager.audioScheduler;
    this.audioPlayerManager.loadItemOrdered(playbackManager, trackUrl, new AudioLoadResultHandler() {
      @Override
      public void trackLoaded(AudioTrack track) {
        processYouTubeLinksAndMediaFiles(ce, audioScheduler, track, isSilent);
      }

      @Override
      public void playlistLoaded(AudioPlaylist trackPlaylist) {
        if (trackPlaylist.isSearchResult()) {
          processYouTubeSearchQueries(ce, trackPlaylist, audioScheduler, isSilent);
        } else {
          processYouTubePlaylistLinks(ce, trackPlaylist, audioScheduler, isSilent);
        }
      }

      @Override
      public void noMatches() {
        if (!isSilent) {
          ce.getChannel().sendMessage(Failure.UNABLE_TO_FIND_TRACK.getMessage()).queue();
        }
      }

      @Override
      public void loadFailed(FriendlyException throwable) {
        if (!isSilent) {
          ce.getChannel().sendMessage(Failure.UNABLE_TO_LOAD_TRACK.getMessage()).queue();
        }
      }
    });
  }

  /**
   * Adds a YouTube video or media file into the queue.
   *
   * @param ce             command event
   * @param audioScheduler {@link AudioScheduler}
   * @param track          track to be added into the queue
   * @param isSilent       if to send a confirmation in the text channel
   */
  private void processYouTubeLinksAndMediaFiles(CommandEvent ce, AudioScheduler audioScheduler, AudioTrack track, boolean isSilent) {
    String requester = "[" + ce.getAuthor().getAsTag() + "]";
    audioScheduler.queue(track, requester);
    if (!isSilent) {
      ce.getChannel().sendMessage("**Added:** `" + track.getInfo().title + "` {*" + TrackTime.convertLong(track.getDuration()) + "*} " + requester).queue();
    }
  }

  /**
   * Adds the first match from a YouTube search query into the queue.
   *
   * @param ce             command event
   * @param trackPlaylist  tracks from the search query
   * @param audioScheduler {@link AudioScheduler}
   * @param isSilent       if to send a confirmation in the text channel
   */
  private void processYouTubeSearchQueries(CommandEvent ce, AudioPlaylist trackPlaylist, AudioScheduler audioScheduler, boolean isSilent) {
    List<AudioTrack> searchResults = trackPlaylist.getTracks();
    AudioTrack track = searchResults.get(0);
    String requester = "[" + ce.getAuthor().getAsTag() + "]";
    audioScheduler.queue(track, requester);
    if (!isSilent) {
      ce.getChannel().sendMessage("**Added:** `" + searchResults.get(0).getInfo().title + "` {*" + TrackTime.convertLong(track.getDuration()) + "*} " + requester).queue();
    }
  }

  /**
   * Adds each YouTube video from the playlist into the queue.
   *
   * @param ce             command event
   * @param trackPlaylist  tracks retrieved from playlist
   * @param audioScheduler {@link AudioScheduler}
   * @param isSilent       if to send a confirmation in the text channel
   */
  private void processYouTubePlaylistLinks(CommandEvent ce, AudioPlaylist trackPlaylist, AudioScheduler audioScheduler, boolean isSilent) {
    String requester = "[" + ce.getAuthor().getAsTag() + "]";
    for (int i = 0; i < trackPlaylist.getTracks().size(); i++) {
      audioScheduler.queue(trackPlaylist.getTracks().get(i), requester);
    }
    if (!isSilent) {
      ce.getChannel().sendMessage("**Added:** `" + trackPlaylist.getTracks().size() + "` tracks " + requester).queue();
    }
  }

  /**
   * Used in conjunction with {@link me.dannynguyen.astarya.commands.audio.SearchTrack},
   * this method displays tracks from a YouTube search query and
   * adds them into a list for the user to later choose from to queue.
   *
   * @param ce                 command event
   * @param youtubeSearchQuery youtube search query
   */
  public void searchAudioTrack(CommandEvent ce, String youtubeSearchQuery) {
    final PlaybackManager playbackManager = this.getPlaybackManager(ce.getGuild());
    this.audioPlayerManager.loadItemOrdered(playbackManager, youtubeSearchQuery, new AudioLoadResultHandler() {
      @Override
      public void trackLoaded(AudioTrack track) {
        ce.getChannel().sendMessage("Use play command to queue tracks.").queue();
      }

      @Override
      public void playlistLoaded(AudioPlaylist playlist) {
        processSearchTrackResults(ce, playlist);
      }

      @Override
      public void noMatches() {
        ce.getChannel().sendMessage(Failure.UNABLE_TO_FIND_TRACK.getMessage()).queue();
      }

      @Override
      public void loadFailed(FriendlyException throwable) {
        ce.getChannel().sendMessage(Failure.UNABLE_TO_LOAD_TRACK.getMessage()).queue();
      }
    });
  }

  /**
   * Displays search results from a YouTube search query, limits the results to
   * the first 5, and sends an embed containing information about the search results.
   *
   * @param ce       command event
   * @param playlist YouTube search results
   */
  private void processSearchTrackResults(CommandEvent ce, AudioPlaylist playlist) {
    List<AudioTrack> searchResults = playlist.getTracks();
    for (int i = 0; i < 5; i++) {
      searchTrackResults[i] = searchResults.get(i);
    }

    StringBuilder searchResultsDisplay = new StringBuilder();
    for (int i = 0; i < 5; i++) {
      long trackDurationLong = searchTrackResults[i].getDuration();
      String trackDuration = TrackTime.convertLong(trackDurationLong);
      searchResultsDisplay.append("**[").append(i + 1).append("]** `").append(searchTrackResults[i].getInfo().title).append("` {*").append(trackDuration).append("*}\n");
    }

    EmbedBuilder embed = new EmbedBuilder();
    embed.setTitle("**__Search Results__**");
    embed.setDescription(searchResultsDisplay);
    Settings.sendEmbed(ce, embed);
  }

  /**
   * Gets search track results.
   *
   * @return search track results
   */
  public AudioTrack[] getSearchTrackResults() {
    return this.searchTrackResults;
  }

  /**
   * Returns object that allows for conversion of track
   * query results into playable audio in a voice channel.
   *
   * @param guild Discord server the bot is in
   * @return playbackManager as an object
   */
  public PlaybackManager getPlaybackManager(Guild guild) {
    return this.musicManagers.computeIfAbsent(guild.getIdLong(), (guildId) -> {
      final PlaybackManager playbackManager = new PlaybackManager(this.audioPlayerManager);
      guild.getAudioManager().setSendingHandler(PlaybackManager.getSendHandler());
      return playbackManager;
    });
  }

  /**
   * Gets an instance of the player manager.
   *
   * @return instance of the player manager
   */
  public static PlayerManager getINSTANCE() {
    if (INSTANCE == null) {
      INSTANCE = new PlayerManager();
    }
    return INSTANCE;
  }

  /**
   * Types of track parsing failures.
   */
  private enum Failure {
    /**
     * Unable to find track.
     */
    UNABLE_TO_FIND_TRACK("Unable to find track."),

    /**
     * Unable to load track.
     */
    UNABLE_TO_LOAD_TRACK("Unable to load track.");

    /**
     * Message.
     */
    private final String message;

    /**
     * Associates a failure with its message.
     *
     * @param message message
     */
    Failure(String message) {
      this.message = message;
    }

    /**
     * Gets the failure's message.
     *
     * @return failure's message
     */
    public String getMessage() {
      return this.message;
    }
  }
}
