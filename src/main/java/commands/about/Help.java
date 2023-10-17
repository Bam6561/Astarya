package commands.about;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import commands.owner.Settings;
import net.dv8tion.jda.api.EmbedBuilder;

/**
 * Help is a command invocation that provides documentation on Astarya's commands.
 *
 * @author Danny Nguyen
 * @version 1.6.5
 * @since 1.0
 */
public class Help extends Command {
  public Help() {
    this.name = "help";
    this.aliases = new String[]{"help"};
    this.arguments = "[0]MainMenu [1]CommandName";
    this.help = "Provides documentation on Astarya's commands.";
  }

  /**
   * Either sends an embed containing all bot commands available to the user or
   * detailed documentation of a command if an additional argument is provided.
   * Otherwise, an invalid number of arguments reference a link to the online wiki.
   *
   * @param ce object containing information about the command event
   */
  @Override
  protected void execute(CommandEvent ce) {
    Settings.deleteInvoke(ce);

    // Parse message for arguments
    String[] arguments = ce.getMessage().getContentRaw().split("\\s");
    int numberOfArguments = arguments.length - 1;

    EmbedBuilder display = new EmbedBuilder();
    switch (numberOfArguments) {
      case 0 -> sendHelpMainMenu(display);
      case 1 -> buildDetailedCommandHelpEmbed(display, arguments[1].toLowerCase());
      default -> display.setDescription("Type `" + Settings.getPrefix() +
          "commands` to get a list of commands available. You can also refer to " +
          "[Astarya's Wiki](https://github.com/Bam6561/Astarya/wiki).");
    }
    Settings.sendEmbed(ce, display);
  }

  /**
   * Sends an embed containing all bot commands available to the user.
   *
   * @param display object representing the embed
   */
  private void sendHelpMainMenu(EmbedBuilder display) {
    display.setAuthor("Help");
    display.setDescription("Type `" + Settings.getPrefix() + "help <CommandName>` " +
        "for more details on each command. Alternatively, see " +
        "[Astarya's Wiki](https://github.com/Bam6561/Astarya/wiki).");
    display.addField("About", "> credits | help | info | ping ", true);
    display.addField("Audio", "> clearQueue | join | leave | loop | " +
        "nowPlaying | pause | playNext | play | queue | remove | return | " +
        "searchTrack | setPosition | shuffle | skip | swap", true);
    display.addField("Games", "> choose | coinflip | highorlow | roll", true);
    display.addField("Owner", "> delete | settings | shutdown", true);
    display.addField("Utility", "> emote | poll | profile | remind | server", true);
  }

  /**
   * Fills out parameters for an embed containing detailed documentation of a command.
   *
   * @param display     object representing the embed
   * @param commandName name of the command to be referenced for documentation
   */
  private void buildDetailedCommandHelpEmbed(EmbedBuilder display, String commandName) {
    switch (commandName) {
      case "choose", "pick" -> sendDetailedCommandHelpEmbed(display, "__Help: Choose__",
          "Chooses randomly between any number of options. " +
              "The options are arguments separated by commas.",
          "choose, pick", "[1, ++]Options",
          "choose Take out the trash, Do the laundry, Walk the dog");
      case "clearqueue", "clear" -> sendDetailedCommandHelpEmbed(display, "__Help: ClearQueue__",
          "Clears the track queue.", "clearqueue, clear", "[0]ClearQueue",
          "clearqueue");
      case "coinflip", "flip" -> sendDetailedCommandHelpEmbed(display, "__Help: Coin Flip__",
          "Flips a coin any number of times. Argument dictates how many times (1-10) to flip the coin.",
          "coinflip, flip", "[0]Once [1]NumberOfFlips", "flip | flip 5");
      case "credits" -> sendDetailedCommandHelpEmbed(display, "__Help: Credits__",
          "Shows a list of credits for Astarya.", "credits", "[0]Credits", "credits");
      case "delete", "purge" -> sendDetailedCommandHelpEmbed(display, "__Help: Delete__",
          "Deletes a number of recent messages. Argument provides amount to delete (2-100).",
          "delete, purge", "[1]NumberOfMessages", "delete 15");
      case "emote", "emoji" -> sendDetailedCommandHelpEmbed(display, "__Help: Emote__",
          "Provides the mentioned custom emote as a file. Argument is the requested emote.",
          "emote, emoji", "[1]Emote", "emote :watameSnacks:");
      case "help" -> sendDetailedCommandHelpEmbed(display, "__Help: Help__",
          "Provides documentation on Astarya's commands. " +
              "Argument describes more detailed command usage.",
          "help", "[0]MainMenu [1]CommandName", "help help");
      case "highorlow", "guess" -> sendDetailedCommandHelpEmbed(display, "__Help: HighOrLow__",
          "Guess whether the next number will be higher or lower!",
          "highorlow, guess", "[0]HighOrLow", "highorlow");
      case "info", "about" -> sendDetailedCommandHelpEmbed(display, "__Help: Info__",
          "Details information about Astarya and its developer.",
          "info, about", "[0]Info", "info");
      case "join", "j" -> sendDetailedCommandHelpEmbed(display, "__Help: Join__",
          "Joins the same voice channel as the user.", "join, j",
          "[0]Join", "join");
      case "leave", "l", "disconnect", "dc" -> sendDetailedCommandHelpEmbed(display, "__Help: Leave__",
          "Leaves the voice channel it's in.", "leave, l, disconnect, dc",
          "[0]Leave", "leave");
      case "loop", "repeat" -> sendDetailedCommandHelpEmbed(display, "__Help: Loops__",
          "Loops the current track.",
          "loop, repeat", "[0]Loop", "loop");
      case "nowplaying", "np" -> sendDetailedCommandHelpEmbed(display, "__Help: NowPlaying__",
          "Shows what track is currently playing.", "nowplaying, np",
          "[0]NowPlaying", "nowplaying");
      case "pause", "stop" -> sendDetailedCommandHelpEmbed(display, "__Help: Pause__",
          "Pauses the audio player. Astarya's activity changes may be rate limited if done rapidly.",
          "pause, stop", "[0]Pause", "pause");
      case "ping", "ms" -> sendDetailedCommandHelpEmbed(display, "__Help: Ping__",
          "Responds with the response time of Astarya in milliseconds.",
          "ping, ms", "[0]Ping", "ping");
      case "play", "p" -> sendDetailedCommandHelpEmbed(display, "__Help: Play__",
          "Adds a track to the queue. Spotify playlists are limited to the most recent 100 songs added. " +
              "Spotify albums are limited to 50 songs at a time. \n" +
              "**Sources** \n > - YouTube: links/playlists \n> - Discord: media links \n> - Spotify: songs/playlists/albums \n" +
              "**Supported File Types** \n > MP3, FLAC, WAV, Matroska/WebM, MP4/M4A, OGG streams, AAC streams",
          "play, p", "[1]URL [2++]YouTubeQuery",
          "play https://www.youtube.com/watch?v=dQw4w9WgXcQ | play Cleverly Disguised Rickrolls");
      case "playnext", "after" -> sendDetailedCommandHelpEmbed(display, "__Help: PlayNext__",
          "Sets the next track to be played in the queue.",
          "playnext, after", "[1]QueueNumber", "playnext 3");
      case "poll", "vote" -> sendDetailedCommandHelpEmbed(display, "__Help: Poll__",
          "Creates a reaction vote with up to 10 options. The options are arguments separated by commas.",
          "poll, vote", "[2, ++]PollOptions", "poll hot pizza, cold pizza");
      case "profile", "whois", "user" -> sendDetailedCommandHelpEmbed(display, "__Help: Profile__",
          "Returns information about a user.", "profile, whois, user",
          "[0]Self [1]Mention/UserID/<@UserId> [1+]Name/Nickname", "profile | profile @Bam | " +
              "profile 204448598539239424 | profile <@204448598539239424> | profile Bam | profile Bam's Nickname");
      case "queue", "q" -> sendDetailedCommandHelpEmbed(display, "__Help: Queue__",
          "Provides a list of tracks queued.", "queue, q",
          "[0]Queue [1]PageNumber", "queue | queue 1");
      case "remind", "timer" -> sendDetailedCommandHelpEmbed(display, "__Help: Remind__",
          "Sets a timer and alerts the user when the time expires for up to " +
              "a day's maximum length. Arguments provide the time duration, type, " +
              "and event name. Astarya recognizes the following time types: \n" +
              "> - 1 argument: (0-7)d, (0-168)h, (0-10080)m, (0-604800)s \n" +
              "> - 2+ arguments: days, day, d, hours, hour, hrs, hr, h, " +
              "minutes, minute, mins, min, m, seconds, second, secs, sec, s.",
          "remind, timer",
          "[1]TimeDuration&TimeType/Time [2]TimeDuration/TimeType/EventName [3++]EventName",
          "remind (0-7)d | remind (0-168)h | remind (0-10080)m | remind (0-604800)s | "
              + "remind TimeDurationTimeType EventName | remind TimeDuration TimeType EventName");
      case "remove", "r" -> sendDetailedCommandHelpEmbed(display, "__Help: Remove__",
          "Removes track(s) from the queue.", "remove, r",
          "[1]QueueNumber [1, ++]QueueNumbers", "remove 1 | remove 2 4 5");
      case "return", "ret" -> sendDetailedCommandHelpEmbed(display, "__Help: Return__",
          "Returns a recently skipped track to the queue.", "return, ret",
          "[0]RecentlySkipped [1]SkippedStackNumber", "return | return 1");
      case "roll", "rng", "dice" -> sendDetailedCommandHelpEmbed(display, "__Help: Roll__",
          "Dice roll and random integer generator. No arguments to roll once. "
              + "One argument to roll 1-10 times. Three arguments to set how many times to roll" +
              " a custom range of minimum and maximum values.",
          "roll, rng, dice", "[0]Once [1]NumberOfRolls [2]Minimum [3]Maximum",
          "roll | roll 10 | roll 2 25 50");
      case "searchtrack", "search", "st" -> sendDetailedCommandHelpEmbed(display, "__Help: SearchTrack__",
          "Searches for a track to add to the queue.", "searchtrack, search, st",
          "[1++]YouTubeQuery -> [1]SearchResultNumber", "search towa pallete");
      case "server" -> sendDetailedCommandHelpEmbed(display, "__Help: Server__",
          "Provides information on the Discord server.",
          "server", "[0]Server", "server");
      case "setposition", "setpos" -> sendDetailedCommandHelpEmbed(display, "__Help: SetPosition__",
          "Sets the position of the currently playing track. " +
              "\":\" separates the time types from hours:minutes:seconds.",
          "setposition, setpos", "[1]TimeString",
          "setposition 150 | setposition 2:30");
      case "settings", "config" -> sendDetailedCommandHelpEmbed(display, "__Help: Settings__",
          "Provides information on Astarya settings.", "settings, config",
          "[0]MainMenu [1]Setting [2]True/False", "settings | settings deleteinvoke | settings deleteinvoke true");
      case "shuffle", "mix" -> sendDetailedCommandHelpEmbed(display, "__Help: Shuffle__",
          "Shuffles the queue.", "shuffle, mix", "[0]Shuffle", "shuffle");
      case "shutdown" -> sendDetailedCommandHelpEmbed(display, "__Help: Shutdown__",
          "Shuts Astarya down.", "shutdown", "[0]Shutdown", "shutdown");
      case "skip", "s", "next" -> sendDetailedCommandHelpEmbed(display, "__Help: Skip__",
          "Skips the currently playing track. Astarya's activity changes may be rate limited if done rapidly.",
          "skip, s, next", "[0]Skip", "skip");
      case "swap", "switch" -> sendDetailedCommandHelpEmbed(display, "__Help: Swap__",
          "Swaps the position of a track in queue with another.", "swap, switch",
          "[1]QueueNumber [2]QueueNumber", "swap 2 4");
      default -> {
        display.setAuthor("Help: Command Not Found");
        display.setDescription("Type `" + Settings.getPrefix() + "commands` to get a list of commands available. " +
            "You can also refer to [Astarya's Wiki](https://github.com/Bam6561/Astarya/wiki).");
      }
    }
  }

  /**
   * Sends an embed containing detailed documentation of a command.
   *
   * @param display     object representing the embed
   * @param title       name of the command
   * @param description description of the command
   * @param aliases     aliases of the command
   * @param arguments   arguments the command will accept for different variations of usage
   * @param examples    examples of how to use the command
   */
  private void sendDetailedCommandHelpEmbed(EmbedBuilder display, String title,
                                            String description, String aliases, String arguments, String examples) {
    display.setAuthor(title);
    display.setDescription(description);
    display.addField("Aliases", aliases, false);
    display.addField("Arguments", arguments, false);
    display.addField("Examples", examples, false);
  }
}