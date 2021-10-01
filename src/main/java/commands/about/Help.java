package commands.about;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import commands.owner.Settings;
import net.dv8tion.jda.api.EmbedBuilder;

public class Help extends Command {
  public Help() {
    this.name = "help";
    this.aliases = new String[]{"help", "manual", "instructions"};
    this.arguments = "[0]HelpCommandDescription [1]CommandName";
    this.help = "Provides the command manual for the bot.";
  }

  @Override
  protected void execute(CommandEvent ce) {
    Settings.deleteInvoke(ce);
    EmbedBuilder display = new EmbedBuilder();
    String[] args = ce.getMessage().getContentRaw().split("\\s"); // Parse message for arguments
    int arguments = args.length;
    switch (arguments) {
      case 1 -> getHelpMenu(ce, display); // Main menu
      case 2 -> getHelpDetail(ce, display, args[1].toLowerCase()); // Command help details
      default -> ce.getChannel().sendMessage("Try doing " + Settings.getPrefix() // Invalid arguments
          + "commands for a full command list.").queue();
    }
  }

  private void getHelpMenu(CommandEvent ce, EmbedBuilder display) {
    display.setTitle("__Help__");
    display.setDescription("Type help [CommandName] for more details on the usage of each command.");
    display.addField("**About:**", "▫info \n▫help \n▫credits", true);
    display.addField("**Utility:**", "▫serverinfo \n▫whois \n▫avatar \n▫emote \n▫remind", true);
    display.addField("**Games:**", "▫roll \n▫flip \n▫choice \n▫highorlow", true);
    display.addField("**Miscellaneous:**", "▫bruh \n▫echo \n▫ping", true);
    display.addField("**Promotion:**", "▫dungeonarchives", true);
    display.addField("**Owner:**", "▫settings \n▫buildembed \n▫clear \n▫shutdown", true);
    Settings.sendEmbed(ce, display);
  }

  private void getHelpDetail(CommandEvent ce, EmbedBuilder display, String commandName) {
    switch (commandName) {
      case "avatar" -> sendEmbed(ce, display, "__Command: Avatar__",
          "Provides the user's profile picture. By default, the size of the image is 1024x1024. "
              + "Additional arguments adjust the size of the image in choices of 128, 256, & 512.",
          "avatar, pfp", "[0]Self [1]Mention/UserID/Size [2]Size",
          "avatar, avatar (128, 256, 512), avatar @user, avatar UserID, " +
              "avatar @user (128, 256, 512), avatar UserID (128, 256, 512)");
      case "buildembed" -> sendEmbed(ce, display, "__Command: BuildEmbed__",
          "Builds embeds using the Discord message line."
              + " This command is very sensitive to user input, and any exceptions will cancel the embed " +
              "building process. The switch signal for the bot to know which embed properties " +
              "to include is either T for true/include or F for false/exclude. Use no arguments" +
              " to bring up a reminder of what the switches are, and one argument that includes" +
              " the 9 character switch block to begin. From there, the rest of the process will " +
              "be bot requested inputs and user feedback. The options displayed in brackets ([]) " +
              "are different types of variations of embed properties and their number of arguments" +
              " to use them. There is no prefix necessary, you can type normally for input. The " +
              "signal to let the bot know that you've finished with one argument or field is to " +
              "include >> at the end of every argument you wish to use. Although Tenor gif links " +
              "are not acceptable as image previews, saving the gif and uploading it to use its " +
              "media link from there will work. \n\n__**Glossary:**__ \n(hyperlink): clickable text | " +
              "(url): a website link or media location | (inline): true or false - determines " +
              "whether or not the field is on the same row as the previous",
          "buildembed, embed, embedtemplate", "[0]BuildEmbed Switches [1]9 Character Switch",
          "buildembed, buildembed TTTTTTTTT");
      case "choice" -> sendEmbed(ce, display, "__Command: Choice__",
          "Chooses randomly between any number of options. " +
              "The options are arguments provided by commas (,).",
          "choice, choose, pick", "[1, ++]Option",
          "choice Take out the trash, Do the laundry, Walk the dog");
      case "clear" -> sendEmbed(ce, display, "__Command: Clear__",
          "Clears a number of messages. " +
              "Argument provides how many (2 - 100).",
          "clear, purge, clean", "[1]Number", "clear (2-100)");
      case "credits" -> sendEmbed(ce, display, "__Command: Credits__",
          "Provides user with a list of credits for the bot.",
          "credits", "[0]Credits", "credits");
      case "dungeonarchives" -> sendEmbed(ce, display, "__Command: DungeonArchives__",
          "Discord advertisment for Dungeon Archives.",
          "dungeonarchives, dainvite", "[0]DungeonArchives", "dungeonarchives");
      case "echo" -> sendEmbed(ce, display, "__Command: Echo__",
          "Repeats the user's text. Argument provides the text content.",
          "echo, repeat, copycat", "[0++]Text", "echo Stop repeating me!");
      case "emote" -> sendEmbed(ce, display, "__Command: Emote__",
          "Provides the mentioned custom emote as a file. " +
              "Argument is the requested emote.",
          "emote, emoji", "[1]Emote", "emote :happyFeetDance:");
      case "flip" -> sendEmbed(ce, display, "__Command: Flip__",
          "Flips a coin any number of times. " +
              "Argument provides how many times to flip the coin. (1-10)",
          "flip, coinflip, headsortails", "[0]Once [1]Number",
          "flip, flip [1-10]");
      case "help" -> sendEmbed(ce, display, "__Command: Help__",
          "Provides documentation on Lucyfer commands. " +
              "Argument describes more detailed command usage.",
          "help, manual, instructions",
          "[0]HelpCommandDescription [1]CommandName", "help help");
      case "highorlow" -> sendEmbed(ce, display, "__HighOrLow__",
          "Guess whether the next number will be higher or lower!",
          "highorlow, hol", "[0]HighOrLow", "highorlow");
      case "info" -> sendEmbed(ce, display, "__Command: Info__",
          "Provides information on the bot and its developer.",
          "info, commands.commands.games.commands.games.about.about.games.commands.games.about.about", "[0]Info", "info");
      case "ping" -> sendEmbed(ce, display, "__Command: Ping__",
          "Response time of the bot in milliseconds.",
          "ping, response", "[0]Ping", "ping");
      case "random" -> sendEmbed(ce, display, "__Command: Random__",
          "Provides a random image.",
          "random", "[0]random", "random");
      case "remind" -> sendEmbed(ce, display, "__Command: Remind__",
          "Sets a timer and alerts the user when the time expires for up to " +
              "a day's maximum length. Arguments provide the time duration, type, " +
              "and event name. The bot recognizes the following data types for 1 argument: " +
              "(0-86400)s, (0-1440)m, (0-24)h - and for 2+ arguments: hours, hour, hrs, hr, " +
              "h, minutes, minute, mins, min, m, seconds, second, secs, sec, s.",
          "remind, reminder, remindme, notify, mentionme, alert, timer",
          "[1]TimeDuration&TimeType/Time [2]TimeDuration/TimeType/EventName [3++]EventName",
          "remind (0-86400)s, remind (0-1440)m, remind (0-24)h, "
              + "remind TimeDurationTimeType EventName, remind TimeDuration TimeType EventName");
      case "roll" -> sendEmbed(ce, display, "__Command: Roll__",
          "Dice roll and integer RNG (random number generator). No arguments to roll once. "
              + "One argument to roll (1-10) times. Three arguments to set your own RNG - lower bound" +
              "and upper-bound values are included in the range.",
          "roll, rng, dice", "[0]Once [1]Number [2]LowerBound [3]UpperBound",
          "roll, roll (1-10), roll (1-10) (0++) (1-214748367)");
      case "serverinfo" -> sendEmbed(ce, display, "__Command: ServerInfo__",
          "Provides information on the server.",
          "serverinfo, discord, server", "[0]ServerInfo", "serverinfo");
      case "settings" -> sendEmbed(ce, display, "__Command: Settings__",
          "Provides information on bot settings.",
          "settings, config",
          "[0]Settings [1]Setting [2]True/False", "settings, settings (setting) (true/false) ");
      case "shutdown" -> sendEmbed(ce, display, "__Command: Shutdown__",
          "Shuts the bot down. Use only when absolutely necessary.",
          "shutdown , turnoff, terminate", "[0]Shutdown", "shutdown");
      case "whois" -> sendEmbed(ce, display, "__Command: WhoIs__",
          "Provides information on the user.",
          "whois, profile, user", "[0]Self [1]Mention/UserID",
          "whois, whois @user, whois UserID");
      default -> {
        display.setTitle("__Command Not Found__");
        display.setDescription("Try typing " + Settings.getPrefix() + "commands for a full command list.");
        Settings.sendEmbed(ce, display);
      }
    }
  }

  // Help details display
  private void sendEmbed(CommandEvent ce, EmbedBuilder display, String title, String description, String aliases, String arguments,
                         String examples) {
    display.setTitle(title);
    display.setDescription(description);
    display.addField("**Aliases:**", aliases, false);
    display.addField("**Arguments:**", arguments, false);
    display.addField("**Examples:**", examples, false);
    Settings.sendEmbed(ce, display);
  }
}