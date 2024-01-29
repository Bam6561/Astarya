package astarya;

/**
 * Text is an enum collection containing Astarya's commonly sent messages.
 *
 * @author Danny Nguyen
 * @version 1.7.12
 * @since 1.7.6
 */
public class BotMessage {
  /**
   * Sent on success.
   */
  public enum Success {
    VERSION("V1.7.15"),

    COLORROLE_CLEAR_ROLES("Cleared color roles."),
    COLORROLE_CLEANED_UP_ROLES("Cleaned up empty color roles."),
    CREDITS_THANK_YOU("Thank you to the following resources used in " +
        "Astarya's development, as well as all my friends who use the bot " +
        "regularly and leave feedback so the experience is as bug-free as possible."),
    CREDITS_APIS("> [Discord](https://discord.com) | " +
        "[Genius](https://genius.com) | " +
        "[Spotify](https://open.spotify.com)"),
    CREDITS_LIBRARIES_WRAPPERS("> [JDA](https://github.com/DV8FromTheWorld/JDA) | " +
        "[JDA-Chewtils](https://github.com/Chew/JDA-Chewtils) | " +
        "[LavaPlayer](https://github.com/sedmelluq/lavaplayer) | " +
        "[Spotify Web API Java](https://github.com/spotify-web-api-java/spotify-web-api-java)"),
    CREDITS_REFERENCES("> [Kody Simpson](https://www.youtube.com/@KodySimpson) | " +
        "[MenuDocs](https://www.youtube.com/@MenuDocs) | " +
        "[TechToolBox](https://www.youtube.com/@TechToolboxOfficial)"),
    PAUSE_PLAYER_PAUSE("Audio player paused."),
    PAUSE_PLAYER_RESUME("Audio player resumed."),
    LYRICS_NO_MATCHES("No matches found."),
    RETURN_NO_SKIPPED_TRACKS("No recently skipped tracks."),
    SHUTDOWN("Well, it was fun while it lasted. Change the world... " +
        "my final message. Goodbye. **Astarya is shutting down.**");

    public final String text;

    Success(String text) {
      this.text = text;
    }
  }

  /**
   * Sent on failure.
   */
  public enum Failure {
    MISSING_PERMISSION_MANAGE_MESSAGES("Unable to manage messages."),
    MISSING_PERMISSION_MANAGE_ROLES("Unable to manage roles."),
    MISSING_SPOTIFY_API_KEY("Unable to play Spotify links. No Spotify API key provided in .env file."),

    INVALID_NUMBER_OF_PARAMETERS("Invalid number of parameters."),
    INVALID_QUEUE_NUMBER("Nonexistent queue number."),

    USER_NOT_IN_VC("User not in a voice channel."),
    USER_NOT_IN_SAME_VC("User not in same voice channel."),
    BOT_NOT_IN_VC("Not in a voice channel."),

    CHOOSE_EMPTY_OPTION("Empty option."),
    CHOOSE_SEPARATE_OPTIONS("Separate options with a comma."),
    COINFLIP_RANGE("Provide between 1-10 times to flip coin."),
    COLORROLE_PARAMETERS("Provide a hex color code `#ffffff` or `clear`."),
    COLORROLE_NOT_HEX("Invalid color code."),
    DELETE_RANGE("Provide between 2-100 messages to clear."),
    EMOTE_SPECIFY_EMOTE("Provide an emote."),
    HIGHORLOW_ONGOING("Please wait until current high or low game finishes or expires."),
    PLAY_INVALID_SPOTIFY_ID("Invalid Spotify track id."),
    PLAY_INVALID_SPOTIFY_PLAYLIST_ID("Invalid Spotify playlist id."),
    PLAY_INVALID_SPOTIFY_ALBUM_ID("Invalid Spotify album id."),
    PLAY_UNSUPPORTED_SPOTIFY_FEATURE("Spotify feature not supported."),
    PLAYNEXT_SPECIFY("Provide next track number."),
    POLL_SEPARATE_OPTIONS("Provide options separated by a comma."),
    POLL_EMPTY_OPTION("Empty option."),
    POLL_RANGE("Provide between than 1-10 options."),
    PROFILE_USER_NOT_FOUND("User not found."),
    QUEUE_SPECIFY("Provide queue page number."),
    REMIND_NO_TIME_TYPES("No time types provided."),
    REMIND_INVALID_INPUT("Provide a valid numerical value followed by an accepted time type."),
    REMIND_RANGE_EXCEEDED("Can only set timer for maximum length of a week."),
    REMOVE_SPECIFY("Provide queue number to be removed."),
    REMOVE_SPECIFY_GROUP("Provide queue numbers to be removed with a space between each."),
    ROLL_INVALID_INPUT("Invalid parameter format."),
    ROLL_RANGE("Provide between 1-10 times to roll dice."),
    ROLL_RANGES("Provide between 1-10 for number of rolls and range."),
    ROLL_RANGE_RNG("Provide between 1-10 times to generate numbers."),
    ROLL_NEGATIVE("Minimum and maximum cannot be negative."),
    ROLL_EQUAL("Minimum cannot be equal to maximum."),
    ROLL_LARGER("Minimum cannot be larger than maximum."),
    RETURN_SPECIFY("Provide stack number to be returned."),
    SEARCHTRACK_RANGE("Responses must be in range of 1-5."),
    SEARCHTRACK_TIMED_OUT("No response. Search timed out."),
    SEARCHTRACK_SPECIFY("Provide result number."),
    SETPOSITION_NOTHING_PLAYING("Nothing is currently playing."),
    SETPOSITION_INVALID_TIME("Invalid time frame. Provide hh:mm:ss."),
    SETPOSITION_EXCEED_TRACK_LENGTH("Requested position exceeds track length."),
    SKIP_NOTHING_TO_SKIP("Nothing to skip."),
    SWAP_SPECIFY("Provide numbers to swap tracks in track queue."),
    SETTINGS_NOT_FOUND("Setting not found."),
    SETTINGS_TRUE_FALSE("Provide true or false."),
    SETTINGS_EMBED_DECAY_RANGE("Provide between 15 - 120 seconds."),

    PLAYERMANAGER("Use play command to queue tracks."),

    SERVER_OWNER_ONLY("Server owner only command."),

    ERROR_SPOTIFY_API("Something went wrong while trying to access SpotifyAPI."),
    ERROR_UNABLE_TO_FIND_TRACK("Unable to find track."),
    ERROR_UNABLE_TO_LOAD_TRACK("Unable to load track."),
    ERROR_CONNECTION_INTERRUPTED("Connection interrupted.");

    public final String text;

    Failure(String text) {
      this.text = text;
    }
  }
}
