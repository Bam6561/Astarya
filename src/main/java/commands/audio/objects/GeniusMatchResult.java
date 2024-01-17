package commands.audio.objects;

/**
 * GeniusMatchResult is an object representing a track result from Genius API.
 *
 * @author Danny Nguyen
 * @version 1.7.8
 * @since 1.7.2
 */
public record GeniusMatchResult(String title, String url) {

  public String getTitle() {
    return this.title;
  }

  public String getUrl() {
    return this.url;
  }
}
