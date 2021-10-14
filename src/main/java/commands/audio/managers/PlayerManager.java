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

public class PlayerManager {
  private static PlayerManager INSTANCE; // For query handling

  private final Map<Long, PlaybackManager> musicManagers;
  private final AudioPlayerManager audioPlayerManager; // Audio capabilities

  public ArrayList<AudioTrack> searchTrackResults;

  public PlayerManager() { // Register audio player with bot
    this.musicManagers = new HashMap<>();
    this.audioPlayerManager = new DefaultAudioPlayerManager();
    AudioSourceManagers.registerRemoteSources(this.audioPlayerManager);
    AudioSourceManagers.registerLocalSource(this.audioPlayerManager);
    this.searchTrackResults = new ArrayList<>();
  }

  // Converts JDA query results to playable Discord audio
  public PlaybackManager getPlaybackManager(Guild guild) {
    return this.musicManagers.computeIfAbsent(guild.getIdLong(), (guildId) -> {
      final PlaybackManager playbackManager = new PlaybackManager(this.audioPlayerManager);
      guild.getAudioManager().setSendingHandler(PlaybackManager.getSendHandler());
      return playbackManager;
    });
  }

  public void createAudioTrack(CommandEvent ce, String trackURL) { // Play
    final PlaybackManager playbackManager = this.getPlaybackManager(ce.getGuild());
    this.audioPlayerManager.loadItemOrdered(playbackManager, trackURL, new AudioLoadResultHandler() {
      String requester = "[" + ce.getAuthor().getAsTag() + "]";

      @Override
      public void trackLoaded(AudioTrack track) {
        playbackManager.audioScheduler.queue(track);
        playbackManager.audioScheduler.addToRequesterList(requester);
        long trackDurationLong = track.getDuration();
        String trackDuration = floatTimeConversion(trackDurationLong);
        ce.getChannel().sendMessage("**Added:** `" +
            track.getInfo().title + "` {*" + trackDuration + "*} "
            + requester).queue();
      }

      @Override
      public void playlistLoaded(AudioPlaylist playlist) {
        List<AudioTrack> results = playlist.getTracks();
        if (playlist.isSearchResult()) { // Search query
          AudioTrack track = results.get(0);
          playbackManager.audioScheduler.queue(track);
          playbackManager.audioScheduler.addToRequesterList(requester);
          long trackDurationLong = track.getDuration();
          String trackDuration = floatTimeConversion(trackDurationLong);
          ce.getChannel().sendMessage("**Added:** `" +
              results.get(0).getInfo().title + "` {*" + trackDuration + "*} "
              + requester).queue();
        } else { // Playlist
          for (int i = 0; i < playlist.getTracks().size(); i++) {
            playbackManager.audioScheduler.queue(playlist.getTracks().get(i));
            playbackManager.audioScheduler.addToRequesterList(requester);
          }
          ce.getChannel().sendMessage("**Added:** `" + playlist.getTracks().size()
              + "` tracks " + requester).queue();
        }
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

  public void createAudioTrackSilent(CommandEvent ce, String trackURL) { // Play
    final PlaybackManager playbackManager = this.getPlaybackManager(ce.getGuild());
    this.audioPlayerManager.loadItemOrdered(playbackManager, trackURL, new AudioLoadResultHandler() {
      String requester = "[" + ce.getAuthor().getAsTag() + "]";

      @Override
      public void trackLoaded(AudioTrack track) {
        playbackManager.audioScheduler.queue(track);
        playbackManager.audioScheduler.addToRequesterList(requester);
        long trackDurationLong = track.getDuration();
        String trackDuration = floatTimeConversion(trackDurationLong);
      }

      @Override
      public void playlistLoaded(AudioPlaylist playlist) {
        List<AudioTrack> results = playlist.getTracks();
        if (playlist.isSearchResult()) { // Search query
          AudioTrack track = results.get(0);
          playbackManager.audioScheduler.queue(track);
          playbackManager.audioScheduler.addToRequesterList(requester);
          long trackDurationLong = track.getDuration();
          String trackDuration = floatTimeConversion(trackDurationLong);
        } else { // Playlist
          for (int i = 0; i < playlist.getTracks().size(); i++) {
            playbackManager.audioScheduler.queue(playlist.getTracks().get(i));
            playbackManager.audioScheduler.addToRequesterList(requester);
          }
        }
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

  public void searchAudioTrack(CommandEvent ce, String youtubeSearchQuery) { // SearchTrack
    final PlaybackManager playbackManager = this.getPlaybackManager(ce.getGuild());
    this.audioPlayerManager.loadItemOrdered(playbackManager, youtubeSearchQuery, new AudioLoadResultHandler() {
      String requester = "[" + ce.getAuthor().getAsTag() + "]";

      @Override
      public void trackLoaded(AudioTrack track) {
        ce.getChannel().sendMessage("Use the play command to queue tracks.").queue();
      }

      @Override
      public void playlistLoaded(AudioPlaylist playlist) {
        clearSearchTrackResults(); // Clear results from previous search
        List<AudioTrack> searchResults = playlist.getTracks();
        for (int i = 0; i < 5; i++) { // Limit youtube search results to 5
          addSearchTrackResults(searchResults.get(i));
        }
        EmbedBuilder display = new EmbedBuilder();
        StringBuilder searchResultsDisplay = new StringBuilder();
        for (int i = 0; i < 5; i++) { // Queue Entries
          long trackDurationLong = getSearchTrackResults(i).getDuration();
          String trackDuration = floatTimeConversion(trackDurationLong);
          searchResultsDisplay.append("**[").append(i + 1).append("]** `").
              append(getSearchTrackResults(i).getInfo().title)
              .append("` {*").append(trackDuration).append("*}\n");
        }
        display.setTitle("**__Search Results__**");
        display.setDescription(searchResultsDisplay);
        Settings.sendEmbed(ce, display);
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

  private void addSearchTrackResults(AudioTrack audioTrack) {
    this.searchTrackResults.add(audioTrack);
  }

  private AudioTrack getSearchTrackResults(int index) {
    return this.searchTrackResults.get(index);
  }

  private void clearSearchTrackResults() {
    this.searchTrackResults.clear();
  }

  // Query handler
  public static PlayerManager getINSTANCE() {
    if (INSTANCE == null) {
      INSTANCE = new PlayerManager();
    }
    return INSTANCE;
  }
}
