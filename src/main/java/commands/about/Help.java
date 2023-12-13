package commands.about;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import commands.owner.Settings;
import net.dv8tion.jda.api.EmbedBuilder;

/**
 * Help is a command invocation that provides documentation on Astarya's commands.
 *
 * @author Danny Nguyen
 * @version 1.6.11
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
   * detailed documentation of a command if an additional parameter is provided.
   * Otherwise, an invalid number of parameters reference a link to the online wiki.
   *
   * @param ce object containing information about the command event
   */
  @Override
  protected void execute(CommandEvent ce) {
    Settings.deleteInvoke(ce);

    String[] parameters = ce.getMessage().getContentRaw().split("\\s");
    int numberOfParameters = parameters.length - 1;

    EmbedBuilder display = new EmbedBuilder();
    switch (numberOfParameters) {
      case 0 -> sendHelpMainMenu(display);
      case 1 -> buildDetailedCommandHelpEmbed(display, parameters[1].toLowerCase());
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
    display.addField("Games", "> choose | coinflip | highorlow | pandorasbox | roll", true);
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
      case "choose", "pick" -> sendDetailedCommandHelpEmbed(display, "Help: Choose",
          "Chooses randomly between any number of options. " +
              "The options are parameters separated by commas.",
          "choose, pick", "[1, ++]Options",
          "choose Take out the trash, Do the laundry, Walk the dog");
      case "clearqueue", "clear" -> sendDetailedCommandHelpEmbed(display, "Help: ClearQueue",
          "Clears the track queue.", "clearqueue, clear", "[0]ClearQueue",
          "clearqueue");
      case "coinflip", "flip" -> sendDetailedCommandHelpEmbed(display, "Help: Coin Flip",
          "Flips a coin any number of times. Parameter dictates how many times (1-10) to flip the coin.",
          "coinflip, flip", "[0]Once [1]NumberOfFlips", "flip | flip 5");
      case "credits" -> sendDetailedCommandHelpEmbed(display, "Help: Credits",
          "Shows a list of credits for Astarya.", "credits", "[0]Credits", "credits");
      case "delete", "purge" -> sendDetailedCommandHelpEmbed(display, "Help: Delete",
          "Deletes a number of recent messages. Parameter provides amount to delete (2-100).",
          "delete, purge", "[1]NumberOfMessages", "delete 15");
      case "emote", "emoji" -> sendDetailedCommandHelpEmbed(display, "Help: Emote",
          "Provides the mentioned custom emote as a file. Parameter is the requested emote.",
          "emote, emoji", "[1]Emote", "emote :watameSnacks:");
      case "help" -> sendDetailedCommandHelpEmbed(display, "Help: Help",
          "Provides documentation on Astarya's commands. " +
              "Parameter describes more detailed command usage.",
          "help", "[0]MainMenu [1]CommandName", "help help");
      case "highorlow", "guess" -> sendDetailedCommandHelpEmbed(display, "Help: HighOrLow",
          "Guess if the next number will be higher or lower!",
          "highorlow, guess", "[0]HighOrLow", "highorlow");
      case "info", "about" -> sendDetailedCommandHelpEmbed(display, "Help: Info",
          "Details information about Astarya and its developer.",
          "info, about", "[0]Info", "info");
      case "join", "j" -> sendDetailedCommandHelpEmbed(display, "Help: Join",
          "Joins the same voice channel as the user.", "join, j",
          "[0]Join", "join");
      case "leave", "l", "disconnect", "dc" -> sendDetailedCommandHelpEmbed(display, "Help: Leave",
          "Leaves the voice channel it's in.", "leave, l, disconnect, dc",
          "[0]Leave", "leave");
      case "loop", "repeat" -> sendDetailedCommandHelpEmbed(display, "Help: Loops",
          "Loops the current track.",
          "loop, repeat", "[0]Loop", "loop");
      case "lyrics" -> sendDetailedCommandHelpEmbed(display, "Help: Lyrics",
          "Finds lyrics of a song using Genius.", "lyrics", "[1 ++]*", "lyrics duck song");
      case "nowplaying", "np" -> sendDetailedCommandHelpEmbed(display, "Help: NowPlaying",
          "Shows what track is currently playing.", "nowplaying, np",
          "[0]NowPlaying", "nowplaying");
      case "pandorasbox", "pb" -> sendDetailedCommandHelpEmbed(display, "Help: PandorasBox",
          "Sends a random scenario prompt. Prompts' subjects are substituted if paramaters are provided.",
          "pandorasbox, pb", "[0]Self [1]VC/DC/* [2 ++]*",
          "pandorasbox, pandorasbox vc, pandorasbox dc, pandorasbox John Constantine");
      case "pause", "stop" -> sendDetailedCommandHelpEmbed(display, "Help: Pause",
          "Pauses the audio player. Astarya's activity changes may be rate limited if done rapidly.",
          "pause, stop", "[0]Pause", "pause");
      case "ping", "ms" -> sendDetailedCommandHelpEmbed(display, "Help: Ping",
          "Responds with the response time of Astarya in milliseconds.",
          "ping, ms", "[0]Ping", "ping");
      case "play", "p" -> sendDetailedCommandHelpEmbed(display, "Help: Play",
          "Adds a track to the track queue. Spotify playlists are limited to the most recent 100 songs added. " +
              "Spotify albums are limited to 50 songs at a time. \n" +
              "**Sources** \n > - YouTube: links/playlists \n> - Discord: media links \n> - Spotify: songs/playlists/albums \n" +
              "**Supported File Types** \n > MP3, FLAC, WAV, Matroska/WebM, MP4/M4A, OGG streams, AAC streams",
          "play, p", "[1]URL [2++]YouTubeQuery",
          "play https://www.youtube.com/watch?v=dQw4w9WgXcQ | play Cleverly Disguised Rickrolls");
      case "playnext", "after" -> sendDetailedCommandHelpEmbed(display, "Help: PlayNext",
          "Sets the next track to be played in the track queue.",
          "playnext, after", "[1]QueueNumber", "playnext 3");
      case "poll", "vote" -> sendDetailedCommandHelpEmbed(display, "Help: Poll",
          "Creates a reaction vote with up to 10 options. The options are parameters separated by commas.",
          "poll, vote", "[2, ++]PollOptions", "poll hot pizza, cold pizza");
      case "profile", "whois", "user" -> sendDetailedCommandHelpEmbed(display, "Help: Profile",
          "Sends information about a user.", "profile, whois, user",
          "[0]Self [1]Mention/UserId/<@UserId> [1+]Name/Nickname", "profile | profile @Bam | " +
              "profile 204448598539239424 | profile <@204448598539239424> | profile Bam | profile Bam's Nickname");
      case "queue", "q" -> sendDetailedCommandHelpEmbed(display, "Help: Queue",
          "Provides a list of tracks queued.", "queue, q",
          "[0]Queue [1]PageNumber", "queue | queue 1");
      case "remind", "timer" -> sendDetailedCommandHelpEmbed(display, "Help: Remind",
          "Sets a timer and alerts the user when the time expires for up to " +
              "a day's maximum length. Parameters provide the time duration, type, " +
              "and event name. Astarya recognizes the following time types: \n" +
              "> - 1 parameter: (0-7)d, (0-168)h, (0-10080)m, (0-604800)s \n" +
              "> - 2+ parameters: days, day, d, hours, hour, hrs, hr, h, " +
              "minutes, minute, mins, min, m, seconds, second, secs, sec, s.",
          "remind, timer",
          "[1]TimeDuration&TimeType/Time [2]TimeDuration/TimeType/EventName [3++]EventName",
          "remind (0-7)d | remind (0-168)h | remind (0-10080)m | remind (0-604800)s | "
              + "remind TimeDurationTimeType EventName | remind TimeDuration TimeType EventName");
      case "remove", "r" -> sendDetailedCommandHelpEmbed(display, "Help: Remove",
          "Removes track(s) from the track queue.", "remove, r",
          "[1]QueueNumber [1, ++]QueueNumbers", "remove 1 | remove 2 4 5");
      case "return", "ret" -> sendDetailedCommandHelpEmbed(display, "Help: Return",
          "Returns a recently skipped track to the track queue.", "return, ret",
          "[0]RecentlySkipped [1]SkippedStackNumber", "return | return 1");
      case "roll", "rng", "dice" -> sendDetailedCommandHelpEmbed(display, "Help: Roll",
          "Dice roll and random integer generator. No parameters to roll once. "
              + "One parameter to roll 1-10 times. Three parameters to set how many times to roll" +
              " a custom range of minimum and maximum values.",
          "roll, rng, dice", "[0]Once [1]NumberOfRolls [2]Minimum [3]Maximum",
          "roll | roll 10 | roll 2 25 50");
      case "searchtrack", "search", "st" -> sendDetailedCommandHelpEmbed(display, "Help: SearchTrack",
          "Searches for a track to add to the track queue.", "searchtrack, search, st",
          "[1++]YouTubeQuery -> [1]SearchResultNumber", "search towa pallete");
      case "server" -> sendDetailedCommandHelpEmbed(display, "Help: Server",
          "Provides information on the Discord server.",
          "server", "[0]Server", "server");
      case "setposition", "setpos" -> sendDetailedCommandHelpEmbed(display, "Help: SetPosition",
          "Sets the position of the currently playing track. " +
              "\":\" separates the time types from hours:minutes:seconds.",
          "setposition, setpos", "[1]TimeString",
          "setposition 150 | setposition 2:30");
      case "settings", "config" -> sendDetailedCommandHelpEmbed(display, "Help: Settings",
          "Provides information on Astarya settings.", "settings, config",
          "[0]MainMenu [1]Setting [2]True/False", "settings | settings deleteinvoke | settings deleteinvoke true");
      case "shuffle", "mix" -> sendDetailedCommandHelpEmbed(display, "Help: Shuffle",
          "Shuffles the track queue.", "shuffle, mix", "[0]Shuffle", "shuffle");
      case "shutdown" -> sendDetailedCommandHelpEmbed(display, "Help: Shutdown",
          "Shuts Astarya down.", "shutdown", "[0]Shutdown", "shutdown");
      case "skip", "s", "next" -> sendDetailedCommandHelpEmbed(display, "Help: Skip",
          "Skips the currently playing track. Astarya's activity changes may be rate limited if done rapidly.",
          "skip, s, next", "[0]Skip", "skip");
      case "swap", "switch" -> sendDetailedCommandHelpEmbed(display, "Help: Swap",
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
   * @param parameters  parameters the command will accept for different variations of usage
   * @param examples    examples of how to use the command
   */
  private void sendDetailedCommandHelpEmbed(EmbedBuilder display, String title,
                                            String description, String aliases, String parameters, String examples) {
    display.setAuthor(title);
    display.setDescription(description);
    display.addField("Aliases", aliases, false);
    display.addField("Parameters", parameters, false);
    display.addField("Examples", examples, false);
  }
}