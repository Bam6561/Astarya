package commands.audio.managers;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.Guild;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerManager {
  private static PlayerManager INSTANCE; // For query handling

  private final Map<Long, PlaybackManager> musicManagers;
  private final AudioPlayerManager audioPlayerManager; // Audio capabilities

  public PlayerManager() { // Register audio player with bot
    this.musicManagers = new HashMap<>();
    this.audioPlayerManager = new DefaultAudioPlayerManager();
    AudioSourceManagers.registerRemoteSources(this.audioPlayerManager);
    AudioSourceManagers.registerLocalSource(this.audioPlayerManager);
  }

  // Converts JDA query results to playable Discord audio
  public PlaybackManager getPlaybackManager(Guild guild) {
    return this.musicManagers.computeIfAbsent(guild.getIdLong(), (guildId) -> {
      final PlaybackManager playbackManager = new PlaybackManager(this.audioPlayerManager);
      guild.getAudioManager().setSendingHandler(PlaybackManager.getSendHandler());
      return playbackManager;
    });
  }

  // Query processing
  public void createAudioTrack(CommandEvent ce, String trackURL) {
    final PlaybackManager playbackManager = this.getPlaybackManager(ce.getGuild());
    this.audioPlayerManager.loadItemOrdered(playbackManager, trackURL, new AudioLoadResultHandler() {
      String requester = "[" + ce.getAuthor().getAsTag() + "]";

      @Override
      public void trackLoaded(AudioTrack track) {
        playbackManager.audioScheduler.queue(track);
        playbackManager.audioScheduler.addToRequesterList(requester);
        ce.getChannel().sendMessage("**Added:** `" +
            track.getInfo().title + "` " + requester).queue();
      }

      @Override
      public void playlistLoaded(AudioPlaylist playlist) {
        List<AudioTrack> results = playlist.getTracks();
        if (playlist.isSearchResult()) { // Search query
          playbackManager.audioScheduler.queue(results.get(0));
          playbackManager.audioScheduler.addToRequesterList(requester);
          ce.getChannel().sendMessage("**Added:** `" +
              results.get(0).getInfo().title + "` " + requester).queue();
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

  // Query handler
  public static PlayerManager getINSTANCE() {
    if (INSTANCE == null) {
      INSTANCE = new PlayerManager();
    }
    return INSTANCE;
  }
}
