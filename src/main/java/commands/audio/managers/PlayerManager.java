package commands.audio.managers;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import commands.owner.Settings;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * PlayerManager is a LavaPlayer component that converts files and
 * search queries into playable tracks for the AudioScheduler.
 *
 * @author Danny Nguyen
 * @version 1.7.0
 * @since 1.1.0
 */
public class PlayerManager {
  private static PlayerManager INSTANCE;
  private final Map<Long, PlaybackManager> musicManagers;
  private final AudioPlayerManager audioPlayerManager;
  private ArrayList<AudioTrack> searchTrackResults;

  // Registers audio player with bot application
  public PlayerManager() {
    this.musicManagers = new HashMap<>();
    this.audioPlayerManager = new DefaultAudioPlayerManager();
    AudioSourceManagers.registerRemoteSources(this.audioPlayerManager);
    AudioSourceManagers.registerLocalSource(this.audioPlayerManager);
    this.searchTrackResults = new ArrayList<>();
  }

  /**
   * Creates an AudioTrack object based on user provided parameters
   * after the play command request to put in the track queue.
   * <p>
   * YouTube video links and media files are handled by the trackLoaded method.
   * YouTube playlist links and search queries are handled by the playlistLoaded method.
   * </p>
   *
   * @param ce       object containing information about the command event
   * @param trackURL either a direct url link to the track(s) requested or a YouTube search query
   * @param isSilent whether to send a confirmation in the text channel
   */
  public void createAudioTrack(CommandEvent ce, String trackURL, boolean isSilent) {
    final PlaybackManager playbackManager = this.getPlaybackManager(ce.getGuild());
    AudioScheduler audioScheduler = playbackManager.audioScheduler;
    this.audioPlayerManager.loadItemOrdered(playbackManager, trackURL, new AudioLoadResultHandler() {
      @Override
      public void trackLoaded(AudioTrack track) {
        handleYouTubeVideoLinksAndMediaFiles(ce, audioScheduler, track, isSilent);
      }

      @Override
      public void playlistLoaded(AudioPlaylist trackPlaylist) {
        if (trackPlaylist.isSearchResult()) {
          handleYouTubeSearchQueries(ce, trackPlaylist, audioScheduler, isSilent);
        } else {
          handleYouTubePlaylistLinks(ce, trackPlaylist, audioScheduler, isSilent);
        }
      }

      @Override
      public void noMatches() {
        if (!isSilent) ce.getChannel().sendMessage("Unable to find track.").queue();
      }

      @Override
      public void loadFailed(FriendlyException throwable) {
        if (!isSilent) ce.getChannel().sendMessage("Unable to load track.").queue();
      }
    });
  }

  /**
   * Adds a YouTube video or media file into the track queue.
   *
   * @param ce             object containing information about the command event
   * @param audioScheduler bot's audio scheduler
   * @param track          track to be added into the queue
   * @param isSilent       whether to send a confirmation in the text channel
   */
  private void handleYouTubeVideoLinksAndMediaFiles(CommandEvent ce, AudioScheduler audioScheduler,
                                                    AudioTrack track, boolean isSilent) {
    String requester = "[" + ce.getAuthor().getAsTag() + "]";
    addTrackToQueue(audioScheduler, track, requester);
    if (!isSilent) {
      ce.getChannel().sendMessage("**Added:** `" + track.getInfo().title + "` {*"
          + longTimeConversion(track.getDuration()) + "*} " + requester).queue();
    }
  }

  /**
   * Adds the first match from a YouTube search query into the track queue.
   *
   * @param ce             object containing information about the command event
   * @param trackPlaylist  list of tracks generated from the search query
   * @param audioScheduler bot's audio scheduler
   * @param isSilent       whether to send a confirmation in the text channel
   */
  private void handleYouTubeSearchQueries(CommandEvent ce, AudioPlaylist trackPlaylist,
                                          AudioScheduler audioScheduler, boolean isSilent) {
    List<AudioTrack> searchResults = trackPlaylist.getTracks();
    AudioTrack track = searchResults.get(0);
    String requester = "[" + ce.getAuthor().getAsTag() + "]";
    addTrackToQueue(audioScheduler, track, requester);
    if (!isSilent) {
      ce.getChannel().sendMessage("**Added:** `" + searchResults.get(0).getInfo().title + "` {*"
          + longTimeConversion(track.getDuration()) + "*} " + requester).queue();
    }
  }

  /**
   * Adds each YouTube video from the playlist into the track queue.
   *
   * @param ce             object containing information about the command event
   * @param trackPlaylist  list of tracks retrieved from the playlist
   * @param audioScheduler bot's audio scheduler
   * @param isSilent       whether to send a confirmation in the text channel
   */
  private void handleYouTubePlaylistLinks(CommandEvent ce, AudioPlaylist trackPlaylist,
                                          AudioScheduler audioScheduler, boolean isSilent) {
    String requester = "[" + ce.getAuthor().getAsTag() + "]";
    for (int i = 0; i < trackPlaylist.getTracks().size(); i++) {
      addTrackToQueue(audioScheduler, trackPlaylist.getTracks().get(i), requester);
    }
    if (!isSilent) {
      ce.getChannel().sendMessage("**Added:** `" + trackPlaylist.getTracks().size()
          + "` tracks " + requester).queue();
    }
  }

  /**
   * Adds the track and the requester to their respective lists.
   *
   * @param audioScheduler bot's audio scheduler
   * @param track          track to be queued
   * @param requester      user who queued the track
   */
  private void addTrackToQueue(AudioScheduler audioScheduler,
                               AudioTrack track, String requester) {
    audioScheduler.queue(track, requester);
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

  /**
   * Used in conjunction with the SearchTrack command request, this method displays tracks from a
   * YouTube search query and adds them into an ArrayList for the user to later choose from to queue.
   *
   * @param ce                 object that contains information about the command event
   * @param youtubeSearchQuery youtube search query
   */
  public void searchAudioTrack(CommandEvent ce, String youtubeSearchQuery) {
    final PlaybackManager playbackManager = this.getPlaybackManager(ce.getGuild());
    this.audioPlayerManager.loadItemOrdered(playbackManager, youtubeSearchQuery, new AudioLoadResultHandler() {
      @Override
      public void trackLoaded(AudioTrack track) {
        ce.getChannel().sendMessage("Use the play command to queue tracks.").queue();
      }

      @Override
      public void playlistLoaded(AudioPlaylist playlist) {
        clearSearchTrackResults(); // Clear results from the previous search
        handleSearchTrackResults(ce, playlist);
      }

      @Override
      public void noMatches() {
        ce.getChannel().sendMessage("Unable to find track.").queue();
      }

      @Override
      public void loadFailed(FriendlyException throwable) {
        ce.getChannel().sendMessage("Unable to load track.").queue();
      }
    });
  }

  /**
   * Displays search results from a YouTube search query, limits the results to
   * the first 5, and sends an embed containing information about the search results.
   *
   * @param ce       object containing information about the command event
   * @param playlist YouTube search results
   */
  private void handleSearchTrackResults(CommandEvent ce, AudioPlaylist playlist) {
    // Limit YouTube search results to 5 tracks
    List<AudioTrack> searchResults = playlist.getTracks();
    for (int i = 0; i < 5; i++) {
      addSearchTrackResults(searchResults.get(i));
    }

    // Build search result's embed contents
    StringBuilder searchResultsDisplay = new StringBuilder();
    for (int i = 0; i < 5; i++) {
      long trackDurationLong = getTrackFromSearchResults(i).getDuration();
      String trackDuration = longTimeConversion(trackDurationLong);
      searchResultsDisplay.append("**[").append(i + 1).append("]** `").
          append(getTrackFromSearchResults(i).getInfo().title)
          .append("` {*").append(trackDuration).append("*}\n");
    }

    EmbedBuilder display = new EmbedBuilder();
    display.setTitle("**__Search Results__**");
    display.setDescription(searchResultsDisplay);
    Settings.sendEmbed(ce, display);
  }

  private void clearSearchTrackResults() {
    this.searchTrackResults.clear();
  }

  private void addSearchTrackResults(AudioTrack audioTrack) {
    this.searchTrackResults.add(audioTrack);
  }

  private AudioTrack getTrackFromSearchResults(int index) {
    return this.searchTrackResults.get(index);
  }

  public ArrayList<AudioTrack> getSearchTrackResults() {
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

  public static PlayerManager getINSTANCE() {
    if (INSTANCE == null) {
      INSTANCE = new PlayerManager();
    }
    return INSTANCE;
  }
}
