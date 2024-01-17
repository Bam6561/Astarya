package commands.audio;

import astarya.Text;
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
 * Play is a command invocation that adds a track to the track queue.
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
 * @version 1.7.9
 * @since 1.1.0
 */
public class Play extends Command {

  public Play() {
    this.name = "play";
    this.aliases = new String[]{"play", "p"};
    this.help = "Adds a track to the track queue.";
    this.arguments = ("[1]URL [2 ++]YouTubeQuery");
  }

  /**
   * Checks if the user is in the same voice channel as the bot to read a play command request.
   * If the bot is not currently in any voice channel, then attempt to join the same one as the user.
   *
   * @param ce command event
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
        readPlayRequest(ce);
      } else {
        boolean userInSameVoiceChannel = userVoiceState.getChannel().equals(botVoiceState.getChannel());
        if (userInSameVoiceChannel) {
          readPlayRequest(ce);
        } else {
          ce.getChannel().sendMessage(Text.NOT_IN_SAME_VC.value()).queue();
        }
      }
    } catch (NullPointerException e) {
      ce.getChannel().sendMessage(Text.NOT_IN_VC.value()).queue();
    }
  }

  /**
   * Attempts to connect the bot to the same voice channel as the user.
   *
   * @param ce command event
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
   * Checks if the play command request was formatted correctly before interpreting its usage.
   *
   * @param ce command event
   */
  private void readPlayRequest(CommandEvent ce) {
    String[] parameters = ce.getMessage().getContentRaw().split("\\s");
    int numberOfParameters = parameters.length - 1;

    switch (numberOfParameters) {
      case 0 -> ce.getChannel().sendMessage(Text.INVALID_NUMBER_OF_PARAMS.value()).queue();
      case 1 -> readSpotifyApiKey(ce, parameters);
      default -> processYouTubeSearchQuery(ce, parameters, numberOfParameters);
    }
  }

  /**
   * Checks if a Spotify API Key was provided in order for the bot to look up the names
   * of tracks within Spotify track, playlist, and album links to play from YouTube.
   *
   * @param ce         command event
   * @param parameters user provided parameters
   */
  private void readSpotifyApiKey(CommandEvent ce, String[] parameters) {
    Dotenv dotenv = Dotenv.load();
    String spotifyClientId = dotenv.get("SPOTIFY_CLIENT_ID");
    String spotifyClientSecret = dotenv.get("SPOTIFY_CLIENT_SECRET");

    boolean missingSpotifyApiKey = spotifyClientId == null || spotifyClientSecret == null;
    boolean isSpotifyLink = parameters[1].contains("https://open.spotify.com/");

    if (isSpotifyLink) {
      if (!missingSpotifyApiKey) {
        interpretPlayRequest(ce, parameters, accessSpotifyApi(spotifyClientId, spotifyClientSecret));
      } else {
        ce.getChannel().sendMessage("Unable to play Spotify links. No Spotify API key provided " +
            "in the bot's .env file.").queue();
      }
    } else {
      PlayerManager.getINSTANCE().createAudioTrack(ce, parameters[1], false);
    }
  }

  /**
   * Builds a YouTube search query using user provided parameters and adds the first result to the track queue.
   *
   * @param ce                 command event
   * @param parameters         user provided parameters
   * @param numberOfParameters number of user provided parameters
   */
  private void processYouTubeSearchQuery(CommandEvent ce, String[] parameters, int numberOfParameters) {
    StringBuilder searchQuery = new StringBuilder();
    for (int i = 1; i < numberOfParameters + 1; i++) {
      searchQuery.append(parameters[i]);
    }
    String youtubeSearchQuery = "ytsearch:" + String.join(" ", searchQuery);
    PlayerManager.getINSTANCE().createAudioTrack(ce, youtubeSearchQuery, false);
  }

  /**
   * Generates an access token to access Spotify API's scopes.
   *
   * @param spotifyClientId     Spotify API client id
   * @param spotifyClientSecret Spotify API client secret
   * @return an object representing Spotify API
   */
  private SpotifyApi accessSpotifyApi(String spotifyClientId, String spotifyClientSecret) {
    try {
      SpotifyApi spotifyApi = new SpotifyApi.Builder()
          .setClientId(spotifyClientId)
          .setClientSecret(spotifyClientSecret)
          .build();
      ClientCredentialsRequest clientCredentialsRequest = spotifyApi.clientCredentials().build();
      ClientCredentials clientCredentials = clientCredentialsRequest.execute();
      spotifyApi.setAccessToken(clientCredentials.getAccessToken());
      return spotifyApi;
    } catch (IOException | ParseException | SpotifyWebApiException e) {
      System.out.println(Text.SPOTIFY_API_ERROR.value());
      return null;
    }
  }

  /**
   * Identifies user given Spotify link as either a track,
   * playlist, or album before adding it to the track queue.
   *
   * @param ce         command event
   * @param parameters user provided parameters
   * @param spotifyApi object representing Spotify API
   */
  private void interpretPlayRequest(CommandEvent ce, String[] parameters, SpotifyApi spotifyApi) {
    boolean isSpotifyTrack = parameters[1].contains("https://open.spotify.com/track/");
    boolean isSpotifyPlayList = parameters[1].contains("https://open.spotify.com/playlist/");
    boolean isSpotifyAlbum = parameters[1].contains("https://open.spotify.com/album/");

    if (isSpotifyTrack) {
      readSpotifyTrackId(ce, parameters, spotifyApi);
    } else if (isSpotifyPlayList) {
      readSpotifyPlaylistId(ce, parameters, spotifyApi);
    } else if (isSpotifyAlbum) {
      readSpotifyAlbumId(ce, parameters, spotifyApi);
    } else {
      ce.getChannel().sendMessage("Spotify feature not supported.").queue();
    }
  }

  /**
   * Checks if the Spotify track link was formatted correctly before adding it to the track queue.
   *
   * @param ce         command event
   * @param parameters user provided parameters
   * @param spotifyApi object representing Spotify API
   */
  private void readSpotifyTrackId(CommandEvent ce, String[] parameters, SpotifyApi spotifyApi) {
    String spotifyTrack = parameters[1].substring(31); // Remove https portion
    if (spotifyTrack.length() >= 22) {
      addSpotifyTrackToQueue(ce, spotifyTrack, spotifyApi);
    } else {
      ce.getChannel().sendMessage("Invalid Spotify track Id.").queue();
    }
  }

  /**
   * Checks if the Spotify playlist link was formatted correctly before adding it to the track queue.
   *
   * @param ce         command event
   * @param parameters user provided parameters
   * @param spotifyApi object representing Spotify API
   */
  private void readSpotifyPlaylistId(CommandEvent ce, String[] parameters, SpotifyApi spotifyApi) {
    String spotifyPlaylist = parameters[1].substring(34); // Remove https portion
    if (spotifyPlaylist.length() >= 22) {
      addSpotifyPlaylistToQueue(ce, spotifyPlaylist, spotifyApi);
    } else {
      ce.getChannel().sendMessage("Invalid Spotify playlist Id").queue();
    }
  }

  /**
   * Checks if the Spotify album link was formatted correctly before adding it to the track queue.
   *
   * @param ce         command event
   * @param parameters user provided parameters
   * @param spotifyApi object representing Spotify API
   */
  private void readSpotifyAlbumId(CommandEvent ce, String[] parameters, SpotifyApi spotifyApi) {
    String spotifyAlbum = parameters[1].substring(31); // Remove https portion
    if (spotifyAlbum.length() >= 22) {
      addSpotifyAlbumToQueue(ce, spotifyAlbum, spotifyApi);
    } else {
      ce.getChannel().sendMessage("Invalid Spotify album Id").queue();
    }
  }

  /**
   * Deciphers Spotify song names through their Spotify track Id,
   * adds the track's associated artists with the song name as a
   * search query on YouTube, and adds the first result to the track queue.
   *
   * @param ce           command event
   * @param spotifyTrack Spotify track identified by track Id
   * @param spotifyApi   object representing Spotify API
   */
  private void addSpotifyTrackToQueue(CommandEvent ce, String spotifyTrack, SpotifyApi spotifyApi) {
    // Id & Query -> Id only
    spotifyTrack = spotifyTrack.substring(0, 22);
    try {
      // Match track with Id and get track's artists
      GetTrackRequest getTrackRequest = spotifyApi.getTrack(spotifyTrack).build();
      JSONObject jsonTrack = new JSONObject(getTrackRequest.getJson());
      JSONArray jsonTrackArtists = new JSONArray(jsonTrack.getJSONObject("album").
          getJSONArray("artists").toString());

      PlayerManager.getINSTANCE().createAudioTrack(ce,
          buildYouTubeSearchQuery(jsonTrack, jsonTrackArtists), false);
    } catch (IOException | SpotifyWebApiException | ParseException e) {
      System.out.println(Text.SPOTIFY_API_ERROR.value());
    }
  }

  /**
   * Deciphers Spotify song names through their Spotify playlist Id,
   * adds the track's associated artists with the song name as a
   * search query on YouTube, and adds the first result to the track queue.
   * <p>
   * Only the first 100 tracks in a Spotify playlist will be queued due to Spotify API limits.
   * </p>
   *
   * @param ce              command event
   * @param spotifyPlaylist Spotify playlist identified by track Id
   * @param spotifyApi      object representing Spotify API
   */
  private void addSpotifyPlaylistToQueue(CommandEvent ce, String spotifyPlaylist,
                                         SpotifyApi spotifyApi) {
    // Id & Query -> Id only
    spotifyPlaylist = spotifyPlaylist.substring(0, 22);
    try {
      // Match playlist with Id and get playlist's tracks
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
    } catch (IOException | SpotifyWebApiException | ParseException e) {
      System.out.println(Text.SPOTIFY_API_ERROR.value());
    }
  }

  /**
   * Deciphers Spotify song names through their Spotify playlist Id,
   * adds the track's associated artists with the song name as a
   * search query on YouTube, and adds the first result to the track queue.
   * <p>
   * Only the first 50 tracks in a Spotify playlist will be queued due to Spotify API limits.
   * </p>
   *
   * @param ce           command event
   * @param spotifyAlbum Spotify playlist identified by track Id
   * @param spotifyApi   object representing Spotify API
   */
  private void addSpotifyAlbumToQueue(CommandEvent ce, String spotifyAlbum,
                                      SpotifyApi spotifyApi) {
    // Id & Query -> Id only
    spotifyAlbum = spotifyAlbum.substring(0, 22);
    try {
      // Match playlist with Id and get album's tracks
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
    } catch (IOException | SpotifyWebApiException | ParseException e) {
      System.out.println(Text.SPOTIFY_API_ERROR.value());
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