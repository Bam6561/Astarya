package me.dannynguyen.astarya.utility;

/**
 * TextReader is a utility class that identifies whether a string is in hex code format.
 *
 * @author Danny Nguyen
 * @version 1.8.0
 * @since 1.8.0
 */
public class TextReader {
  private TextReader() {
  }

  /**
   * Determines if a role name is a hex color code.
   *
   * @param roleName role name
   * @return is a hex color code
   */
  public static boolean isHexColorCode(String roleName) {
    if (!roleName.startsWith("#") || roleName.length() != 7) {
      return false;
    }

    for (char c : roleName.substring(1).toCharArray()) {
      switch (c) {
        case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' -> {
        }
        default -> {
          return false;
        }
      }
    }
    return true;
  }
}
