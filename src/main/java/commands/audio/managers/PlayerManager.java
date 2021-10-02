package commands.audio.managers;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.HashMap;
import java.util.Map;

public class PlayerManager {
  private static PlayerManager INSTANCE;

  private final Map<Long, PlaybackManager> musicManager;
  private final AudioPlayerManager audioPlayerManager;

  public PlayerManager() {
    this.musicManager = new HashMap<>();
    this.audioPlayerManager = new DefaultAudioPlayerManager();

    AudioSourceManagers.registerRemoteSources(audioPlayerManager);
    AudioSourceManagers.registerLocalSource(audioPlayerManager);
  }

  public PlaybackManager getPlaybackManager(Guild guild) {
    return this.musicManager.computeIfAbsent(guild.getIdLong(), (guildId) -> {
      final PlaybackManager playbackManager = new PlaybackManager(this.audioPlayerManager);

      guild.getAudioManager().setSendingHandler(PlaybackManager.getSendHandler());
      return playbackManager;
    });
  }

  public void createAudioTrack(TextChannel channel, String trackURL) {
    final PlaybackManager playbackManager = this.getPlaybackManager(channel.getGuild());
    this.audioPlayerManager.loadItemOrdered(playbackManager, trackURL, new AudioLoadResultHandler() {
      @Override
      public void trackLoaded(AudioTrack track) {
        System.out.println("TRACK LOADED.");
        playbackManager.audioScheduler.queue(track);
      }

      @Override
      public void playlistLoaded(AudioPlaylist playlist) {
        for (AudioTrack track : playlist.getTracks()) {
          playbackManager.audioScheduler.queue(track);
        }
      }

      @Override
      public void noMatches() {
      }

      @Override
      public void loadFailed(FriendlyException throwable) {
      }
    });
  }

  public static PlayerManager getINSTANCE() {
    if (INSTANCE == null) {
      INSTANCE = new PlayerManager();
    }
    return INSTANCE;
  }
}
