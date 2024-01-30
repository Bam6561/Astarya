package astarya;

/**
 * Text is an enum collection containing Astarya's commonly sent messages.
 *
 * @author Danny Nguyen
 * @version 1.8.0
 * @since 1.7.6
 */
public class BotMessage {
  public enum Success {
    VERSION("V1.8.0");
    
    public final String text;

    Success(String text) {
      this.text = text;
    }
  }

  public enum Failure {
    MISSING_PERMISSION_MANAGE_MESSAGES("Unable to manage messages."),
    MISSING_PERMISSION_MANAGE_ROLES("Unable to manage roles."),

    INVALID_NUMBER_OF_PARAMETERS("Invalid number of parameters."),
    INVALID_QUEUE_NUMBER("Nonexistent queue number."),

    USER_NOT_IN_VC("User not in a voice channel."),
    USER_NOT_IN_SAME_VC("User not in same voice channel.");

    public final String text;

    Failure(String text) {
      this.text = text;
    }
  }
}
