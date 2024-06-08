package me.bam6561.astarya.enums;

import org.jetbrains.annotations.NotNull;

/**
 * Bot messages.
 *
 * @author Danny Nguyen
 * @version 1.8.6
 * @since 1.7.6
 */
public enum BotMessage {
  /**
   * Unable to manage messages.
   */
  MISSING_PERMISSION_MANAGE_MESSAGES("Unable to manage messages."),

  /**
   * Unable to manage roles.
   */
  MISSING_PERMISSION_MANAGE_ROLES("Unable to manage roles."),

  /**
   * Invalid number of parameters.
   */
  INVALID_NUMBER_OF_PARAMETERS("Invalid number of parameters."),

  /**
   * Invalid queue number.
   */
  INVALID_QUEUE_NUMBER("Nonexistent queue number."),

  /**
   * User not in a voice channel.
   */
  USER_NOT_IN_VC("User not in a voice channel."),

  /**
   * User not in same voice channel.
   */
  USER_NOT_IN_SAME_VC("User not in same voice channel.");

  /**
   * Message content.
   */
  private final String message;

  /**
   * Associates a message with its content.
   *
   * @param message message content
   */
  BotMessage(String message) {
    this.message = message;
  }

  /**
   * Gets a message's content.
   *
   * @return message content
   */
  @NotNull
  public String getMessage() {
    return this.message;
  }
}
