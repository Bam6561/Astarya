package astarya;

/**
 * Text is an enum relating Astarya's placeholder references with their text value.
 *
 * @author Danny Nguyen
 * @version 1.7.6
 * @since 1.7.6
 */
public enum Text {
  VERSION("V1.7.7"),
  MISSING_DELETE_PERMISSION("Insufficient permissions to delete message.");

  private final String value;

  Text(String text) {
    this.value = text;
  }

  public String value() {
    return this.value;
  }
}
