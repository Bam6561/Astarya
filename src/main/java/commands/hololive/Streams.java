package commands.hololive;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import commands.owner.Settings;
import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.EmbedBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class Streams extends Command {
  private EmbedBuilder display = new EmbedBuilder();
  private boolean dailyUpdate = false;

  public Streams() {
    this.name = "streams";
    this.aliases = new String[]{"streams"};
    this.arguments = "[0]Streams";
    this.help = "Displays upcoming and current HoloLive streams.";
  }

  @Override
  protected void execute(CommandEvent ce) {
    Settings.deleteInvoke(ce);
    if (!dailyUpdate) { // Not updated today
      display.clear(); // Empty display
      getStreams(display, getChannelNames(), getChannelIDs()); // Update display
      dailyUpdate = true; // Updated today
      // Available for update again in 24h
      new java.util.Timer().schedule(new java.util.TimerTask() {
        public void run() {
          dailyUpdate = false;
        }
      }, 86400000);
    }
    Settings.sendEmbed(ce, display);
  }

  private void getStreams(EmbedBuilder display, String[] channelNames, String[] channelIds) {
    // Last update time
    LocalDate date = LocalDate.now();
    LocalTime time = LocalTime.now();
    Calendar calendar = new GregorianCalendar();
    TimeZone timeZone = calendar.getTimeZone();
    DateTimeFormatter dt1 = DateTimeFormatter.ofPattern("EEE MMM dd");
    DateTimeFormatter dt2 = DateTimeFormatter.ofPattern("hh:mm:ss");
    DateTimeFormatter dt3 = DateTimeFormatter.ofPattern("yyyy");
    StringBuilder dateTime = new StringBuilder();
    dateTime.append(date.format(dt1)).append(" ").append(time.format(dt2)).append(" ").
        append(timeZone.getDisplayName()).append(" ").append(date.format(dt3));
    StringBuilder liveStreamsDescription = new StringBuilder();
    liveStreamsDescription.append("**Last Updated:** `").append(dateTime).append("`\n");
    liveStreamsDescription.append("__**Livestreams:**__").append("\n"); // Livestreams
    for (int i = 0; i < channelIds.length; i++) { // Check channels
      StringBuilder liveStreams = new StringBuilder();
      StringBuilder upcomingStreams = new StringBuilder();
      // YouTube API request url
      Dotenv dotenv = Dotenv.load();
      String youtubeAPIRequest = "https://youtube.googleapis.com/youtube/v3/search" +
          "?part=snippet&channelId=" + channelIds[i] + "&maxResults=10&type=video&key="
          + dotenv.get("YOUTUBE_API_KEY");
      // Search channel's videos (10)
      JSONObject jsonResponse = getJSONResponse(youtubeAPIRequest);
      if (!(jsonResponse.getJSONObject("pageInfo").getInt("totalResults") == 0)) {
        // Found upcoming & live streams
        JSONArray videoList = new JSONArray(jsonResponse.getJSONArray("items").toString());
        for (int j = 0; j < videoList.length(); j++) {
          String liveBroadcastContent = videoList.getJSONObject(j).getJSONObject("snippet").
              getString("liveBroadcastContent");
          if (liveBroadcastContent.equals("live") || (liveBroadcastContent.equals("upcoming"))) {
            // Get video ID
            String videoID = videoList.getJSONObject(j).getJSONObject("id").getString("videoId");
            // YouTube API request url
            youtubeAPIRequest = "https://youtube.googleapis.com/youtube/v3/videos" +
                "?part=snippet&part=liveStreamingDetails&id=" + videoID + "&key=" + dotenv.get("YOUTUBE_API_KEY");
            // Search for video's details
            jsonResponse = getJSONResponse(youtubeAPIRequest);
            JSONArray video = new JSONArray(jsonResponse.getJSONArray("items").toString());
            for (int k = 0; k < 1; k++) { // Video details
              try { // Livestream (Upcoming streams have no concurrent viewers)
                video.getJSONObject(k).getJSONObject("liveStreamingDetails").getString("concurrentViewers");
                // Video title
                String videoTitle = videoList.getJSONObject(j).getJSONObject("snippet").getString("title");
                // Append livestream entry
                liveStreams.append("[").append(videoTitle).append("](https://www.youtube.com/watch?v=")
                    .append(videoID).append(") ").append("\n");
              } catch (JSONException error) { // Parse upcoming stream details
                // Scheduled start time
                String scheduledStartTime = video.getJSONObject(0).getJSONObject("liveStreamingDetails").
                    getString("scheduledStartTime");
                TemporalAccessor ta = DateTimeFormatter.ISO_INSTANT.parse(scheduledStartTime);
                Instant instant = Instant.from(ta);
                Date d = Date.from(instant);
                // Video title
                String videoTitle = videoList.getJSONObject(j).getJSONObject("snippet").getString("title");
                // Append upcoming stream entry
                upcomingStreams.append("[").append(videoTitle).append("](https://www.youtube.com/watch?v=")
                    .append(videoID).append(") `").append(d).append("`\n");
              }
            }
          }
        }
      }
      // Add livestreams under channel name if they exist
      if (!liveStreams.isEmpty()) {
        liveStreamsDescription.append("**").append(channelNames[i]).append("**\n").
            append(liveStreams);
        liveStreams.delete(0, liveStreams.length());
      }
      // Add upcoming streams under channel name if they exist
      if (!upcomingStreams.isEmpty()) {
        String channelName = "**" + channelNames[i] + "**";
        display.addField(channelName, upcomingStreams.toString(), false);
        upcomingStreams.delete(0, upcomingStreams.length());
      }
    }
    liveStreamsDescription.append("\n__**Upcoming:**__");
    display.setDescription(liveStreamsDescription);
  }

  private JSONObject getJSONResponse(String youtubeAPIRequest) {
    try { // GET Request from YouTube
      URL youtubeAPIRequestURL = new URL(youtubeAPIRequest);
      HttpsURLConnection connection = (HttpsURLConnection) youtubeAPIRequestURL.openConnection();
      // Parse request results
      InputStream inputStream = connection.getInputStream();
      InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
      BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
      String inputLine;
      StringBuilder response = new StringBuilder();
      while ((inputLine = bufferedReader.readLine()) != null) {
        response.append(inputLine);
      }
      bufferedReader.close();
      inputStreamReader.close();
      inputStream.close();
      connection.disconnect();
      // Return request results as jsonObject format
      return new JSONObject(response.toString());
    } catch (MalformedURLException error) {
      return null;
    } catch (IOException error) {
      return null;
    }
  }

  private String[] getChannelNames() {
    return new String[]{"Tokino Sora", "Roboco-san", "Sakura Miko", "Hoshimachi Suisei", "AZKi",
        "Nozora Mel", "Shirakami Fubuki", "Natsuiro Matsuri", "Aki Rosenthal", "Akai Haato", "Minato Aqua",
        "Murasaki Shion", "Nakiri Ayame", "Yuzuki Choco", "Oozora Subaru", "Ookami Mio", "Nekomata Okayu",
        "Inugami Korone", "Usada Pekora", "Uruha Rushia", "Shiranui Flare", "Shirogane Noel", "Houshou Marine",
        "Amane Kanata", "Tsunomaki Watame", "Tokoyami Towa", "Himemori Luna", "Yukihana Lamy", "Momosuzu Nene",
        "Shishiro Botan", "Omaru Polka", "Ayunda Risu", "Moona Hoshinova", "Airani Iofifteen", "Kureiji Ollie",
        "Anya Melfissa", "Paviola Reine", "Mori Calliope", "Takanashi Kiara", "Ninomae Ina'nis", "Gawr Gura",
        "Amelia Watson", "IRyS", "Tsukumo Sana", "Ceres Fauna", "Ouro Kronii", "Nanashi Mumei", "Hakos Baelz",
        "Hanasaki Miyabi", "Kanade Izuru", "Arurandeisu", "Rikka", "Astel Leda", "Kishido Temma", "Yukoku Roberu",
        "Kageyama Shien", "Aragami Oga"};
  }

  private String[] getChannelIDs() {
    return new String[]{"UCp6993wxpyDPHUpavwDFqgg", "UCDqI2jOz0weumE8s7paEk6g", "UC-hM6YJuNYVAmUWxeIr9FeA",
        "UC5CwaMl1eIgY8h02uZw7u8A", "UC0TXe_LYZ4scaW2XMyi5_kw", "UCD8HOxPs4Xvsm8H0ZxXGiBw", "UCdn5BQ06XqgXoAxIhbqw5Rg",
        "UCQ0UDLQCjY0rmuxCDE38FGg", "UCFTLzh12_nrtzqBPsTCqenA", "UC1CfXB_kRs3C-zaeTG3oGyg", "UC1opHUrw8rvnsadT-iGp7Cg",
        "UCXTpFs_3PqI41qX2d9tL2Rw", "UC7fk0CB07ly8oSl0aqKkqFg", "UC1suqwovbL1kzsoaZgFZLKg", "UCvzGlP9oQwU--Y0r9id_jnA",
        "UCp-5t9SrOQwXMU7iIjQfARg", "UCvaTdHTWBGv3MKj3KVqJVCw", "UChAnqc_AY5_I3Px5dig3X1Q", "UC1DCedRgGHBdm81E1llLhOQ",
        "UCl_gCybOJRIgOXw6Qb4qJzQ", "UCvInZx9h3jC2JzsIzoOebWg", "UCdyqAaZDKHXg4Ahi7VENThQ", "UCCzUftO8KOVkV4wQG1vkUvg",
        "UCZlDXzGoo7d44bwdNObFacg", "UCqm3BQLlJfvkTsX_hvm0UmA", "UC1uv2Oq6kNxgATlCiez59hw", "UCa9Y57gfeY0Zro_noHRVrnw",
        "UCFKOVgVbGmX65RxO3EtH3iw", "UCAWSyEs_Io8MtpY3m-zqILA", "UCUKD-uaobj9jiqB-VXt71mA", "UCK9V2B22uJYu3N7eR_BT9QA",
        "UCOyYb1c43VlX9rc_lT6NKQw", "UCP0BspO_AMEe3aQqqpo89Dg", "UCAoy6rzhSf4ydcYjJw3WoVg", "UCYz_5n-uDuChHtLo7My1HnQ",
        "UC727SQYUvx5pDDGQpTICNWg", "UChgTyjG-pdNvxxhdsXfHQ5Q", "UCL_qhgtOy0dy1Agp8vkySQg", "UCHsx4Hqa-1ORjQTh9TYDhww",
        "UCMwGHR0BTZuLsmjY_NT5Pwg", "UCoSrY_IQQVpmIRZ9Xf-y93g", "UCyl1z3jo3XHR1riLFKG5UAg", "UC8rcEBzJSleTkf_-agPM20g",
        "UCsUj0dszADCGbF3gNrQEuSQ", "UCO_aKKYxn4tvrqPjcTzZ6EQ", "UCmbs8T6MWqUHP1tIQvSgKrg", "UC3n5uGu18FoCy23ggWWp8tA",
        "UCgmPnx-EEeOrZSg5Tiw7ZRQ", "UC6t3-_N8A6ME1JShZHHqOMw", "UCZgOv3YDEs-ZnZWDYVwJdmA", "UCKeAhJvy8zgXWbh9duVjIaQ",
        "UC9mf_ZVpouoILRY9NUIaK-w", "UCNVEsYbiZjH5QLmGeSgTSzg", "UCGNI4MENvnsymYjKiZwv9eg", "UCANDOlYTJT7N5jlRC3zfzVA",
        "UChSvpZYRPh0FvG4SJGSga3g", "UCwL7dgTxKo8Y4RFIKWaf8gA"
    };
  }
}