package me.dannynguyen.astarya.commands.audio;

import org.jetbrains.annotations.NotNull;

/**
 * Converts tracks' durations to conventional readable time.
 *
 * @author Danny Nguyen
 * @version 1.8.8
 * @since 1.7.8
 */
public class TrackTime {
  /**
   * Utility methods only.
   */
  private TrackTime() {
  }

  /**
   * Converts long duration to conventional readable time.
   *
   * @param longTime duration of the track in long
   * @return readable time format
   */
  @NotNull
  public static String convertLong(long longTime) {
    long days = longTime / 86400000 % 30;
    long hours = longTime / 3600000 % 24;
    long minutes = longTime / 60000 % 60;
    long seconds = longTime / 1000 % 60;
    return (days == 0 ? "" : days < 10 ? "0" + days + ":" : days + ":") +
        (hours == 0 ? "" : hours < 10 ? "0" + hours + ":" : hours + ":") +
        (minutes == 0 ? "00:" : minutes < 10 ? "0" + minutes + ":" : minutes + ":") +
        (seconds == 0 ? "00" : seconds < 10 ? "0" + seconds : seconds);
  }
}
