package commands.audio;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.credentials.ClientCredentials;
import com.wrapper.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;
import com.wrapper.spotify.requests.data.albums.GetAlbumsTracksRequest;
import com.wrapper.spotify.requests.data.playlists.GetPlaylistRequest;
import com.wrapper.spotify.requests.data.tracks.GetTrackRequest;
import commands.audio.managers.PlayerManager;
import commands.owner.Settings;
import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.managers.AudioManager;
import org.apache.hc.core5.http.ParseException;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

public class Play extends Command {

  public Play() {
    this.name = "play";
    this.aliases = new String[]{"play", "p", "add"};
    this.help = "Adds an audio track to the queue.";
    this.arguments = ("[1]URL [2++]YouTubeQuery");
  }

  // Requests for a track to be added to the track queue, changes bot's presence and activity
  @Override
  protected void execute(CommandEvent ce) {
    Settings.deleteInvoke(ce);

    GuildVoiceState userVoiceState = ce.getMember().getVoiceState();
    GuildVoiceState botVoiceState = ce.getGuild().getSelfMember().getVoiceState();

    boolean userInVoiceChannel = ce.getMember().getVoiceState().inVoiceChannel();
    boolean botNotAlreadyInVoiceChannel = !botVoiceState.inVoiceChannel();
    boolean userInSameVoiceChannel = userVoiceState.getChannel().equals(botVoiceState.getChannel());

    if (userInVoiceChannel) {
      if (botNotAlreadyInVoiceChannel) {
        joinVoiceChannel(ce);
        parsePlayRequest(ce);
      } else {
        if (userInSameVoiceChannel) {
          parsePlayRequest(ce);
        } else {
          ce.getChannel().sendMessage("User not in the same voice channel.").queue();
        }
      }
    } else {
      ce.getChannel().sendMessage("User not in a voice channel.").queue();
    }
  }

  // Attempts to join the same voice channel as the user
  private void joinVoiceChannel(CommandEvent ce) {
    VoiceChannel voiceChannel = ce.getMember().getVoiceState().getChannel();
    AudioManager audioManager = ce.getGuild().getAudioManager();

    try {
      audioManager.openAudioConnection(voiceChannel);
      ce.getChannel().sendMessage("Connected to <#" + voiceChannel.getId() + ">").queue();
    } catch (Exception e) { // Insufficient permissions
      ce.getChannel().sendMessage("Unable to join <#" + voiceChannel.getId() + ">").queue();
    }
  }

  // Processes play request before proceeding further
  private void parsePlayRequest(CommandEvent ce) {
    // Parse message for arguments
    String[] arguments = ce.getMessage().getContentRaw().split("\\s");
    int numberOfArguments = arguments.length;

    switch (numberOfArguments) {
      case 2 -> { // Spotify Track || Playlist || Album
        boolean isSpotifyLink = arguments[1].contains("https://open.spotify.com/");
        if (isSpotifyLink) {
          boolean isSpotifyTrack = arguments[1].contains("https://open.spotify.com/track/");
          boolean isSpotifyPlayList = arguments[1].contains("https://open.spotify.com/playlist/");
          boolean isSpotifyAlbum = arguments[1].contains("https://open.spotify.com/album/");

          if (isSpotifyTrack) {
            processSpotifyTrack(ce, arguments);
          } else if (isSpotifyPlayList) {
            processSpotifyPlaylist(ce, arguments);
          } else if (isSpotifyAlbum) {
            processSpotifyAlbum(ce, arguments);
          } else {
            ce.getChannel().sendMessage("Spotify feature not supported.").queue();
          }
        } else { // Not Spotify link
          PlayerManager.getINSTANCE().createAudioTrack(ce, arguments[1]);
        }
      }

      case 1 -> // Invalid argument
          ce.getChannel().sendMessage("Invalid number of arguments.").queue();

      default -> { // Search query
        StringBuilder searchQuery = new StringBuilder();
        for (int i = 1; i < numberOfArguments; i++) {
          searchQuery.append(arguments[i]);
        }
        String youtubeSearchQuery = "ytsearch:" + String.join(" ", searchQuery);
        PlayerManager.getINSTANCE().createAudioTrack(ce, youtubeSearchQuery);
      }
    }
  }

  // Validates Spotify track links before proceeding further
  private void processSpotifyTrack(CommandEvent ce, String[] args) {
    String spotifyTrack = args[1].substring(31); // Remove https portion
    if (spotifyTrack.length() >= 22) { // ID or ID & Query
      playSpotifyTrackRequest(ce, spotifyTrack);
    } else { // Invalid ID
      ce.getChannel().sendMessage("Invalid Spotify track ID.").queue();
    }
  }

  // Validates Spotify playlist links before proceeding further
  private void processSpotifyPlaylist(CommandEvent ce, String[] args) {
    String spotifyPlaylist = args[1].substring(34); // Remove https portion
    if (spotifyPlaylist.length() >= 22) { // ID or ID & Query
      playSpotifyPlaylistRequest(ce, spotifyPlaylist);
    } else { //Invalid ID
      ce.getChannel().sendMessage("Invalid Spotify playlist ID").queue();
    }
  }

  // Validates Spotify album links before proceeding further
  private void processSpotifyAlbum(CommandEvent ce, String[] args) {
    String spotifyAlbum = args[1].substring(31); // Remove https portion
    if (spotifyAlbum.length() >= 22) { // ID or ID & Query
      playSpotifyAlbumRequest(ce, spotifyAlbum);
    } else { // Invalid ID
      ce.getChannel().sendMessage("Invalid Spotify album ID").queue();
    }
  }

  // Queues Spotify track requests
  private void playSpotifyTrackRequest(CommandEvent ce, String spotifyTrack) {
    spotifyTrack = spotifyTrack.substring(0, 22); // ID & Query -> ID only

    // Spotify API authorization
    Dotenv dotenv = Dotenv.load();
    String clientID = dotenv.get("SPOTIFY_CLIENT_ID");
    String clientSecret = dotenv.get("SPOTIFY_CLIENT_SECRET");
    SpotifyApi spotifyApi = new SpotifyApi.Builder()
        .setClientId(clientID)
        .setClientSecret(clientSecret)
        .build();
    ClientCredentialsRequest clientCredentialsRequest = spotifyApi.clientCredentials().build();

    try {
      // Generate Spotify API access token
      ClientCredentials clientCredentials = clientCredentialsRequest.execute();
      spotifyApi.setAccessToken(clientCredentials.getAccessToken());

      // Search for matching track to ID
      GetTrackRequest getTrackRequest = spotifyApi.getTrack(spotifyTrack).build();
      JSONObject jsonTrack = new JSONObject(getTrackRequest.getJson()); // Convert response to json

      // Get track's artists
      JSONArray jsonTrackArtists = new JSONArray(jsonTrack.getJSONObject("album").
          getJSONArray("artists").toString());

      // Full YouTube query (track name and artists)
      StringBuilder fullTrackRequest = new StringBuilder();
      fullTrackRequest.append(jsonTrack.getString("name")); // Track name
      for (int i = 0; i < jsonTrackArtists.length(); i++) { // Artist name(s)
        fullTrackRequest.append(jsonTrackArtists.getJSONObject(i).getString("name"));
      }

      String youtubeSearchQuery = "ytsearch:" + String.join(" ", fullTrackRequest);
      PlayerManager.getINSTANCE().createAudioTrack(ce, youtubeSearchQuery);
    } catch (IOException | SpotifyWebApiException | ParseException error) {
      System.out.println("Error: " + error.getMessage());
    }
  }

  // Queues Spotify playlists
  private void playSpotifyPlaylistRequest(CommandEvent ce, String spotifyPlaylist) {
    spotifyPlaylist = spotifyPlaylist.substring(0, 22); // ID & Query -> ID only

    // Spotify API authorization
    Dotenv dotenv = Dotenv.load();
    String clientID = dotenv.get("SPOTIFY_CLIENT_ID");
    String clientSecret = dotenv.get("SPOTIFY_CLIENT_SECRET");
    SpotifyApi spotifyApi = new SpotifyApi.Builder()
        .setClientId(clientID)
        .setClientSecret(clientSecret)
        .build();
    ClientCredentialsRequest clientCredentialsRequest = spotifyApi.clientCredentials().build();

    try {
      // Generate Spotify API access token
      ClientCredentials clientCredentials = clientCredentialsRequest.execute();
      spotifyApi.setAccessToken(clientCredentials.getAccessToken());

      // Search for matching playlist to ID
      GetPlaylistRequest getPlaylistRequest = spotifyApi.getPlaylist(spotifyPlaylist).build();
      JSONObject jsonPlaylist = new JSONObject(getPlaylistRequest.getJson()); // Convert response to json
      JSONArray jsonTracks = new JSONArray(jsonPlaylist.getJSONObject("tracks"). // Get playlist's tracks
          getJSONArray("items").toString());

      int tracksAdded = 0; // Number of tracks added
      for (int i = 0; i < jsonTracks.length(); i++) { // Get tracks from playlist's tracks
        // Convert playlist tracks to json
        JSONObject jsonTrack = new JSONObject(jsonTracks.getJSONObject(i).getJSONObject("track").toString());

        // Get track's artists
        JSONArray jsonTrackArtists = new JSONArray(jsonTrack.getJSONObject("album").
            getJSONArray("artists").toString());

        // Full YouTube query (track name and artists)
        StringBuilder fullTrackRequest = new StringBuilder();
        fullTrackRequest.append(jsonTrack.getString("name")); // Track name
        for (int j = 0; j < jsonTrackArtists.length(); j++) { // Artist name(s)
          fullTrackRequest.append(jsonTrackArtists.getJSONObject(j).getString("name"));
        }
        String youtubeSearchQuery = "ytsearch:" + String.join(" ", fullTrackRequest);

        PlayerManager.getINSTANCE().createAudioTrackSilent(ce, youtubeSearchQuery);
        tracksAdded++;
      }
      String requester = "[" + ce.getAuthor().getAsTag() + "]";
      ce.getChannel().sendMessage("**Added:** `" + tracksAdded
          + "` tracks " + requester).queue();
    } catch (IOException | SpotifyWebApiException | ParseException error) {
      System.out.println("Error: " + error.getMessage());
    }
  }

  // Queues Spotify Albums
  private void playSpotifyAlbumRequest(CommandEvent ce, String spotifyAlbum) {
    // ID & Query -> ID only
    spotifyAlbum = spotifyAlbum.substring(0, 22);

    // Spotify API authorization
    Dotenv dotenv = Dotenv.load();
    String clientID = dotenv.get("SPOTIFY_CLIENT_ID");
    String clientSecret = dotenv.get("SPOTIFY_CLIENT_SECRET");
    SpotifyApi spotifyApi = new SpotifyApi.Builder()
        .setClientId(clientID)
        .setClientSecret(clientSecret)
        .build();
    ClientCredentialsRequest clientCredentialsRequest = spotifyApi.clientCredentials().build();

    try {
      // Generate Spotify API access token
      ClientCredentials clientCredentials = clientCredentialsRequest.execute();
      spotifyApi.setAccessToken(clientCredentials.getAccessToken());

      // Search for matching album to ID
      GetAlbumsTracksRequest getAlbumsTracksRequest = spotifyApi.getAlbumsTracks(spotifyAlbum).limit(50).build();
      JSONObject jsonAlbum = new JSONObject(getAlbumsTracksRequest.getJson()); // Convert response to json
      JSONArray jsonTracks = new JSONArray(jsonAlbum.getJSONArray("items").toString()); // Get tracks from album

      int tracksAdded = 0; // Number of tracks added
      for (int i = 0; i < jsonTracks.length(); i++) { // Get tracks from playlist's tracks
        // Convert album tracks to json
        JSONObject jsonTrack = new JSONObject(jsonTracks.getJSONObject(i).toString());

        // Get track's artists
        JSONArray jsonTrackArtists = new JSONArray(jsonTrack.getJSONArray("artists").toString());

        // Full YouTube query (track name and artists)
        StringBuilder fullTrackRequest = new StringBuilder();
        fullTrackRequest.append(jsonTrack.getString("name")); // Track name
        for (int j = 0; j < jsonTrackArtists.length(); j++) { // Artist name(s)
          fullTrackRequest.append(jsonTrackArtists.getJSONObject(j).getString("name"));
        }
        tracksAdded++;

        String youtubeSearchQuery = "ytsearch:" + String.join(" ", fullTrackRequest);
        PlayerManager.getINSTANCE().createAudioTrackSilent(ce, youtubeSearchQuery);
      }
      String requester = "[" + ce.getAuthor().getAsTag() + "]";
      ce.getChannel().sendMessage("**Added:** `" + tracksAdded
          + "` tracks " + requester).queue();
    } catch (IOException | SpotifyWebApiException | ParseException error) {
      System.out.println("Error: " + error.getMessage());
    }
  }
}