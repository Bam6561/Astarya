package commands.audio;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import commands.audio.managers.PlayerManager;
import commands.owner.Settings;
import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.managers.AudioManager;
import org.apache.hc.core5.http.ParseException;
import org.json.JSONArray;
import org.json.JSONObject;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.credentials.ClientCredentials;
import se.michaelthelin.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;
import se.michaelthelin.spotify.requests.data.albums.GetAlbumsTracksRequest;
import se.michaelthelin.spotify.requests.data.playlists.GetPlaylistRequest;
import se.michaelthelin.spotify.requests.data.tracks.GetTrackRequest;

import java.io.IOException;

/**
 * Play is a command invocation that adds a track to the queue.
 * <p>
 * By default, the play command supports YouTube videos, playlists, and most media files
 * posted in Discord chat. By adding a Spotify API key, the command will also be able
 * to support queueing Spotify songs, playlists, and album links.
 * <p>
 * For queueing Spotify playlists, only the first 100 songs in the playlist
 * are queued due to Spotify API limits. For queueing Spotify albums, only
 * the first 50 songs in the album are queued due to Spotify's API limits.
 * </p>
 *
 * @author Danny Nguyen
 * @version 1.6.1
 * @since 1.1.0
 */
public class Play extends Command {

  public Play() {
    this.name = "play";
    this.aliases = new String[]{"play", "p"};
    this.help = "Adds a track to the queue.";
    this.arguments = ("[1]URL [2++]YouTubeQuery");
  }

  /**
   * Determines whether the user is in the same voice channel as the bot to process a play command request.
   * If the bot is not currently in any voice channel, then attempt to join the same one as the user.
   *
   * @param ce object containing information about the command event
   */
  @Override
  protected void execute(CommandEvent ce) {
    Settings.deleteInvoke(ce);

    GuildVoiceState userVoiceState = ce.getMember().getVoiceState();
    GuildVoiceState botVoiceState = ce.getGuild().getSelfMember().getVoiceState();

    boolean botNotAlreadyInVoiceChannel = !botVoiceState.inAudioChannel();
    try {
      if (botNotAlreadyInVoiceChannel) {
        joinVoiceChannel(ce);
        parsePlayRequest(ce);
      } else {
        boolean userInSameVoiceChannel = userVoiceState.getChannel().equals(botVoiceState.getChannel());
        if (userInSameVoiceChannel) {
          parsePlayRequest(ce);
        } else {
          ce.getChannel().sendMessage("User not in the same voice channel.").queue();
        }
      }
    } catch (NullPointerException e) {
      ce.getChannel().sendMessage("User not in a voice channel.").queue();
    }
  }

  /**
   * Attempts to connect the bot to the same voice channel as the user.
   *
   * @param ce object that contains information about the command event
   */
  private void joinVoiceChannel(CommandEvent ce) {
    AudioChannel audioChannel = ce.getMember().getVoiceState().getChannel();
    AudioManager audioManager = ce.getGuild().getAudioManager();

    try {
      audioManager.openAudioConnection(audioChannel);
      ce.getChannel().sendMessage("Connected to <#" + audioChannel.getId() + ">").queue();
    } catch (Exception e) { // Insufficient permissions
      ce.getChannel().sendMessage("Unable to join <#" + audioChannel.getId() + ">").queue();
    }
  }

  /**
   * Either adds Spotify tracks and Discord media files or a YouTube video
   * using user provided arguments for the search query to the track queue.
   *
   * @param ce object containing information about the command event
   */
  private void parsePlayRequest(CommandEvent ce) {
    // Parse message for arguments
    String[] arguments = ce.getMessage().getContentRaw().split("\\s");
    int numberOfArguments = arguments.length - 1;

    switch (numberOfArguments) {
      case 0 -> ce.getChannel().sendMessage("Invalid number of arguments.").queue();
      case 1 -> handleSpotifyAndMediaLinks(ce, arguments);
      default -> handleYouTubeSearchQuery(ce, arguments, numberOfArguments);
    }
  }

  /**
   * Either adds Spotify tracks or Discord media files to the track queue.
   * <p>
   * A Spotify API key must be provided in order for the bot to look up the names
   * of tracks within Spotify track, playlist, and album links to play from YouTube.
   * </p>
   *
   * @param ce        object containing information about the command event
   * @param arguments user provided arguments
   */
  private void handleSpotifyAndMediaLinks(CommandEvent ce, String[] arguments) {
    Dotenv dotenv = Dotenv.load();
    String spotifyClientID = dotenv.get("SPOTIFY_CLIENT_ID");
    String spotifyClientSecret = dotenv.get("SPOTIFY_CLIENT_SECRET");

    boolean missingSpotifyAPIKey = spotifyClientID == null || spotifyClientSecret == null;
    boolean isSpotifyLink = arguments[1].contains("https://open.spotify.com/");

    if (isSpotifyLink) {
      if (!missingSpotifyAPIKey) {
        boolean isSpotifyTrack = arguments[1].contains("https://open.spotify.com/track/");
        boolean isSpotifyPlayList = arguments[1].contains("https://open.spotify.com/playlist/");
        boolean isSpotifyAlbum = arguments[1].contains("https://open.spotify.com/album/");

        if (isSpotifyTrack) {
          processSpotifyTrackID(ce, arguments, spotifyClientID, spotifyClientSecret);
        } else if (isSpotifyPlayList) {
          processSpotifyPlaylistID(ce, arguments, spotifyClientID, spotifyClientSecret);
        } else if (isSpotifyAlbum) {
          processSpotifyAlbumID(ce, arguments, spotifyClientID, spotifyClientSecret);
        } else {
          ce.getChannel().sendMessage("Spotify feature not supported.").queue();
        }
      } else {
        ce.getChannel().sendMessage("Unable to play Spotify links. No Spotify API key provided " +
            "in the bot's .env file.").queue();
      }
    } else {
      PlayerManager.getINSTANCE().createAudioTrack(ce, arguments[1], false);
    }
  }

  /**
   * Builds a YouTube search query using user provided arguments and adds its first result to the track queue.
   *
   * @param ce                object containing information about the command event
   * @param arguments         user provided arguments
   * @param numberOfArguments number of user provided arguments
   */
  private void handleYouTubeSearchQuery(CommandEvent ce, String[] arguments, int numberOfArguments) {
    StringBuilder searchQuery = new StringBuilder();
    for (int i = 1; i < numberOfArguments + 1; i++) {
      searchQuery.append(arguments[i]);
    }
    String youtubeSearchQuery = "ytsearch:" + String.join(" ", searchQuery);
    PlayerManager.getINSTANCE().createAudioTrack(ce, youtubeSearchQuery, false);
  }

  /**
   * Checks if the Spotify track link was formatted correctly before adding the track to the queue.
   *
   * @param ce                  object containing information about the command event
   * @param arguments           user provided arguments
   * @param spotifyClientID     Spotify API key's client ID
   * @param spotifyClientSecret Spotify API key's client secret
   */
  private void processSpotifyTrackID(CommandEvent ce, String[] arguments,
                                     String spotifyClientID, String spotifyClientSecret) {
    String spotifyTrack = arguments[1].substring(31); // Remove https portion
    if (spotifyTrack.length() >= 22) {
      addSpotifyTrackToQueue(ce, spotifyTrack, spotifyClientID, spotifyClientSecret);
    } else {
      ce.getChannel().sendMessage("Invalid Spotify track ID.").queue();
    }
  }

  /**
   * Checks if the Spotify playlist link was formatted correctly before adding the track to the queue.
   *
   * @param ce                  object containing information about the command event
   * @param arguments           user provided arguments
   * @param spotifyClientID     Spotify API key's client ID
   * @param spotifyClientSecret Spotify API key's client secret
   */
  private void processSpotifyPlaylistID(CommandEvent ce, String[] arguments,
                                        String spotifyClientID, String spotifyClientSecret) {
    String spotifyPlaylist = arguments[1].substring(34); // Remove https portion
    if (spotifyPlaylist.length() >= 22) {
      playSpotifyPlaylistRequest(ce, spotifyPlaylist, spotifyClientID, spotifyClientSecret);
    } else {
      ce.getChannel().sendMessage("Invalid Spotify playlist ID").queue();
    }
  }

  /**
   * Checks if the Spotify album link was formatted correctly before adding the track to the queue.
   *
   * @param ce                  object containing information about the command event
   * @param arguments           user provided arguments
   * @param spotifyClientID     Spotify API key's client ID
   * @param spotifyClientSecret Spotify API key's client secret
   */
  private void processSpotifyAlbumID(CommandEvent ce, String[] arguments,
                                     String spotifyClientID, String spotifyClientSecret) {
    String spotifyAlbum = arguments[1].substring(31); // Remove https portion
    if (spotifyAlbum.length() >= 22) {
      playSpotifyAlbumRequest(ce, spotifyAlbum, spotifyClientID, spotifyClientSecret);
    } else {
      ce.getChannel().sendMessage("Invalid Spotify album ID").queue();
    }
  }

  /**
   * Deciphers Spotify song names through their Spotify track ID,
   * adds the track's associated artists with the song name as a
   * search query on YouTube, and adds the first result to the track queue.
   *
   * @param ce                  object containing information about the command event
   * @param spotifyTrack        Spotify track identified by track ID
   * @param spotifyClientID     Spotify API key's client ID
   * @param spotifyClientSecret Spotify API key's client secret
   */
  private void addSpotifyTrackToQueue(CommandEvent ce, String spotifyTrack,
                                      String spotifyClientID, String spotifyClientSecret) {
    // ID & Query -> ID only
    spotifyTrack = spotifyTrack.substring(0, 22);
    try {
      // Generate Spotify API access token
      SpotifyApi spotifyApi = new SpotifyApi.Builder().setClientId(spotifyClientID)
          .setClientSecret(spotifyClientSecret).build();
      ClientCredentialsRequest clientCredentialsRequest = spotifyApi.clientCredentials().build();
      ClientCredentials clientCredentials = clientCredentialsRequest.execute();
      spotifyApi.setAccessToken(clientCredentials.getAccessToken());

      // Match track with ID and get track's artists
      GetTrackRequest getTrackRequest = spotifyApi.getTrack(spotifyTrack).build();
      JSONObject jsonTrack = new JSONObject(getTrackRequest.getJson());
      JSONArray jsonTrackArtists = new JSONArray(jsonTrack.getJSONObject("album").
          getJSONArray("artists").toString());

      PlayerManager.getINSTANCE().createAudioTrack(ce,
          buildYouTubeSearchQuery(jsonTrack, jsonTrackArtists), false);
    } catch (IOException | SpotifyWebApiException | ParseException e) {
      System.out.println("Error: " + e.getMessage());
    }
  }

  /**
   * Deciphers Spotify song names through their Spotify playlist ID,
   * adds the track's associated artists with the song name as a
   * search query on YouTube, and adds the first result to the track queue.
   * <p>
   * Only the first 100 tracks in a Spotify playlist will be queued due to Spotify API limits.
   * </p>
   *
   * @param ce                  object containing information about the command event
   * @param spotifyPlaylist     Spotify playlist identified by track ID
   * @param spotifyClientID     Spotify API key's client ID
   * @param spotifyClientSecret Spotify API key's client secret
   */
  private void playSpotifyPlaylistRequest(CommandEvent ce, String spotifyPlaylist,
                                          String spotifyClientID, String spotifyClientSecret) {
    // ID & Query -> ID only
    spotifyPlaylist = spotifyPlaylist.substring(0, 22);
    try {
      // Generate Spotify API access token
      SpotifyApi spotifyApi = new SpotifyApi.Builder()
          .setClientId(spotifyClientID)
          .setClientSecret(spotifyClientSecret)
          .build();
      ClientCredentialsRequest clientCredentialsRequest = spotifyApi.clientCredentials().build();
      ClientCredentials clientCredentials = clientCredentialsRequest.execute();
      spotifyApi.setAccessToken(clientCredentials.getAccessToken());

      // Match playlist with ID and get playlist's tracks
      GetPlaylistRequest getPlaylistRequest = spotifyApi.getPlaylist(spotifyPlaylist).build();
      JSONObject jsonPlaylist = new JSONObject(getPlaylistRequest.getJson());
      JSONArray jsonTracks = new JSONArray(jsonPlaylist.getJSONObject("tracks").
          getJSONArray("items").toString());

      int numberOfTracksAdded = 0;
      for (int i = 0; i < jsonTracks.length(); i++) { // Queues tracks from playlist's tracks
        JSONObject jsonTrack = new JSONObject(jsonTracks.getJSONObject(i).getJSONObject("track").toString());
        JSONArray jsonTrackArtists = new JSONArray(jsonTrack.getJSONObject("album").
            getJSONArray("artists").toString());

        PlayerManager.getINSTANCE().createAudioTrack(ce,
            buildYouTubeSearchQuery(jsonTrack, jsonTrackArtists), true);
        numberOfTracksAdded++;
      }
      String requester = "[" + ce.getAuthor().getAsTag() + "]";
      ce.getChannel().sendMessage("**Added:** `" + numberOfTracksAdded + "` tracks " + requester).queue();
    } catch (IOException | SpotifyWebApiException | ParseException error) {
      System.out.println("Error: " + error.getMessage());
    }
  }

  /**
   * Deciphers Spotify song names through their Spotify playlist ID,
   * adds the track's associated artists with the song name as a
   * search query on YouTube, and adds the first result to the track queue.
   * <p>
   * Only the first 50 tracks in a Spotify playlist will be queued due to Spotify API limits.
   * </p>
   *
   * @param ce                  object containing information about the command event
   * @param spotifyAlbum        Spotify playlist identified by track ID
   * @param spotifyClientID     Spotify API key's client ID
   * @param spotifyClientSecret Spotify API key's client secret
   */
  private void playSpotifyAlbumRequest(CommandEvent ce, String spotifyAlbum,
                                       String spotifyClientID, String spotifyClientSecret) {
    // ID & Query -> ID only
    spotifyAlbum = spotifyAlbum.substring(0, 22);
    try {
      // Generate Spotify API access token
      SpotifyApi spotifyApi = new SpotifyApi.Builder()
          .setClientId(spotifyClientID)
          .setClientSecret(spotifyClientSecret)
          .build();
      ClientCredentialsRequest clientCredentialsRequest = spotifyApi.clientCredentials().build();
      ClientCredentials clientCredentials = clientCredentialsRequest.execute();
      spotifyApi.setAccessToken(clientCredentials.getAccessToken());

      // Match playlist with ID and get album's tracks
      GetAlbumsTracksRequest getAlbumsTracksRequest = spotifyApi.getAlbumsTracks(spotifyAlbum).limit(50).build();
      JSONObject jsonAlbum = new JSONObject(getAlbumsTracksRequest.getJson());
      JSONArray jsonTracks = new JSONArray(jsonAlbum.getJSONArray("items").toString());

      int numberOfTracksAdded = 0;
      for (int i = 0; i < jsonTracks.length(); i++) { // Queues tracks from album's tracks
        JSONObject jsonTrack = new JSONObject(jsonTracks.getJSONObject(i).toString());
        JSONArray jsonTrackArtists = new JSONArray(jsonTrack.getJSONArray("artists").toString());

        PlayerManager.getINSTANCE().createAudioTrack(ce,
            buildYouTubeSearchQuery(jsonTrack, jsonTrackArtists), true);
        numberOfTracksAdded++;
      }
      String requester = "[" + ce.getAuthor().getAsTag() + "]";
      ce.getChannel().sendMessage("**Added:** `" + numberOfTracksAdded + "` tracks " + requester).queue();
    } catch (IOException | SpotifyWebApiException | ParseException error) {
      System.out.println("Error: " + error.getMessage());
    }
  }

  /**
   * Builds a complete YouTube search query that includes the track name and its artists.
   *
   * @param jsonTrack        track in json format
   * @param jsonTrackArtists track's artists in json format
   * @return search query to be sent to YouTube
   */
  private String buildYouTubeSearchQuery(JSONObject jsonTrack, JSONArray jsonTrackArtists) {
    StringBuilder fullTrackRequest = new StringBuilder();
    fullTrackRequest.append(jsonTrack.getString("name"));
    for (int j = 0; j < jsonTrackArtists.length(); j++) {
      fullTrackRequest.append(jsonTrackArtists.getJSONObject(j).getString("name"));
    }
    return "ytsearch:" + String.join(" ", fullTrackRequest);
  }
}