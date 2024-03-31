package me.dannynguyen.astarya.commands.audio;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import me.dannynguyen.astarya.commands.owner.Settings;
import me.dannynguyen.astarya.enums.BotMessage;
import net.dv8tion.jda.api.EmbedBuilder;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

/**
 * Command invocation that queries Genius API for a song's lyrics.
 * <p>
 * By default, the command is set to return the first five matches.
 *
 * @author Danny Nguyen
 * @version 1.8.9
 * @since 1.7.2
 */
public class Lyrics extends Command {
  /**
   * Associates the command with its properties.
   */
  public Lyrics() {
    this.name = "lyrics";
    this.aliases = new String[]{"lyrics"};
    this.arguments = "[1 ++]SongName";
    this.help = "Finds lyrics of a song using Genius.";
  }

  /**
   * Checks if user provided parameters to process the command request.
   *
   * @param ce command event
   */
  @Override
  protected void execute(CommandEvent ce) {
    Settings.deleteInvoke(ce);

    String[] parameters = ce.getMessage().getContentRaw().split("\\s");
    int numberOfParameters = parameters.length - 1;

    if (numberOfParameters >= 1) {
      new GeniusQuery(ce, parameters).processLyricsRequest();
    } else {
      ce.getChannel().sendMessage(BotMessage.INVALID_NUMBER_OF_PARAMETERS.getMessage()).queue();
    }
  }

  /**
   * Represents a Genius track query.
   *
   * @author Danny Nguyen
   * @version 1.8.9
   * @since 1.8.9
   */
  private class GeniusQuery {
    /**
     * Command event.
     */
    private final CommandEvent ce;

    /**
     * User provided parameters.
     */
    private final String[] parameters;

    /**
     * Associates a Genius query with its parameters.
     *
     * @param ce         command event
     * @param parameters parameters
     */
    GeniusQuery(CommandEvent ce, String[] parameters) {
      this.ce = ce;
      this.parameters = parameters;
    }

    /**
     * Combines an endpoint URL with its search query to query Genius API with.
     */
    private void processLyricsRequest() {
      try {
        URL endpointUrlQuery = new URL("https://genius.com/api/search/song?q=" + buildSearchQuery());
        String httpResponse = readHttpResponse(endpointUrlQuery);
        processHttpResponse(httpResponse);
      } catch (MalformedURLException ignored) {
        // Url is pre-set to be non-null
      }
    }

    /**
     * Builds a search query from user provided parameters.
     *
     * @return text containing search query
     */
    private String buildSearchQuery() {
      StringBuilder searchQuery = new StringBuilder();
      for (int i = 1; i < parameters.length; i++) {
        searchQuery.append(parameters[i]);
        if (i < parameters.length - 1) {
          searchQuery.append("%20");
        }
      }
      return searchQuery.toString();
    }

    /**
     * Reads the query response from Genius API.
     *
     * @param endpointUrlQuery endpoint and its search query
     * @return HTTP response
     */
    private String readHttpResponse(URL endpointUrlQuery) {
      try {
        HttpURLConnection connection = (HttpURLConnection) endpointUrlQuery.openConnection();
        connection.setRequestMethod("GET");
        connection.connect();
        return new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8)).lines().collect(Collectors.joining());
      } catch (IOException e) {
        System.out.println("Connection interrupted.");
        return null;
      }
    }

    /**
     * Reads the HTTP response as a JSON and checks if
     * any results were found before parsing its data.
     *
     * @param httpResponse the response from the http connection
     */
    private void processHttpResponse(String httpResponse) {
      // Main JSON body
      JSONObject httpResponseJSON = new JSONObject(httpResponse);
      JSONObject section = (JSONObject) httpResponseJSON.getJSONObject("response").getJSONArray("sections").get(0);
      try {
        // Title & URL from top 5 matches
        GeniusMatchResult[] matches = new GeniusMatchResult[5];
        for (int i = 0; i < 5; i++) {
          JSONObject match = section.getJSONArray("hits").getJSONObject(i).getJSONObject("result");
          matches[i] = new GeniusMatchResult(match.getString("title_with_featured"), match.getString("url"));
        }
        sendLyricsEmbed(section, matches);
      } catch (JSONException e) {
        ce.getChannel().sendMessage("No matches found.").queue();
      }
    }

    /**
     * Sends the lyrics results embed.
     *
     * @param section main JSON body
     * @param matches query matches
     */
    private void sendLyricsEmbed(JSONObject section, GeniusMatchResult[] matches) {
      // - Title [Link](URL)
      StringBuilder descriptionBuilder = new StringBuilder();
      for (GeniusMatchResult geniusMatchResult : matches) {
        descriptionBuilder.append("- ").append(geniusMatchResult.getTitle());
        descriptionBuilder.append(" [Link](").append(geniusMatchResult.getUrl()).append(") \n");
      }
      // Embed Thumbnail
      String firstMatchImageLink = section.getJSONArray("hits").getJSONObject(0).getJSONObject("result").getString("song_art_image_url");

      EmbedBuilder embed = new EmbedBuilder();
      embed.setAuthor("Lyrics Results");
      embed.setThumbnail(firstMatchImageLink);
      embed.setDescription(descriptionBuilder);
      Settings.sendEmbed(ce, embed);
    }

    /**
     * Represents a track result from Genius API.
     *
     * @param title track title
     * @param url   track url
     * @author Danny Nguyen
     * @version 1.7.8
     * @since 1.7.2
     */
    private record GeniusMatchResult(String title, String url) {
      /**
       * Gets the track's title.
       *
       * @return track's title
       */
      private String getTitle() {
        return this.title;
      }

      /**
       * Gets the track's url.
       *
       * @return track's url
       */
      private String getUrl() {
        return this.url;
      }
    }
  }
}


