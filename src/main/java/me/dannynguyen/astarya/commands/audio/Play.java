package me.dannynguyen.astarya.commands.audio;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import io.github.cdimascio.dotenv.Dotenv;
import me.dannynguyen.astarya.commands.audio.managers.PlayerManager;
import me.dannynguyen.astarya.commands.owner.Settings;
import me.dannynguyen.astarya.enums.BotMessage;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
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
 * Command invocation that adds a track to the queue.
 * <p>
 * By default, the play command supports YouTube videos,
 * playlists, and most media files posted in Discord chat.
 * <p>
 * A Spotify API key is required to queue Spotify songs, playlists, and album links.
 * <p>
 * Spotify API limits:
 * <ul>
 *  <li> Playlists: 100
 *  <li> Albums: 50
 * </ul>
 *
 * @author Danny Nguyen
 * @version 1.8.10
 * @since 1.1.0
 */
public class Play extends Command {
  /**
   * Associates the command with its properties.
   */
  public Play() {
    this.name = "play";
    this.aliases = new String[]{"play", "p"};
    this.help = "Adds a track to the track queue.";
    this.arguments = ("[1]URL [2 ++]YouTubeQuery");
  }

  /**
   * Checks if the user is in the same voice channel as the bot to read the command request.
   * <p>
   * If the bot is not currently in any voice channel, then attempt to join the same one as the user.
   *
   * @param ce command event
   */
  @Override
  protected void execute(CommandEvent ce) {
    Settings.deleteInvoke(ce);

    AudioChannelUnion userChannel = ce.getMember().getVoiceState().getChannel();
    AudioChannelUnion botChannel = ce.getGuild().getSelfMember().getVoiceState().getChannel();

    if (userChannel == null) {
      ce.getChannel().sendMessage(BotMessage.USER_NOT_IN_VC.getMessage()).queue();
      return;
    }

    if (botChannel == null) {
      joinVoiceChannel(ce);
      new PlayRequest(ce).interpretRequest();
      return;
    }

    if (userChannel.equals(botChannel)) {
      new PlayRequest(ce).interpretRequest();
    } else {
      ce.getChannel().sendMessage(BotMessage.USER_NOT_IN_SAME_VC.getMessage()).queue();
    }
  }

  /**
   * Attempts to connect the bot to the same voice channel as the user.
   *
   * @param ce command event
   */
  private void joinVoiceChannel(CommandEvent ce) {
    AudioChannel audioChannel = ce.getMember().getVoiceState().getChannel();
    try {
      ce.getGuild().getAudioManager().openAudioConnection(audioChannel);
      ce.getChannel().sendMessage("Connected to <#" + audioChannel.getId() + ">").queue();
    } catch (Exception e) { // Insufficient permissions
      ce.getChannel().sendMessage("Unable to join <#" + audioChannel.getId() + ">").queue();
    }
  }

  /**
   * Represents a track queue addition query.
   *
   * @author Danny Nguyen
   * @version 1.8.10
   * @since 1.8.10
   */
  private static class PlayRequest {
    /**
     * Command event.
     */
    private final CommandEvent ce;

    /**
     * User provided parameters.
     */
    private final String[] parameters;

    /**
     * Number of parameters.
     */
    private final int numberOfParameters;

    /**
     * Spotify API access.
     */
    private SpotifyApi spotifyApi;

    /**
     * Associates a track request with its parameters.
     *
     * @param ce command event
     */
    PlayRequest(CommandEvent ce) {
      this.ce = ce;
      this.parameters = ce.getMessage().getContentRaw().split("\\s");
      this.numberOfParameters = parameters.length - 1;
    }

    /**
     * Checks if the command request was formatted correctly before interpreting its usage.
     */
    private void interpretRequest() {
      switch (numberOfParameters) {
        case 0 -> ce.getChannel().sendMessage(BotMessage.INVALID_NUMBER_OF_PARAMETERS.getMessage()).queue();
        case 1 -> readSpotifyApiKey();
        default -> processYouTubeSearchQuery();
      }
    }

    /**
     * Checks if a Spotify API Key was provided in order for the bot to look up the names
     * of tracks within Spotify track, playlist, and album links to play from YouTube.
     */
    private void readSpotifyApiKey() {
      Dotenv dotenv = Dotenv.load();
      String spotifyClientId = dotenv.get("SPOTIFY_CLIENT_ID");
      String spotifyClientSecret = dotenv.get("SPOTIFY_CLIENT_SECRET");

      if (parameters[1].contains("https://open.spotify.com/")) {
        if (spotifyClientId != null && spotifyClientSecret != null) {
          try {
            spotifyApi = new SpotifyApi.Builder()
                .setClientId(spotifyClientId)
                .setClientSecret(spotifyClientSecret)
                .build();
            ClientCredentialsRequest clientCredentialsRequest = spotifyApi.clientCredentials().build();
            ClientCredentials clientCredentials = clientCredentialsRequest.execute();
            spotifyApi.setAccessToken(clientCredentials.getAccessToken());
          } catch (IOException | ParseException | SpotifyWebApiException e) {
            System.out.println(Error.ERROR_SPOTIFY_API.message);
          }
          interpretSpotifyLinkType();
        } else {
          ce.getChannel().sendMessage("Unable to play Spotify links. No Spotify API key provided in .env file.").queue();
        }
      } else {
        PlayerManager.getINSTANCE().createAudioTrack(ce, parameters[1], false);
      }
    }

    /**
     * Builds a YouTube search query using user provided parameters and adds the first result to the queue.
     */
    private void processYouTubeSearchQuery() {
      StringBuilder searchQuery = new StringBuilder();
      for (int i = 1; i < numberOfParameters + 1; i++) {
        searchQuery.append(parameters[i]);
      }
      String youtubeSearchQuery = "ytsearch:" + String.join(" ", searchQuery);
      PlayerManager.getINSTANCE().createAudioTrack(ce, youtubeSearchQuery, false);
    }

    /**
     * Identifies user given Spotify links as either a track,
     * playlist, or album before adding it to the queue.
     */
    private void interpretSpotifyLinkType() {
      boolean isSpotifyTrack = parameters[1].contains("https://open.spotify.com/track/");
      boolean isSpotifyPlayList = parameters[1].contains("https://open.spotify.com/playlist/");
      boolean isSpotifyAlbum = parameters[1].contains("https://open.spotify.com/album/");

      if (isSpotifyTrack) {
        readSpotifyTrackId();
      } else if (isSpotifyPlayList) {
        readSpotifyPlaylistId();
      } else if (isSpotifyAlbum) {
        readSpotifyAlbumId();
      } else {
        ce.getChannel().sendMessage("Spotify feature not supported.").queue();
      }
    }

    /**
     * Checks if the Spotify track link was formatted correctly before adding it to the queue.
     */
    private void readSpotifyTrackId() {
      String spotifyTrack = parameters[1].substring(31); // Remove https portion
      if (spotifyTrack.length() >= 22) {
        addSpotifyTrackToQueue(spotifyTrack);
      } else {
        ce.getChannel().sendMessage("Invalid Spotify track id.").queue();
      }
    }

    /**
     * Checks if the Spotify playlist link was formatted correctly before adding it to the queue.
     */
    private void readSpotifyPlaylistId() {
      String spotifyPlaylist = parameters[1].substring(34); // Remove https portion
      if (spotifyPlaylist.length() >= 22) {
        addSpotifyPlaylistToQueue(spotifyPlaylist);
      } else {
        ce.getChannel().sendMessage("Invalid Spotify playlist id.").queue();
      }
    }

    /**
     * Checks if the Spotify album link was formatted correctly before adding it to the queue.
     */
    private void readSpotifyAlbumId() {
      String spotifyAlbum = parameters[1].substring(31); // Remove https portion
      if (spotifyAlbum.length() >= 22) {
        addSpotifyAlbumToQueue(spotifyAlbum);
      } else {
        ce.getChannel().sendMessage("Invalid Spotify album id.").queue();
      }
    }

    /**
     * Deciphers Spotify song names through their Spotify track id,
     * adds the track's associated artists with the song name as a
     * search query on YouTube, and adds the first result to the queue.
     *
     * @param spotifyTrack Spotify track identified by id
     */
    private void addSpotifyTrackToQueue(String spotifyTrack) {
      // Id & Query -> Id only
      spotifyTrack = spotifyTrack.substring(0, 22);
      try {
        // Match track with id and get track's artists
        GetTrackRequest getTrackRequest = spotifyApi.getTrack(spotifyTrack).build();
        JSONObject jsonTrack = new JSONObject(getTrackRequest.getJson());
        JSONArray jsonTrackArtists = new JSONArray(jsonTrack.getJSONObject("album").getJSONArray("artists").toString());

        PlayerManager.getINSTANCE().createAudioTrack(ce, buildYouTubeSearchQuery(jsonTrack, jsonTrackArtists), false);
      } catch (IOException | SpotifyWebApiException | ParseException e) {
        System.out.println(Error.ERROR_SPOTIFY_API.message);
      }
    }

    /**
     * Deciphers Spotify song names through their Spotify playlist id,
     * adds the track's associated artists with the song name as a
     * search query on YouTube, and adds the first result to the queue.
     *
     * @param spotifyPlaylist Spotify playlist identified by id
     */
    private void addSpotifyPlaylistToQueue(String spotifyPlaylist) {
      // Id & Query -> Id only
      spotifyPlaylist = spotifyPlaylist.substring(0, 22);
      try {
        // Match playlist with id and get playlist's tracks
        GetPlaylistRequest getPlaylistRequest = spotifyApi.getPlaylist(spotifyPlaylist).build();
        JSONObject jsonPlaylist = new JSONObject(getPlaylistRequest.getJson());
        JSONArray jsonTracks = new JSONArray(jsonPlaylist.getJSONObject("tracks").getJSONArray("items").toString());

        int numberOfTracksAdded = 0;
        for (int i = 0; i < jsonTracks.length(); i++) { // Queue tracks from playlist's tracks
          JSONObject jsonTrack = new JSONObject(jsonTracks.getJSONObject(i).getJSONObject("track").toString());
          JSONArray jsonTrackArtists = new JSONArray(jsonTrack.getJSONObject("album").getJSONArray("artists").toString());

          PlayerManager.getINSTANCE().createAudioTrack(ce, buildYouTubeSearchQuery(jsonTrack, jsonTrackArtists), true);
          numberOfTracksAdded++;
        }

        String requester = "[" + ce.getAuthor().getAsTag() + "]";
        ce.getChannel().sendMessage("**Added:** `" + numberOfTracksAdded + "` tracks " + requester).queue();
      } catch (IOException | SpotifyWebApiException | ParseException e) {
        System.out.println(Error.ERROR_SPOTIFY_API.message);
      }
    }

    /**
     * Deciphers Spotify song names through their Spotify playlist id,
     * adds the track's associated artists with the song name as a
     * search query on YouTube, and adds the first result to the queue.
     *
     * @param spotifyAlbum Spotify playlist identified by id
     */
    private void addSpotifyAlbumToQueue(String spotifyAlbum) {
      // Id & Query -> Id only
      spotifyAlbum = spotifyAlbum.substring(0, 22);
      try {
        // Match playlist with id and get album's tracks
        GetAlbumsTracksRequest getAlbumsTracksRequest = spotifyApi.getAlbumsTracks(spotifyAlbum).limit(50).build();
        JSONObject jsonAlbum = new JSONObject(getAlbumsTracksRequest.getJson());
        JSONArray jsonTracks = new JSONArray(jsonAlbum.getJSONArray("items").toString());

        int numberOfTracksAdded = 0;
        for (int i = 0; i < jsonTracks.length(); i++) { // Queue tracks from album's tracks
          JSONObject jsonTrack = new JSONObject(jsonTracks.getJSONObject(i).toString());
          JSONArray jsonTrackArtists = new JSONArray(jsonTrack.getJSONArray("artists").toString());

          PlayerManager.getINSTANCE().createAudioTrack(ce, buildYouTubeSearchQuery(jsonTrack, jsonTrackArtists), true);
          numberOfTracksAdded++;
        }

        String requester = "[" + ce.getAuthor().getAsTag() + "]";
        ce.getChannel().sendMessage("**Added:** `" + numberOfTracksAdded + "` tracks " + requester).queue();
      } catch (IOException | SpotifyWebApiException | ParseException e) {
        System.out.println(Error.ERROR_SPOTIFY_API.message);
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

    /**
     * Types of errors.
     */
    private enum Error {
      /**
       * Spotify API error.
       */
      ERROR_SPOTIFY_API("Something went wrong while trying to access SpotifyAPI.");

      /**
       * Error message.
       */
      public final String message;

      /**
       * Associates an error with its message.
       *
       * @param message message
       */
      Error(String message) {
        this.message = message;
      }
    }
  }
}