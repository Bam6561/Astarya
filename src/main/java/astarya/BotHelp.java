package astarya;

/**
 * BotHelp is an enum collection containing the bot's help messages.
 *
 * @author Danny Nguyen
 * @version 1.7.14
 * @since 1.7.14
 */
public class BotHelp {
  public enum Description {
    CHOOSE("Chooses randomly between any number of options. The options are parameters separated by commas."),
    CLEARQUEUE("Clears the track queue."),
    COINFLIP("Flips a coin any number of times. Parameter dictates how many times (1-10) to flip the coin."),
    COLOR("Assigns or removes color roles from the user. Using \"clean\" " +
        "as the server owner will remove all empty color roles."),
    CREDITS("Shows a list of credits for Astarya."),
    DELETE("Deletes a number of recent messages. Parameter provides amount to delete (2-100)."),
    EMOTE("Provides the mentioned custom emote as a file. Parameter is the requested emote."),
    HELP("Provides documentation on Astarya's commands. Parameter describes more detailed command usage."),
    HIGHORLOW("Guess if the next number will be higher or lower!"),
    INFO("Details information about Astarya and its developer."),
    JOIN("Joins the same voice channel as the user."),
    LEAVE("Leaves the voice channel it's in."),
    LOOP("Loops the current track."),
    LYRICS("Finds lyrics of a song using Genius."),
    NOWPLAYING("Shows what track is currently playing."),
    PANDORASBOX("Sends a random scenario prompt. Prompts' subjects are substituted if parameters are provided."),
    PAUSE("Pauses the audio player. Astarya's activity changes may be rate limited if done rapidly."),
    PING("Responds with the response time of Astarya in milliseconds."),
    PLAY("""
        Adds a track to the track queue. Spotify playlists are limited to the\040
        most recent 100 songs added. Spotify albums are limited to 50 songs at a time.
        **Sources**
        > - YouTube: links/playlists
        > - Discord: media links
        > - Spotify: songs/playlists/albums
        **Supported File Types**
        > MP3, FLAC, WAV, Matroska/WebM, MP4/M4A, OGG streams, AAC streams"""),
    PLAYNEXT("Sets the next track to be played in the track queue."),
    POLL("Creates a reaction vote with up to 10 options. The options are parameters separated by commas."),
    PROFILE("Sends information about a user."),
    QUEUE("Provides a list of tracks queued."),
    REMIND("""
        Sets a timer and alerts the user when the time expires for up to a day's maximum length.\040
        Parameters provide the time duration, type, and event name. Astarya recognizes the following time types:
        > - 1 parameter: (0-7)d, (0-168)h, (0-10080)m, (0-604800)s
        > - 2+ parameters: days, day, d, hours, hour, hrs, hr, h,
        minutes, minute, mins, min, m, seconds, second, secs, sec, s."""),
    REMOVE("Removes track(s) from the track queue."),
    RETURN("Returns a recently skipped track to the track queue."),
    ROLL("Dice roll and random integer generator. No parameters to roll once. " +
        "One parameter to roll 1-10 times. Three parameters to set how many times to roll" +
        " a custom range of minimum and maximum values."),
    SEARCHTRACK("Searches for a track to add to the track queue."),
    SERVER("Provides information on the Discord server."),
    SETPOSITION("Sets the position of the currently playing track. "
        + "\":\" separates the time types from hours:minutes:seconds."),
    SETTINGS("Provides information on Astarya settings."),
    SHUFFLE("Shuffles the track queue."),
    SHUTDOWN("Shuts Astarya down."),
    SKIP("Skips the currently playing track. Astarya's activity changes may be rate limited if done rapidly."),
    SWAP("Swaps the position of a track in queue with another.");

    public final String text;

    Description(String text) {
      this.text = text;
    }
  }

  public enum Alias {
    CHOOSE("choose, pick"),
    CLEARQUEUE("clearqueue, clear"),
    COINFLIP("coinflip, flip"),
    COLOR("color"),
    CREDITS("credits"),
    DELETE("delete, purge"),
    EMOTE("emote, emoji"),
    HELP("help"),
    HIGHORLOW("highorlow, guess"),
    INFO("info, about"),
    JOIN("join, j"),
    LEAVE("leave, l, disconnect, dc"),
    LOOP("loop, repeat"),
    LYRICS("lyrics"),
    NOWPLAYING("nowplaying, np"),
    PANDORASBOX("pandorasbox, pb"),
    PAUSE("pause, stop"),
    PING("ping, ms"),
    PLAY("play, p"),
    PLAYNEXT("playnext, after"),
    POLL("poll, vote"),
    PROFILE("profile, whois, user"),
    QUEUE("queue, q"),
    REMIND("remind, timer"),
    REMOVE("remove, r"),
    RETURN("return, ret"),
    ROLL("roll, rng, dice"),
    SEARCHTRACK("searchtrack, search, st"),
    SERVER("server"),
    SETPOSITION("setposition, setpos"),
    SETTINGS("settings, config"),
    SHUFFLE("shuffle, mix"),
    SHUTDOWN("shutdown"),
    SKIP("skip, s, next"),
    SWAP("swap, switch");

    public final String text;

    Alias(String text) {
      this.text = text;
    }
  }

  public enum Parameter {
    CHOOSE("[1, ++]Options"),
    CLEARQUEUE("[0]ClearQueue"),
    COINFLIP("[0]Once [1]NumberOfFlips"),
    COLOR("[1]#HexColor/clear/clean"),
    CREDITS("[0]Credits"),
    DELETE("[1]NumberOfMessages"),
    EMOTE("[1]Emote"),
    HELP("[0]MainMenu [1]CommandName"),
    HIGHORLOW("[0]HighOrLow"),
    INFO("[0]Info"),
    JOIN("[0]Join"),
    LEAVE("[0]Leave"),
    LOOP("[0]Loop"),
    LYRICS("[1 ++]SongName"),
    NOWPLAYING("[0]NowPlaying"),
    PANDORASBOX("[0]Self [1]VC/DC/Name [2 ++]Phrase"),
    PAUSE("[0]Pause"),
    PING("[0]Ping"),
    PLAY("[1]URL [2 ++]YouTubeQuery"),
    PLAYNEXT("[1]QueueNumber"),
    POLL("[2, ++]Options"),
    PROFILE("[0]Self [1]Mention/UserId/<@UserId> [1+]Name/Nickname"),
    QUEUE("[0]Queue [1]PageNumber"),
    REMIND("[1]TimeDuration&TimeType/Time [2]TimeDuration/TimeType/EventName [3++]EventName"),
    REMOVE("[1]QueueNumber [1 ++]QueueNumbers"),
    RETURN("[0]RecentlySkipped [1]SkippedStackNumber"),
    ROLL("[0]Once [1]NumberOfRolls [2]Minimum [3]Maximum"),
    SEARCHTRACK("[1 ++]YouTubeQuery -> [1]SearchResultNumber"),
    SERVER("[0]Server"),
    SETPOSITION("[1]TimeString"),
    SETTINGS("[0]MainMenu [1]Setting [2]True/False"),
    SHUFFLE("[0]Shuffle"),
    SHUTDOWN("[0]Shutdown"),
    SKIP("[0]Skip"),
    SWAP("[1]QueueNumber [2]QueueNumber");

    public final String text;

    Parameter(String text) {
      this.text = text;
    }
  }

  public enum Example {
    CHOOSE("choose Take out the trash, Do the laundry, Walk the dog"),
    CLEARQUEUE("clearqueue"),
    COINFLIP("flip | flip 5"),
    COLOR("color #7EC2FE | color clear"),
    CREDITS("credits"),
    DELETE("delete 15"),
    EMOTE("emote :watameSnacks:"),
    HELP("help help"),
    HIGHORLOW("highorlow"),
    INFO("info"),
    JOIN("join"),
    LEAVE("leave"),
    LOOP("loop"),
    LYRICS("lyrics duck song"),
    NOWPLAYING("nowplaying"),
    PANDORASBOX("pandorasbox, pandorasbox vc, pandorasbox dc, pandorasbox John Constantine"),
    PAUSE("pause"),
    PING("ping"),
    PLAY("play https://www.youtube.com/watch?v=dQw4w9WgXcQ | play Cleverly Disguised Rickrolls"),
    PLAYNEXT("playnext 3"),
    POLL("poll hot pizza, cold pizza"),
    PROFILE("profile | profile @Bam | " + "profile 204448598539239424 | " +
        "profile <@204448598539239424> | profile Bam | profile Bam's Nickname"),
    QUEUE("queue | queue 1"),
    REMIND("remind (0-7)d | remind (0-168)h | remind (0-10080)m | " +
        "remind (0-604800)s | remind TimeDurationTimeType EventName | remind TimeDuration TimeType EventName"),
    REMOVE("remove 1 | remove 2 4 5"),
    RETURN("return | return 1"),
    ROLL("roll | roll 10 | roll 2 25 50"),
    SEARCHTRACK("search towa pallete"),
    SERVER("server"),
    SETPOSITION("setposition 150 | setposition 2:30"),
    SETTINGS("settings | settings deleteinvoke | settings deleteinvoke true"),
    SHUFFLE("shuffle"),
    SHUTDOWN("shutdown"),
    SKIP("skip"),
    SWAP("swap 2 4");

    public final String text;

    Example(String text) {
      this.text = text;
    }
  }
}
