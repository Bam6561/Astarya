package commands.audio.objects;

/**
 * GeniusMatchResult is an object representing a track result from Genius API.
 *
 * @author Danny Nguyen
 * @version 1.7.2
 * @since 1.7.2
 */
public class GeniusMatchResult {
  private String title;
  private String url;

  public GeniusMatchResult(String title, String url) {
    this.title = title;
    this.url = url;
  }

  public String getTitle() {
    return this.title;
  }

  public String getUrl() {
    return this.url;
  }

  private void setTitle(String title) {
    this.title = title;
  }

  private void setUrl(String url) {
    this.url = url;
  }
}
