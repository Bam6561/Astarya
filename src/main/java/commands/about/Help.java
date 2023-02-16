package commands.about;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import commands.owner.Settings;
import net.dv8tion.jda.api.EmbedBuilder;

public class Help extends Command {
  public Help() {
    this.name = "help";
    this.aliases = new String[]{"help", "manual", "instructions"};
    this.arguments = "[0]Help [1]CommandName";
    this.help = "Provides the command manual for the bot.";
  }

  // Sends an embed containing documentation on a command
  @Override
  protected void execute(CommandEvent ce) {
    Settings.deleteInvoke(ce);

    // Parse message for arguments
    String[] arguments = ce.getMessage().getContentRaw().split("\\s");
    int numberOfArguments = arguments.length - 1;

    EmbedBuilder display = new EmbedBuilder();
    switch (numberOfArguments) {
      case 0 -> helpMainMenu(ce, display);
      case 1 -> sendDetailedCommandHelpEmbed(ce, display, arguments[1].toLowerCase());
      default -> ce.getChannel().sendMessage("Try doing " + Settings.getPrefix()
          + "commands for a full command list.").queue();
    }
  }

  private void helpMainMenu(CommandEvent ce, EmbedBuilder display) {
    display.setTitle("__Help__");
    display.setDescription("Type help [CommandName] for more details on the usage of each command.");
    display.addField("**About:**", "<credits <help <info <ping ", true);
    display.addField("**Utility:**", "<avatar <emote <poll <profile <remind <server", true);
    display.addField("**Music:**", "<clearQueue <join <leave <loop <nowPlaying " +
        "<pause <playNext <play <queue <remove <searchTrack <setPosition <shuffle <skip <swap", true);
    display.addField("**Games:**", "<choose <coinflip <highorlow <roll", true);
    display.addField("**Owner:**", "<delete <settings <shutdown", true);

    Settings.sendEmbed(ce, display);
  }

  private void sendDetailedCommandHelpEmbed(CommandEvent ce, EmbedBuilder display, String commandName) {
    switch (commandName) {
      case "avatar", "pfp" -> sendDetailedCommandHelpEmbed(ce, display, "__Command: Avatar__",
          "Provides the user's profile picture. By default, the size of the image is 1024x1024. "
              + "Additional arguments adjust the size of the image in choices of 128, 256, & 512.",
          "avatar, pfp", "[0]Self [1]Mention/UserID/Size [2]Size",
          "avatar, avatar (128, 256, 512), avatar @user, avatar UserID, " +
              "avatar @user (128, 256, 512), avatar UserID (128, 256, 512)");
      case "choose", "choice", "pick", "options" -> sendDetailedCommandHelpEmbed(ce, display, "__Command: Choose__",
          "Chooses randomly between any number of options. " +
              "The choices are arguments provided by commas (,).",
          "choose, choice, pick, option", "[1, ++]Options",
          "choice Take out the trash, Do the laundry, Walk the dog");
      case "clearqueue", "clearq", "clear" -> sendDetailedCommandHelpEmbed(ce, display, "__Command: ClearQueue__",
          "Clears the track queue.", "clearqueue, clear", "[0]Clear",
          "clearqueue");
      case "coinflip" -> sendDetailedCommandHelpEmbed(ce, display, "__Command: Coin Flip__",
          "Flips a coin any number of times. " +
              "Argument provides how many times to flip the coin. (1-10)",
          "coinflip, flip", "[0]Once [1]NumberOfFlips",
          "flip, flip [1-10]");
      case "credits" -> sendDetailedCommandHelpEmbed(ce, display, "__Command: Credits__",
          "Shows a list of credits for the bot.",
          "credits", "[0]Credits", "credits");
      case "delete", "purge" -> sendDetailedCommandHelpEmbed(ce, display, "__Command: Delete__",
          "Deletes a number of recent messages. " +
              "Argument provides how many (2 - 100).",
          "delete, purge, wipe", "[1]NumberOfMessagesToDelete", "delete (2-100)");
      case "emote", "emoji" -> sendDetailedCommandHelpEmbed(ce, display, "__Command: Emote__",
          "Provides the mentioned custom emote as a file. " +
              "Argument is the requested emote.",
          "emote, emoji", "[1]Emote", "emote :happyFeetDance:");
      case "help", "manual", "instructions" -> sendDetailedCommandHelpEmbed(ce, display, "__Command: Help__",
          "Provides documentation on LucyferBot commands. " +
              "Argument describes more detailed command usage.",
          "help, manual, instructions",
          "[0]HelpMainMenu [1]CommandName", "help help");
      case "highorlow", "guess" -> sendDetailedCommandHelpEmbed(ce, display, "Command: __HighOrLow__",
          "Guess whether the next number will be higher or lower!",
          "highorlow, guess", "[0]HighOrLow", "highorlow");
      case "info" -> sendDetailedCommandHelpEmbed(ce, display, "__Command: Info__",
          "Provides information on the bot and its developer.",
          "info", "[0]Info", "info");
      case "join", "j" -> sendDetailedCommandHelpEmbed(ce, display, "__Command: Join__",
          "Bot joins the same voice channel as the user.", "join, j",
          "[0]join", "info");
      case "leave", "l", "disconnect", "dc" -> sendDetailedCommandHelpEmbed(ce, display, "__Command: Leave__",
          "Bot leaves the voice channel it is in.", "leave, l, disconnect, dc",
          "[0]leave", "leave");
      case "loop", "repeat" -> sendDetailedCommandHelpEmbed(ce, display, "__Command: Loops__",
          "Loops the current audio track.",
          "loop, repeat", "[0]Loop", "loop");
      case "nowplaying", "np" -> sendDetailedCommandHelpEmbed(ce, display, "__Command: NowPlaying__",
          "Shows what's currently playing.", "nowplaying, np",
          "[0]NowPlaying", "nowplaying");
      case "pause", "stop" -> sendDetailedCommandHelpEmbed(ce, display, "__Command: Pause__",
          "Pauses the audio player. Rapid activity status changes may be rate limited.",
          "pause, stop", "[0]Pause", "pause");
      case "ping", "ms" -> sendDetailedCommandHelpEmbed(ce, display, "__Command: Ping__",
          "Response time of the bot in milliseconds.",
          "ping, ms", "[0]Ping", "ping");
      case "play", "p", "add" -> sendDetailedCommandHelpEmbed(ce, display, "__Command: Play__",
          """
              Adds an audio track to the queue. Limit of Spotify playlists is 100. Limit of Spotify albums is 50.
              Rapid activity status changes may be rate limited.\s
              **Sources:** YouTube links/playlists, Discord media links, Spotify links/playlists/album\s
              **File Types:** MP3, FLAC, WAV, Matroska/WebM, MP4/M4A, OGG streams, AAC streams\s""",
          "play, p, add", "[1]URL, [2++]YouTubeQuery",
          "play https://www.youtube.com/watch?v=dQw4w9WgXcQ, play Cleverly Disguised Rickrolls");
      case "playnext", "after" -> sendDetailedCommandHelpEmbed(ce, display, "__Command: PlayNext__",
          "Sets the position of the currently playing audio track.",
          "playnext, after", "[1]QueueNumber", "playnext 3");
      case "poll", "react", "vote" -> sendDetailedCommandHelpEmbed(ce, display, "__Command: Poll__",
          "Creates a reaction vote with up to 10 options. " +
              "The options are arguments provided by commas (,).",
          "poll, react, vote", "[2, ++]PollOptions",
          "poll hot pizza, cold pizza");
      case "profile", "whois", "who", "user" -> sendDetailedCommandHelpEmbed(ce, display, "__Command: Profile__",
          "Provides information on the user.",
          "profile, whois, who, user", "[0]Self [1]Mention/UserID",
          "profile, profile @user, profile UserID");
      case "queue", "q" -> sendDetailedCommandHelpEmbed(ce, display, "__Command: Queue__",
          "Provides a list of audio tracks queued.", "queue, q",
          "[0]Queue, [1]PageNumber", "queue, queue 1");
      case "remind", "alert", "timer" -> sendDetailedCommandHelpEmbed(ce, display, "__Command: Remind__",
          "Sets a timer and alerts the user when the time expires for up to " +
              "a day's maximum length. Arguments provide the time duration, type, " +
              "and event name. The bot recognizes the following data types for 1 argument: " +
              "(0-86400)s, (0-1440)m, (0-24)h - and for 2+ arguments: hours, hour, hrs, hr, " +
              "h, minutes, minute, mins, min, m, seconds, second, secs, sec, s.",
          "remind, alert, timer",
          "[1]TimeDuration&TimeType/Time [2]TimeDuration/TimeType/EventName [3++]EventName",
          "remind (0-86400)s, remind (0-1440)m, remind (0-24)h, "
              + "remind TimeDurationTimeType EventName, remind TimeDuration TimeType EventName");
      case "remove", "rm", "r" -> sendDetailedCommandHelpEmbed(ce, display, "__Command: Remove__",
          "Removes an audio track from the queue.", "remove, rm, r",
          "[1]QueueNumber", "remove 1");
      case "roll", "rng", "dice", "random" -> sendDetailedCommandHelpEmbed(ce, display, "__Command: Roll__",
          "Dice roll and random integer generator. No arguments to roll once. "
              + "One argument to roll (1-10) times. Three arguments to set how many times to roll" +
              " your own custom range of minimum and maximum values.",
          "roll, rng, dice, random", "[0]Once [1]NumberOfRolls [2]Minimum [3]Maximum",
          "roll, roll (1-10), roll (1-10) (0++) (1-214748367)");
      case "searchtrack", "search", "find" -> sendDetailedCommandHelpEmbed(ce, display, "__Command: SearchTrack__",
          "Searches for an audio track to add to the queue. Rapid activity status changes may be rate limited.", "searchtrack, search, find",
          "[1++]YouTubeQuery -> [1]SearchResultNumber", "search towa pallete, 1");
      case "server" -> sendDetailedCommandHelpEmbed(ce, display, "__Command: Server__",
          "Provides information on the server.",
          "serverinfo, server", "[0]ServerInfo", "serverinfo");
      case "setposition", "setpos", "goto" -> sendDetailedCommandHelpEmbed(ce, display, "__Command: SetPosition__",
          "Sets the position of the currently playing audio track. \":\" seperates" +
              "the time types, from hours:minutes:seconds.",
          "setposition, setpos, goto", "[1]TimeString",
          "setposition 124, set position 2:04");
      case "settings", "config" -> sendDetailedCommandHelpEmbed(ce, display, "__Command: Settings__",
          "Provides information on bot settings.",
          "settings, config",
          "[0]Settings [1]Setting [2]True/False",
          "settings, settings (setting) (true/false) ");
      case "shuffle", "mix" -> sendDetailedCommandHelpEmbed(ce, display, "__Command: Shuffle__",
          "Shuffles the queue.", "shuffle, mix", "[0]Shuffle",
          "shuffle");
      case "shutdown" -> sendDetailedCommandHelpEmbed(ce, display, "__Command: Shutdown__",
          "Shuts the bot down.",
          "shutdown, nuke", "[0]Shutdown", "shutdown");
      case "skip", "s", "next" -> sendDetailedCommandHelpEmbed(ce, display, "__Command: Skip__",
          "Skips the currently playing audio track. Rapid activity status changes may be rate limited.",
          "skip, s, next", "[0]Skip", "skip");
      case "swap", "switch" -> sendDetailedCommandHelpEmbed(ce, display, "__Command: Swap__",
          "Swaps the position of an audio track in queue with another.",
          "swap, switch", "[1]QueueNumber [2]QueueNumber",
          "swap 2 4");
      default -> {
        display.setTitle("__Command Not Found__");
        display.setDescription("Try typing " + Settings.getPrefix() + "commands for a full command list.");

        Settings.sendEmbed(ce, display);
      }
    }
  }

  // Detailed command help details display
  private void sendDetailedCommandHelpEmbed(CommandEvent ce, EmbedBuilder display, String title,
                                            String description, String aliases, String arguments, String examples) {
    display.setTitle(title);
    display.setDescription(description);
    display.addField("**Aliases:**", aliases, false);
    display.addField("**Arguments:**", arguments, false);
    display.addField("**Examples:**", examples, false);

    Settings.sendEmbed(ce, display);
  }
}