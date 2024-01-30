package commands.about;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import commands.owner.Settings;
import net.dv8tion.jda.api.EmbedBuilder;

/**
 * Help is a command invocation that provides documentation on Astarya's commands.
 *
 * @author Danny Nguyen
 * @version 1.8.0
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
          HelpEnum.Description.CHOOSE.text, HelpEnum.Alias.CHOOSE.text,
          HelpEnum.Parameter.CHOOSE.text, HelpEnum.Example.CHOOSE.text);
      case "clearqueue", "clear" -> sendDetailedCommandHelpEmbed(display, "Help: ClearQueue",
          HelpEnum.Description.CLEARQUEUE.text, HelpEnum.Alias.CLEARQUEUE.text,
          HelpEnum.Parameter.CLEARQUEUE.text, HelpEnum.Example.CLEARQUEUE.text);
      case "coinflip", "flip" -> sendDetailedCommandHelpEmbed(display, "Help: Coin Flip",
          HelpEnum.Description.COINFLIP.text, HelpEnum.Alias.COINFLIP.text,
          HelpEnum.Parameter.COINFLIP.text, HelpEnum.Example.COINFLIP.text);
      case "color" -> sendDetailedCommandHelpEmbed(display, "Help: Color",
          HelpEnum.Description.COLOR.text, HelpEnum.Alias.COLOR.text,
          HelpEnum.Parameter.COLOR.text, HelpEnum.Example.COLOR.text);
      case "credits" -> sendDetailedCommandHelpEmbed(display, "Help: Credits",
          HelpEnum.Description.CREDITS.text, HelpEnum.Alias.CREDITS.text,
          HelpEnum.Parameter.CREDITS.text, HelpEnum.Example.CREDITS.text);
      case "delete", "purge" -> sendDetailedCommandHelpEmbed(display, "Help: Delete",
          HelpEnum.Description.DELETE.text, HelpEnum.Alias.DELETE.text,
          HelpEnum.Parameter.DELETE.text, HelpEnum.Example.DELETE.text);
      case "emote", "emoji" -> sendDetailedCommandHelpEmbed(display, "Help: Emote",
          HelpEnum.Description.EMOTE.text, HelpEnum.Alias.EMOTE.text,
          HelpEnum.Parameter.EMOTE.text, HelpEnum.Example.EMOTE.text);
      case "help" -> sendDetailedCommandHelpEmbed(display, "Help: Help",
          HelpEnum.Description.HELP.text, HelpEnum.Alias.HELP.text,
          HelpEnum.Parameter.HELP.text, HelpEnum.Example.HELP.text);
      case "highorlow", "guess" -> sendDetailedCommandHelpEmbed(display, "Help: HighOrLow",
          HelpEnum.Description.HIGHORLOW.text, HelpEnum.Alias.HIGHORLOW.text,
          HelpEnum.Parameter.HIGHORLOW.text, HelpEnum.Example.HIGHORLOW.text);
      case "info", "about" -> sendDetailedCommandHelpEmbed(display, "Help: Info",
          HelpEnum.Description.INFO.text, HelpEnum.Alias.INFO.text,
          HelpEnum.Parameter.INFO.text, HelpEnum.Example.INFO.text);
      case "join", "j" -> sendDetailedCommandHelpEmbed(display, "Help: Join",
          HelpEnum.Description.JOIN.text, HelpEnum.Alias.JOIN.text,
          HelpEnum.Parameter.JOIN.text, HelpEnum.Example.JOIN.text);
      case "leave", "l", "disconnect", "dc" -> sendDetailedCommandHelpEmbed(display, "Help: Leave",
          HelpEnum.Description.LEAVE.text, HelpEnum.Alias.LEAVE.text,
          HelpEnum.Parameter.LEAVE.text, HelpEnum.Example.LEAVE.text);
      case "loop", "repeat" -> sendDetailedCommandHelpEmbed(display, "Help: Loop",
          HelpEnum.Description.LOOP.text, HelpEnum.Alias.LOOP.text,
          HelpEnum.Parameter.LOOP.text, HelpEnum.Example.LOOP.text);
      case "lyrics" -> sendDetailedCommandHelpEmbed(display, "Help: Lyrics",
          HelpEnum.Description.LYRICS.text, HelpEnum.Alias.LYRICS.text,
          HelpEnum.Parameter.LYRICS.text, HelpEnum.Example.LYRICS.text);
      case "nowplaying", "np" -> sendDetailedCommandHelpEmbed(display, "Help: NowPlaying",
          HelpEnum.Description.NOWPLAYING.text, HelpEnum.Alias.NOWPLAYING.text,
          HelpEnum.Parameter.NOWPLAYING.text, HelpEnum.Example.NOWPLAYING.text);
      case "pandorasbox", "pb" -> sendDetailedCommandHelpEmbed(display, "Help: PandorasBox",
          HelpEnum.Description.PANDORASBOX.text, HelpEnum.Alias.PANDORASBOX.text,
          HelpEnum.Parameter.PANDORASBOX.text, HelpEnum.Example.PANDORASBOX.text);
      case "pause", "stop" -> sendDetailedCommandHelpEmbed(display, "Help: Pause",
          HelpEnum.Description.PAUSE.text, HelpEnum.Alias.PAUSE.text,
          HelpEnum.Parameter.PAUSE.text, HelpEnum.Example.PAUSE.text);
      case "ping", "ms" -> sendDetailedCommandHelpEmbed(display, "Help: Ping",
          HelpEnum.Description.PING.text, HelpEnum.Alias.PING.text,
          HelpEnum.Parameter.PING.text, HelpEnum.Example.PING.text);
      case "play", "p" -> sendDetailedCommandHelpEmbed(display, "Help: Play",
          HelpEnum.Description.PLAY.text, HelpEnum.Alias.PLAY.text,
          HelpEnum.Parameter.PLAY.text, HelpEnum.Example.PLAY.text);
      case "playnext", "after" -> sendDetailedCommandHelpEmbed(display, "Help: PlayNext",
          HelpEnum.Description.PLAYNEXT.text, HelpEnum.Alias.PLAYNEXT.text,
          HelpEnum.Parameter.PLAYNEXT.text, HelpEnum.Example.PLAYNEXT.text);
      case "poll", "vote" -> sendDetailedCommandHelpEmbed(display, "Help: Poll",
          HelpEnum.Description.POLL.text, HelpEnum.Alias.POLL.text,
          HelpEnum.Parameter.POLL.text, HelpEnum.Example.POLL.text);
      case "profile", "whois", "user" -> sendDetailedCommandHelpEmbed(display, "Help: Profile",
          HelpEnum.Description.PROFILE.text, HelpEnum.Alias.PROFILE.text,
          HelpEnum.Parameter.PROFILE.text, HelpEnum.Example.PROFILE.text);
      case "queue", "q" -> sendDetailedCommandHelpEmbed(display, "Help: Queue",
          HelpEnum.Description.QUEUE.text, HelpEnum.Alias.QUEUE.text,
          HelpEnum.Parameter.QUEUE.text, HelpEnum.Example.QUEUE.text);
      case "remind", "timer" -> sendDetailedCommandHelpEmbed(display, "Help: Remind",
          HelpEnum.Description.REMIND.text, HelpEnum.Alias.REMIND.text,
          HelpEnum.Parameter.REMIND.text, HelpEnum.Example.REMIND.text);
      case "remove", "r" -> sendDetailedCommandHelpEmbed(display, "Help: Remove",
          HelpEnum.Description.REMOVE.text, HelpEnum.Alias.REMOVE.text,
          HelpEnum.Parameter.REMOVE.text, HelpEnum.Example.REMOVE.text);
      case "return", "ret" -> sendDetailedCommandHelpEmbed(display, "Help: Return",
          HelpEnum.Description.RETURN.text, HelpEnum.Alias.RETURN.text,
          HelpEnum.Parameter.RETURN.text, HelpEnum.Example.RETURN.text);
      case "roll", "rng", "dice" -> sendDetailedCommandHelpEmbed(display, "Help: Roll",
          HelpEnum.Description.ROLL.text, HelpEnum.Alias.ROLL.text,
          HelpEnum.Parameter.ROLL.text, HelpEnum.Example.ROLL.text);
      case "searchtrack", "search", "st" -> sendDetailedCommandHelpEmbed(display, "Help: SearchTrack",
          HelpEnum.Description.SEARCHTRACK.text, HelpEnum.Alias.SEARCHTRACK.text,
          HelpEnum.Parameter.SEARCHTRACK.text, HelpEnum.Example.SEARCHTRACK.text);
      case "server" -> sendDetailedCommandHelpEmbed(display, "Help: Server",
          HelpEnum.Description.SERVER.text, HelpEnum.Alias.SERVER.text,
          HelpEnum.Parameter.SERVER.text, HelpEnum.Example.SERVER.text);
      case "setposition", "setpos" -> sendDetailedCommandHelpEmbed(display, "Help: SetPosition",
          HelpEnum.Description.SETPOSITION.text, HelpEnum.Alias.SETPOSITION.text,
          HelpEnum.Parameter.SETPOSITION.text, HelpEnum.Example.SETPOSITION.text);
      case "settings", "config" -> sendDetailedCommandHelpEmbed(display, "Help: Settings",
          HelpEnum.Description.SETTINGS.text, HelpEnum.Alias.SETTINGS.text,
          HelpEnum.Parameter.SETTINGS.text, HelpEnum.Example.SETTINGS.text);
      case "shuffle", "mix" -> sendDetailedCommandHelpEmbed(display, "Help: Shuffle",
          HelpEnum.Description.SHUFFLE.text, HelpEnum.Alias.SHUFFLE.text,
          HelpEnum.Parameter.SHUFFLE.text, HelpEnum.Example.SHUFFLE.text);
      case "shutdown" -> sendDetailedCommandHelpEmbed(display, "Help: Shutdown",
          HelpEnum.Description.SHUTDOWN.text, HelpEnum.Alias.SHUTDOWN.text,
          HelpEnum.Parameter.SHUTDOWN.text, HelpEnum.Example.SHUTDOWN.text);
      case "skip", "s", "next" -> sendDetailedCommandHelpEmbed(display, "Help: Skip",
          HelpEnum.Description.SKIP.text, HelpEnum.Alias.SKIP.text,
          HelpEnum.Parameter.SKIP.text, HelpEnum.Example.SKIP.text);
      case "swap", "switch" -> sendDetailedCommandHelpEmbed(display, "Help: Swap",
          HelpEnum.Description.SWAP.text, HelpEnum.Alias.SWAP.text,
          HelpEnum.Parameter.SWAP.text, HelpEnum.Example.SWAP.text);
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