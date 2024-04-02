package me.dannynguyen.astarya.commands.about;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import me.dannynguyen.astarya.commands.owner.Settings;
import net.dv8tion.jda.api.EmbedBuilder;

/**
 * Command invocation that provides documentation on Astarya's commands.
 *
 * @author Danny Nguyen
 * @version 1.9.3
 * @since 1.0
 */
public class Help extends Command {
  /**
   * Associates the command with its parameters.
   */
  public Help() {
    this.name = "help";
    this.aliases = new String[]{"help"};
    this.arguments = "[0]MainMenu [1]CommandName";
    this.help = "Provides documentation on Astarya's commands.";
  }

  /**
   * Sends an embed:
   * <ul>
   *  <li> containing all {@link Help.Command} available
   *  <li> containing a {@link Help.Command}'s documentation
   *  <li> referencing the online wiki
   * </ul>
   *
   * @param ce command event
   */
  @Override
  protected void execute(CommandEvent ce) {
    Settings.deleteInvoke(ce);

    String[] parameters = ce.getMessage().getContentRaw().split("\\s");
    int numberOfParameters = parameters.length - 1;

    EmbedBuilder embed = new EmbedBuilder();
    switch (numberOfParameters) {
      case 0 -> new HelpRequest(embed).setHelpMenu();
      case 1 -> new HelpRequest(embed).interpretCommand(parameters[1].toLowerCase());
      default -> embed.setDescription("Type `" + Settings.getPrefix() + "commands` " +
          "to get a list of commands available. You can also refer to " +
          "[Astarya's Wiki](https://github.com/Bam6561/Astarya/wiki).");
    }
    Settings.sendEmbed(ce, embed);
  }

  /**
   * Represents a Help command embed set operation.
   *
   * @param embed embed being set
   * @author Danny Nguyen
   * @version 1.8.7
   * @since 1.8.6
   */
  private record HelpRequest(EmbedBuilder embed) {
    /**
     * Sets the embed to contain all commands available.
     */
    private void setHelpMenu() {
      embed.setAuthor("Help");
      embed.setDescription("Type `" + Settings.getPrefix() + "help <CommandName>` " +
          "for more details on each command. Alternatively, see " +
          "[Astarya's Wiki](https://github.com/Bam6561/Astarya/wiki).");
      embed.addField("About", "> credits | help | info | ping ", true);
      embed.addField("Audio", "> clearQueue | join | leave | loop | " +
          "nowPlaying | pause | playNext | play | queue | remove | return | " +
          "searchTrack | setPosition | shuffle | skip | swap", true);
      embed.addField("Games", "> choose | coinflip | highorlow | pandorasbox | roll", true);
      embed.addField("Owner", "> delete | settings | shutdown", true);
      embed.addField("Utility", "> color | emote | jpg | poll | profile | remind | server", true);
    }

    /**
     * Determines which {@link Help.Command} to send the documentation for.
     *
     * @param commandName command name
     */
    private void interpretCommand(String commandName) {
      switch (commandName) {
        case "choose", "pick" -> setCommandDetails(Help.Command.CHOOSE);
        case "clearqueue", "clear" -> setCommandDetails(Help.Command.CLEARQUEUE);
        case "coinflip", "flip" -> setCommandDetails(Help.Command.COINFLIP);
        case "color" -> setCommandDetails(Help.Command.COLOR);
        case "credits" -> setCommandDetails(Help.Command.CREDITS);
        case "delete", "purge" -> setCommandDetails(Help.Command.DELETE);
        case "emote", "emoji" -> setCommandDetails(Help.Command.EMOTE);
        case "help" -> setCommandDetails(Help.Command.HELP);
        case "highorlow", "guess" -> setCommandDetails(Help.Command.HIGHORLOW);
        case "info", "about" -> setCommandDetails(Help.Command.INFO);
        case "join", "j" -> setCommandDetails(Help.Command.JOIN);
        case "jpg" -> setCommandDetails(Help.Command.JPG);
        case "leave", "l", "disconnect", "dc" -> setCommandDetails(Help.Command.LEAVE);
        case "loop", "repeat" -> setCommandDetails(Help.Command.LOOP);
        case "lyrics" -> setCommandDetails(Help.Command.LYRICS);
        case "nowplaying", "np" -> setCommandDetails(Help.Command.NOWPLAYING);
        case "pandorasbox", "pb" -> setCommandDetails(Help.Command.PANDORASBOX);
        case "pause", "stop" -> setCommandDetails(Help.Command.PAUSE);
        case "ping", "ms" -> setCommandDetails(Help.Command.PING);
        case "play", "p" -> setCommandDetails(Help.Command.PLAY);
        case "playnext", "after" -> setCommandDetails(Help.Command.PLAYNEXT);
        case "poll", "vote" -> setCommandDetails(Help.Command.POLL);
        case "profile", "whois", "user" -> setCommandDetails(Help.Command.PROFILE);
        case "queue", "q" -> setCommandDetails(Help.Command.QUEUE);
        case "remind", "timer" -> setCommandDetails(Help.Command.REMIND);
        case "remove", "r" -> setCommandDetails(Help.Command.REMOVE);
        case "return", "ret" -> setCommandDetails(Help.Command.RETURN);
        case "roll", "rng", "dice" -> setCommandDetails(Help.Command.ROLL);
        case "searchtrack", "search", "st" -> setCommandDetails(Help.Command.SEARCHTRACK);
        case "server" -> setCommandDetails(Help.Command.SERVER);
        case "setposition", "setpos" -> setCommandDetails(Help.Command.SETPOSITION);
        case "settings", "config" -> setCommandDetails(Help.Command.SETTINGS);
        case "shuffle", "mix" -> setCommandDetails(Help.Command.SHUFFLE);
        case "shutdown" -> setCommandDetails(Help.Command.SHUTDOWN);
        case "skip", "s", "next" -> setCommandDetails(Help.Command.SKIP);
        case "swap", "switch" -> setCommandDetails(Help.Command.SWAP);
        default -> {
          embed.setAuthor("Help: Command Not Found");
          embed.setDescription("Type `" + Settings.getPrefix() + "commands` to get a list of commands available. " +
              "You can also refer to [Astarya's Wiki](https://github.com/Bam6561/Astarya/wiki).");
        }
      }
    }

    /**
     * Sets the embed to contain documentation for a command.
     *
     * @param command {@link Help.Command}
     */
    private void setCommandDetails(Help.Command command) {
      embed.setAuthor(command.getTitle());
      embed.setDescription(command.getDescription());
      embed.addField("Aliases", command.getAliases(), false);
      embed.addField("Parameters", command.getParameters(), false);
      embed.addField("Examples", command.getExamples(), false);
    }
  }

  /**
   * Types of commands.
   */
  private enum Command {
    /**
     * {@link me.dannynguyen.astarya.commands.games.Choose}
     */
    CHOOSE("Help: Choose",
        "Chooses randomly between any number of options. The options are parameters separated by commas.",
        "choose, pick",
        "[1, ++]Options",
        "choose Take out the trash, Do the laundry, Walk the dog"),

    /**
     * {@link me.dannynguyen.astarya.commands.audio.ClearQueue}
     */
    CLEARQUEUE("Help: ClearQueue",
        "Clears the track queue.",
        "clearqueue, clear",
        "[0]ClearQueue",
        "clearqueue"),

    /**
     * {@link me.dannynguyen.astarya.commands.games.CoinFlip}
     */
    COINFLIP("Help: CoinFlip",
        "Flips a coin any number of times. Parameter dictates how many times (1-10) to flip the coin.",
        "coinflip, flip",
        "[0]Once [1]NumberOfFlips",
        "flip | flip 5"),

    /**
     * {@link me.dannynguyen.astarya.commands.utility.ColorRole}
     */
    COLOR("Help: Color",
        "Assigns or removes color roles from the user. Using \"clean\" as the server owner will remove all empty color roles.",
        "color",
        "[1]#HexColor/clear/clean",
        "color #7EC2FE | color clear"),

    /**
     * {@link Credits}
     */
    CREDITS("Help: Credits",
        "Shows a list of credits for Astarya.",
        "credits",
        "[0]Credits",
        "credits"),

    /**
     * {@link me.dannynguyen.astarya.commands.owner.Delete}
     */
    DELETE("Help: Delete",
        "Deletes a number of recent messages. Parameter provides amount to delete (2-100).",
        "delete, purge",
        "[1]NumberOfMessages",
        "delete 15"),

    /**
     * {@link me.dannynguyen.astarya.commands.utility.Emote}
     */
    EMOTE("Help: Emote",
        "Provides the mentioned custom emote as a file. Parameter is the requested emote.",
        "emote, emoji",
        "[1]Emote",
        "emote :watameSnacks:"),

    /**
     * {@link Help}
     */
    HELP("Help: Help",
        "Provides documentation on Astarya's commands. Parameter describes more detailed command usage.",
        "help",
        "[0]MainMenu [1]CommandName",
        "help help"),

    /**
     * {@link me.dannynguyen.astarya.commands.games.HighOrLow}
     */
    HIGHORLOW("Help: HighOrLow",
        "Guess if the next number will be higher or lower!",
        "highorlow, guess",
        "[0]HighOrLow",
        "highorlow"),

    /**
     * {@link Info}
     */
    INFO("Help: Info",
        "Details information about Astarya and its developer.",
        "info, about",
        "[0]Info",
        "info"),

    /**
     * {@link me.dannynguyen.astarya.commands.audio.Join}
     */
    JOIN("Help: Join",
        "Joins the same voice channel as the user.",
        "join, j",
        "[0]Join",
        "join"),

    /**
     * {@link me.dannynguyen.astarya.commands.utility.Jpg}
     */
    JPG("Help: JPG",
        "Converts images to .jpg format with an optional image quality.",
        "jpg",
        "[0]+image(s) [1]<quality> +images(s)",
        "jpg, jpg 0.75"),

    /**
     * {@link me.dannynguyen.astarya.commands.audio.Leave}
     */
    LEAVE("Help: Leave",
        "Leaves the voice channel it's in.",
        "leave, l, disconnect, dc",
        "[0]Leave",
        "leave"),

    /**
     * {@link me.dannynguyen.astarya.commands.audio.Loop}
     */
    LOOP("Help: Loop",
        "Loops the current track.",
        "loop, repeat",
        "[0]Loop",
        "loop"),

    /**
     * {@link me.dannynguyen.astarya.commands.audio.Lyrics}
     */
    LYRICS("Help: Lyrics",
        "Finds lyrics of a song using Genius.",
        "lyrics",
        "[1 ++]SongName",
        "lyrics duck song"),

    /**
     * {@link me.dannynguyen.astarya.commands.audio.NowPlaying}
     */
    NOWPLAYING("Help: NowPlaying",
        "Shows what track is currently playing.",
        "nowplaying, np",
        "[0]NowPlaying",
        "nowplaying"),

    /**
     * {@link me.dannynguyen.astarya.commands.games.PandorasBox}
     */
    PANDORASBOX("Help: PandorasBox",
        "Sends a random scenario prompt. Prompts' subjects are substituted if parameters are provided.",
        "pandorasbox, pb",
        "[0]Self [1]VC/DC/Name [2 ++]Phrase",
        "pandorasbox, pandorasbox vc, pandorasbox dc, pandorasbox John Constantine"),

    /**
     * {@link me.dannynguyen.astarya.commands.audio.Pause}
     */
    PAUSE("Help: Pause",
        "Pauses the audio player. Astarya's activity changes may be rate limited if done rapidly.",
        "pause, stop",
        "[0]Pause",
        "pause"),

    /**
     * {@link Ping}
     */
    PING("Help: Ping",
        "Responds with the response time of Astarya in milliseconds.",
        "ping, ms",
        "[0]Ping",
        "ping"),

    /**
     * {@link me.dannynguyen.astarya.commands.audio.Play}
     */
    PLAY("Help: Play",
        """
            Adds a track to the track queue. Spotify playlists are limited to the\040
            most recent 100 songs added. Spotify albums are limited to 50 songs at a time.
            **Sources**
            > - YouTube: links/playlists
            > - Discord: media links
            > - Spotify: songs/playlists/albums
            **Supported File Types**
            > MP3, FLAC, WAV, Matroska/WebM, MP4/M4A, OGG streams, AAC streams""",
        "play, p",
        "[1]URL [2 ++]YouTubeQuery",
        "play https://www.youtube.com/watch?v=dQw4w9WgXcQ | play Cleverly Disguised Rickrolls"),

    /**
     * {@link me.dannynguyen.astarya.commands.audio.PlayNext}
     */
    PLAYNEXT("Help: PlayNext",
        "Sets the next track to be played in the track queue.",
        "playnext, after",
        "[1]QueueNumber",
        "playnext 3"),

    /**
     * {@link me.dannynguyen.astarya.commands.utility.Poll}
     */
    POLL("Help: Poll",
        "Creates a reaction vote with up to 10 options. The options are parameters separated by commas.",
        "poll, vote",
        "[2, ++]Options",
        "poll hot pizza, cold pizza"),

    /**
     * {@link me.dannynguyen.astarya.commands.utility.Profile}
     */
    PROFILE("Help: Profile",
        "Sends information about a user.",
        "profile, whois, user",
        "[0]Self [1]Mention/UserId/<@UserId> [1+]Name/Nickname",
        "profile | profile @Bam | " + "profile 204448598539239424 | " +
            "profile <@204448598539239424> | profile Bam | profile Bam's Nickname"),

    /**
     * {@link me.dannynguyen.astarya.commands.audio.Queue}
     */
    QUEUE("Help: Queue",
        "Provides a list of tracks queued.",
        "queue, q",
        "[0]Queue [1]PageNumber",
        "queue | queue 1"),

    /**
     * {@link me.dannynguyen.astarya.commands.utility.Remind}
     */
    REMIND("Help: Remind",
        """
            Sets a timer and alerts the user when the time expires for up to a day's maximum length.\040
            Parameters provide the time duration, type, and event name. Astarya recognizes the following time types:
            > - 1 parameter: (0-7)d, (0-168)h, (0-10080)m, (0-604800)s
            > - 2+ parameters: days, day, d, hours, hour, hrs, hr, h,
            minutes, minute, mins, min, m, seconds, second, secs, sec, s.""",
        "remind, timer",
        "[1]TimeDuration&TimeType/Time [2]TimeDuration/TimeType/EventName [3++]EventName",
        "remind (0-7)d | remind (0-168)h | remind (0-10080)m | " +
            "remind (0-604800)s | remind TimeDurationTimeType EventName | remind TimeDuration TimeType EventName"),

    /**
     * {@link me.dannynguyen.astarya.commands.audio.Remove}
     */
    REMOVE("Help: Remove",
        "Removes track(s) from the track queue.",
        "remove, r",
        "[1]QueueNumber [1 ++]QueueNumbers",
        "remove 1 | remove 2 4 5"),

    /**
     * {@link me.dannynguyen.astarya.commands.audio.Return}
     */
    RETURN("Help: Return",
        "Returns a recently skipped track to the track queue.",
        "return, ret",
        "[0]RecentlySkipped [1]SkippedNumber",
        "return | return 1"),

    /**
     * {@link me.dannynguyen.astarya.commands.games.Roll}
     */
    ROLL("Help: Roll",
        "Dice roll and random integer generator. No parameters to roll once. One parameter to roll 1-10 times. " +
            "Three parameters to set how many times to roll a custom range of minimum and maximum values.",
        "roll, rng, dice",
        "[0]Once [1]NumberOfRolls [2]Minimum [3]Maximum",
        "roll | roll 10 | roll 2 25 50"),

    /**
     * {@link me.dannynguyen.astarya.commands.audio.SearchTrack}
     */
    SEARCHTRACK("Help: SearchTrack",
        "Searches for a track to add to the track queue.",
        "searchtrack, search, st",
        "[1 ++]YouTubeQuery -> [1]SearchResultNumber",
        "search towa pallete"),

    /**
     * {@link me.dannynguyen.astarya.commands.utility.Server}
     */
    SERVER("Help: Server",
        "Provides information on the Discord server.",
        "server",
        "[0]Server",
        "server"),

    /**
     * {@link me.dannynguyen.astarya.commands.audio.SetPosition}
     */
    SETPOSITION("Help: SetPosition",
        "Sets the position of the currently playing track. \":\" separates the time types from hours:minutes:seconds.",
        "setposition, setpos",
        "[1]TimeString",
        "setposition 150 | setposition 2:30"),

    /**
     * {@link Settings}
     */
    SETTINGS("Help: Settings",
        "Provides information on Astarya settings.",
        "settings, config",
        "[0]MainMenu [1]Setting [2]True/False",
        "settings | settings deleteinvoke | settings deleteinvoke true"),

    /**
     * {@link me.dannynguyen.astarya.commands.audio.Shuffle}
     */
    SHUFFLE("Help: Shuffle",
        "Shuffles the track queue.",
        "shuffle, mix",
        "[0]Shuffle",
        "shuffle"),

    /**
     * {@link me.dannynguyen.astarya.commands.owner.Shutdown}
     */
    SHUTDOWN("Help: Shutdown",
        "Shuts Astarya down.",
        "shutdown",
        "[0]Shutdown",
        "shutdown"),

    /**
     * {@link me.dannynguyen.astarya.commands.audio.Skip}
     */
    SKIP("Help: Skip",
        "Skips the currently playing track. Astarya's activity changes may be rate limited if done rapidly.",
        "skip, s, next",
        "[0]Skip",
        "skip"),

    /**
     * {@link me.dannynguyen.astarya.commands.audio.Swap}
     */
    SWAP("Help: Swap",
        "Swaps the position of a track in queue with another.",
        "swap, switch",
        "[1]QueueNumber [2]QueueNumber",
        "swap 2 4");

    /**
     * Title.
     */
    private final String title;

    /**
     * Description.
     */
    private final String description;

    /**
     * Aliases.
     */
    private final String aliases;

    /**
     * Parameters.
     */
    private final String parameters;

    /**
     * Examples.
     */
    private final String examples;

    /**
     * Associates a command with its title, description, aliases, parameters, and examples.
     *
     * @param title       title
     * @param description description
     * @param aliases     aliases
     * @param parameters  parameters
     * @param examples    examples
     */
    Command(String title, String description, String aliases, String parameters, String examples) {
      this.title = title;
      this.description = description;
      this.aliases = aliases;
      this.parameters = parameters;
      this.examples = examples;
    }

    /**
     * Gets the command's title.
     *
     * @return command's title
     */
    private String getTitle() {
      return this.title;
    }

    /**
     * Gets the command's description.
     *
     * @return command's description
     */
    private String getDescription() {
      return this.description;
    }

    /**
     * Gets the command's aliases.
     *
     * @return command's aliases
     */
    private String getAliases() {
      return this.aliases;
    }

    /**
     * Gets the command's parameters.
     *
     * @return command's parameters
     */
    private String getParameters() {
      return this.parameters;
    }

    /**
     * Gets the command's examples
     *
     * @return command's examples
     */
    private String getExamples() {
      return this.examples;
    }
  }
}