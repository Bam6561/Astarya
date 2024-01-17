package astarya;

/**
 * Text is an enum relating Astarya's placeholder references with their text value.
 *
 * @author Danny Nguyen
 * @version 1.7.6
 * @since 1.7.6
 */
public enum Text {
  VERSION("V1.7.8"),
  MISSING_DELETE_PERMISSION("Insufficient permissions to delete message."),
  NOT_IN_VC("User not in a voice channel."),
  NOT_IN_SAME_VC("User not in same voice channel."),
  INVALID_QUEUE_NUMBER("Queue number does not exist."),
  SPOTIFY_API_ERROR("Something went wrong while trying to access SpotifyAPI.");


  private final String value;

  Text(String text) {
    this.value = text;
  }

  public String value() {
    return this.value;
  }
}
