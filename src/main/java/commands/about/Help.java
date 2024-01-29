package commands.about;

import astarya.BotHelp;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import commands.owner.Settings;
import net.dv8tion.jda.api.EmbedBuilder;

/**
 * Help is a command invocation that provides documentation on Astarya's commands.
 *
 * @author Danny Nguyen
 * @version 1.7.14
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
   * @param ce command event
   */
  @Override
  protected void execute(CommandEvent ce) {
    Settings.deleteInvoke(ce);

    String[] parameters = ce.getMessage().getContentRaw().split("\\s");
    int numberOfParameters = parameters.length - 1;

    EmbedBuilder display = new EmbedBuilder();
    switch (numberOfParameters) {
      case 0 -> sendHelpMenu(display);
      case 1 -> sendCommandDetails(display, parameters[1].toLowerCase());
      default -> display.setDescription("Type `" + Settings.getPrefix() +
          "commands` to get a list of commands available. You can also refer to " +
          "[Astarya's Wiki](https://github.com/Bam6561/Astarya/wiki).");
    }
    Settings.sendEmbed(ce, display);
  }

  /**
   * Sends an embed containing all bot commands available to the user.
   *
   * @param display embed
   */
  private void sendHelpMenu(EmbedBuilder display) {
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
    display.addField("Utility", "> color | emote | poll | profile | remind | server", true);
  }

  /**
   * Fills out parameters for an embed containing detailed documentation of a command.
   *
   * @param display     embed
   * @param commandName command name
   */
  private void sendCommandDetails(EmbedBuilder display, String commandName) {
    switch (commandName) {
      case "choose", "pick" -> sendDetailedCommandHelpEmbed(display, "Help: Choose",
          BotHelp.Description.CHOOSE.text, BotHelp.Alias.CHOOSE.text,
          BotHelp.Parameter.CHOOSE.text, BotHelp.Example.CHOOSE.text);
      case "clearqueue", "clear" -> sendDetailedCommandHelpEmbed(display, "Help: ClearQueue",
          BotHelp.Description.CLEARQUEUE.text, BotHelp.Alias.CLEARQUEUE.text,
          BotHelp.Parameter.CLEARQUEUE.text, BotHelp.Example.CLEARQUEUE.text);
      case "coinflip", "flip" -> sendDetailedCommandHelpEmbed(display, "Help: Coin Flip",
          BotHelp.Description.COINFLIP.text, BotHelp.Alias.COINFLIP.text,
          BotHelp.Parameter.COINFLIP.text, BotHelp.Example.COINFLIP.text);
      case "color" -> sendDetailedCommandHelpEmbed(display, "Help: Color",
          BotHelp.Description.COLOR.text, BotHelp.Alias.COLOR.text,
          BotHelp.Parameter.COLOR.text, BotHelp.Example.COLOR.text);
      case "credits" -> sendDetailedCommandHelpEmbed(display, "Help: Credits",
          BotHelp.Description.CREDITS.text, BotHelp.Alias.CREDITS.text,
          BotHelp.Parameter.CREDITS.text, BotHelp.Example.CREDITS.text);
      case "delete", "purge" -> sendDetailedCommandHelpEmbed(display, "Help: Delete",
          BotHelp.Description.DELETE.text, BotHelp.Alias.DELETE.text,
          BotHelp.Parameter.DELETE.text, BotHelp.Example.DELETE.text);
      case "emote", "emoji" -> sendDetailedCommandHelpEmbed(display, "Help: Emote",
          BotHelp.Description.EMOTE.text, BotHelp.Alias.EMOTE.text,
          BotHelp.Parameter.EMOTE.text, BotHelp.Example.EMOTE.text);
      case "help" -> sendDetailedCommandHelpEmbed(display, "Help: Help",
          BotHelp.Description.HELP.text, BotHelp.Alias.HELP.text,
          BotHelp.Parameter.HELP.text, BotHelp.Example.HELP.text);
      case "highorlow", "guess" -> sendDetailedCommandHelpEmbed(display, "Help: HighOrLow",
          BotHelp.Description.HIGHORLOW.text, BotHelp.Alias.HIGHORLOW.text,
          BotHelp.Parameter.HIGHORLOW.text, BotHelp.Example.HIGHORLOW.text);
      case "info", "about" -> sendDetailedCommandHelpEmbed(display, "Help: Info",
          BotHelp.Description.INFO.text, BotHelp.Alias.INFO.text,
          BotHelp.Parameter.INFO.text, BotHelp.Example.INFO.text);
      case "join", "j" -> sendDetailedCommandHelpEmbed(display, "Help: Join",
          BotHelp.Description.JOIN.text, BotHelp.Alias.JOIN.text,
          BotHelp.Parameter.JOIN.text, BotHelp.Example.JOIN.text);
      case "leave", "l", "disconnect", "dc" -> sendDetailedCommandHelpEmbed(display, "Help: Leave",
          BotHelp.Description.LEAVE.text, BotHelp.Alias.LEAVE.text,
          BotHelp.Parameter.LEAVE.text, BotHelp.Example.LEAVE.text);
      case "loop", "repeat" -> sendDetailedCommandHelpEmbed(display, "Help: Loop",
          BotHelp.Description.LOOP.text, BotHelp.Alias.LOOP.text,
          BotHelp.Parameter.LOOP.text, BotHelp.Example.LOOP.text);
      case "lyrics" -> sendDetailedCommandHelpEmbed(display, "Help: Lyrics",
          BotHelp.Description.LYRICS.text, BotHelp.Alias.LYRICS.text,
          BotHelp.Parameter.LYRICS.text, BotHelp.Example.LYRICS.text);
      case "nowplaying", "np" -> sendDetailedCommandHelpEmbed(display, "Help: NowPlaying",
          BotHelp.Description.NOWPLAYING.text, BotHelp.Alias.NOWPLAYING.text,
          BotHelp.Parameter.NOWPLAYING.text, BotHelp.Example.NOWPLAYING.text);
      case "pandorasbox", "pb" -> sendDetailedCommandHelpEmbed(display, "Help: PandorasBox",
          BotHelp.Description.PANDORASBOX.text, BotHelp.Alias.PANDORASBOX.text,
          BotHelp.Parameter.PANDORASBOX.text, BotHelp.Example.PANDORASBOX.text);
      case "pause", "stop" -> sendDetailedCommandHelpEmbed(display, "Help: Pause",
          BotHelp.Description.PAUSE.text, BotHelp.Alias.PAUSE.text,
          BotHelp.Parameter.PAUSE.text, BotHelp.Example.PAUSE.text);
      case "ping", "ms" -> sendDetailedCommandHelpEmbed(display, "Help: Ping",
          BotHelp.Description.PING.text, BotHelp.Alias.PING.text,
          BotHelp.Parameter.PING.text, BotHelp.Example.PING.text);
      case "play", "p" -> sendDetailedCommandHelpEmbed(display, "Help: Play",
          BotHelp.Description.PLAY.text, BotHelp.Alias.PLAY.text,
          BotHelp.Parameter.PLAY.text, BotHelp.Example.PLAY.text);
      case "playnext", "after" -> sendDetailedCommandHelpEmbed(display, "Help: PlayNext",
          BotHelp.Description.PLAYNEXT.text, BotHelp.Alias.PLAYNEXT.text,
          BotHelp.Parameter.PLAYNEXT.text, BotHelp.Example.PLAYNEXT.text);
      case "poll", "vote" -> sendDetailedCommandHelpEmbed(display, "Help: Poll",
          BotHelp.Description.POLL.text, BotHelp.Alias.POLL.text,
          BotHelp.Parameter.POLL.text, BotHelp.Example.POLL.text);
      case "profile", "whois", "user" -> sendDetailedCommandHelpEmbed(display, "Help: Profile",
          BotHelp.Description.PROFILE.text, BotHelp.Alias.PROFILE.text,
          BotHelp.Parameter.PROFILE.text, BotHelp.Example.PROFILE.text);
      case "queue", "q" -> sendDetailedCommandHelpEmbed(display, "Help: Queue",
          BotHelp.Description.QUEUE.text, BotHelp.Alias.QUEUE.text,
          BotHelp.Parameter.QUEUE.text, BotHelp.Example.QUEUE.text);
      case "remind", "timer" -> sendDetailedCommandHelpEmbed(display, "Help: Remind",
          BotHelp.Description.REMIND.text, BotHelp.Alias.REMIND.text,
          BotHelp.Parameter.REMIND.text, BotHelp.Example.REMIND.text);
      case "remove", "r" -> sendDetailedCommandHelpEmbed(display, "Help: Remove",
          BotHelp.Description.REMOVE.text, BotHelp.Alias.REMOVE.text,
          BotHelp.Parameter.REMOVE.text, BotHelp.Example.REMOVE.text);
      case "return", "ret" -> sendDetailedCommandHelpEmbed(display, "Help: Return",
          BotHelp.Description.RETURN.text, BotHelp.Alias.RETURN.text,
          BotHelp.Parameter.RETURN.text, BotHelp.Example.RETURN.text);
      case "roll", "rng", "dice" -> sendDetailedCommandHelpEmbed(display, "Help: Roll",
          BotHelp.Description.ROLL.text, BotHelp.Alias.ROLL.text,
          BotHelp.Parameter.ROLL.text, BotHelp.Example.ROLL.text);
      case "searchtrack", "search", "st" -> sendDetailedCommandHelpEmbed(display, "Help: SearchTrack",
          BotHelp.Description.SEARCHTRACK.text, BotHelp.Alias.SEARCHTRACK.text,
          BotHelp.Parameter.SEARCHTRACK.text, BotHelp.Example.SEARCHTRACK.text);
      case "server" -> sendDetailedCommandHelpEmbed(display, "Help: Server",
          BotHelp.Description.SERVER.text, BotHelp.Alias.SERVER.text,
          BotHelp.Parameter.SERVER.text, BotHelp.Example.SERVER.text);
      case "setposition", "setpos" -> sendDetailedCommandHelpEmbed(display, "Help: SetPosition",
          BotHelp.Description.SETPOSITION.text, BotHelp.Alias.SETPOSITION.text,
          BotHelp.Parameter.SETPOSITION.text, BotHelp.Example.SETPOSITION.text);
      case "settings", "config" -> sendDetailedCommandHelpEmbed(display, "Help: Settings",
          BotHelp.Description.SETTINGS.text, BotHelp.Alias.SETTINGS.text,
          BotHelp.Parameter.SETTINGS.text, BotHelp.Example.SETTINGS.text);
      case "shuffle", "mix" -> sendDetailedCommandHelpEmbed(display, "Help: Shuffle",
          BotHelp.Description.SHUFFLE.text, BotHelp.Alias.SHUFFLE.text,
          BotHelp.Parameter.SHUFFLE.text, BotHelp.Example.SHUFFLE.text);
      case "shutdown" -> sendDetailedCommandHelpEmbed(display, "Help: Shutdown",
          BotHelp.Description.SHUTDOWN.text, BotHelp.Alias.SHUTDOWN.text,
          BotHelp.Parameter.SHUTDOWN.text, BotHelp.Example.SHUTDOWN.text);
      case "skip", "s", "next" -> sendDetailedCommandHelpEmbed(display, "Help: Skip",
          BotHelp.Description.SKIP.text, BotHelp.Alias.SKIP.text,
          BotHelp.Parameter.SKIP.text, BotHelp.Example.SKIP.text);
      case "swap", "switch" -> sendDetailedCommandHelpEmbed(display, "Help: Swap",
          BotHelp.Description.SWAP.text, BotHelp.Alias.SWAP.text,
          BotHelp.Parameter.SWAP.text, BotHelp.Example.SWAP.text);
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
   * @param display     embed
   * @param title       command name
   * @param description command description
   * @param aliases     command aliases
   * @param parameters  command parameters
   * @param examples    command examples
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