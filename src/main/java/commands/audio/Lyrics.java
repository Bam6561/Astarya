package commands.audio;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import commands.audio.objects.GeniusMatchResult;
import commands.owner.Settings;
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
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * Lyrics is a command invocation that queries Genius API for a song's lyrics.
 * <p>
 * By default, the command is set to return the first five matches.
 * </p>
 *
 * @author Danny Nguyen
 * @version 1.7.2
 * @since 1.7.2
 */
public class Lyrics extends Command {
  public Lyrics() {
    this.name = "lyrics";
    this.aliases = new String[]{"lyrics"};
    this.arguments = "[1 ++]SongName";
    this.help = "Finds lyrics of a song using Genius.";
  }

  /**
   * Checks if user provided parameters to process the lyrics command request.
   *
   * @param ce command event
   */
  @Override
  protected void execute(CommandEvent ce) {
    Settings.deleteInvoke(ce);

    String[] parameters = ce.getMessage().getContentRaw().split("\\s");
    int numberOfParameters = parameters.length - 1;

    if (numberOfParameters >= 1) {
      processLyricsRequest(ce, parameters);
    } else {
      ce.getChannel().sendMessage("Invalid number of parameters.").queue();
    }
  }

  /**
   * Combines an endpoint URL with its search query to query Genius API with.
   *
   * @param ce         command event
   * @param parameters user provided parameters
   * @throws MalformedURLException invalid URL
   */
  private void processLyricsRequest(CommandEvent ce, String[] parameters) {
    try {
      URL endpointUrlQuery = new URL("https://genius.com/api/search/song?q=" + buildSearchQuery(parameters));
      readHttpResponse(ce, endpointUrlQuery);
    } catch (MalformedURLException e) {
      System.out.println("Invalid URL.");
    }
  }

  /**
   * Builds a search query from user provided parameters.
   *
   * @param parameters user provided parameters
   * @return text containing search query
   */
  private String buildSearchQuery(String[] parameters) {
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
   * @param ce               command event
   * @param endpointUrlQuery endpoint and its search query
   * @throws IOException interrupted input stream
   */
  private void readHttpResponse(CommandEvent ce, URL endpointUrlQuery) {
    try {
      HttpURLConnection connection = (HttpURLConnection) endpointUrlQuery.openConnection();
      connection.setRequestMethod("GET");
      connection.connect();
      String httpResponse = new BufferedReader(new InputStreamReader(
          connection.getInputStream(), StandardCharsets.UTF_8)).lines().collect(Collectors.joining());
      processHttpResponse(ce, httpResponse);
    } catch (IOException e) {
      System.out.println("Connection interrupted.");
    }
  }

  /**
   * Reads the Https response as a JSON and checks if any results were found.
   *
   * @param ce           command event
   * @param httpResponse the response from the http connection
   * @throws JSONException no results found
   */
  private void processHttpResponse(CommandEvent ce, String httpResponse) {
    // Main JSON body
    JSONObject httpResponseJSON = new JSONObject(httpResponse);
    JSONObject section = (JSONObject) httpResponseJSON.getJSONObject("response").getJSONArray("sections").get(0);
    try {
      extractDataFromJSON(ce, section);
    } catch (JSONException e) {
      ce.getChannel().sendMessage("No matches found on Genius.").queue();
    }
  }

  /**
   * Extracts data from JSON fields to respond to user's lyrics request.
   *
   * @param ce      command event
   * @param section main JSON body
   */
  private void extractDataFromJSON(CommandEvent ce, JSONObject section) {
    // Title & URL from top 5 matches
    ArrayList<GeniusMatchResult> matches = new ArrayList<>();
    for (int i = 0; i < 5; i++) {
      JSONObject match = section.getJSONArray("hits").getJSONObject(i).getJSONObject("result");
      matches.add(new GeniusMatchResult(match.getString("title_with_featured"), match.getString("url")));
    }
    buildLyricsEmbed(ce, section, matches);
  }

  /**
   * Builds the lyrics results embed.
   *
   * @param ce      command event
   * @param section main JSON body
   * @param matches array list of query matches
   */
  private void buildLyricsEmbed(CommandEvent ce, JSONObject section, ArrayList<GeniusMatchResult> matches) {
    // - Title [Link](URL)
    StringBuilder lyricsEmbedDescription = new StringBuilder();
    for (GeniusMatchResult geniusMatchResult : matches) {
      lyricsEmbedDescription.append("- " + geniusMatchResult.getTitle());
      lyricsEmbedDescription.append(" [Link](" + geniusMatchResult.getUrl() + ") \n");
    }

    // Embed Thumbnail
    String firstMatchImageLink = section.getJSONArray("hits").
        getJSONObject(0).getJSONObject("result").getString("song_art_image_url");

    sendLyricsEmbed(ce, lyricsEmbedDescription.toString(), firstMatchImageLink);
  }

  /**
   * Sends the lyric results embed.
   *
   * @param ce                     command event
   * @param lyricsEmbedDescription list of title and lyrics links
   * @param firstMatchImageLink    first match's image link
   */
  private void sendLyricsEmbed(CommandEvent ce, String lyricsEmbedDescription, String firstMatchImageLink) {
    EmbedBuilder display = new EmbedBuilder();
    display.setAuthor("Lyrics Results");
    display.setThumbnail(firstMatchImageLink);
    display.setDescription(lyricsEmbedDescription);
    Settings.sendEmbed(ce, display);
  }
}


