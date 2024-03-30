package me.dannynguyen.astarya;

import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import io.github.cdimascio.dotenv.Dotenv;
import me.dannynguyen.astarya.commands.about.Credits;
import me.dannynguyen.astarya.commands.about.Help;
import me.dannynguyen.astarya.commands.about.Info;
import me.dannynguyen.astarya.commands.about.Ping;
import me.dannynguyen.astarya.commands.audio.Queue;
import me.dannynguyen.astarya.commands.audio.*;
import me.dannynguyen.astarya.commands.games.*;
import me.dannynguyen.astarya.commands.owner.Delete;
import me.dannynguyen.astarya.commands.owner.Settings;
import me.dannynguyen.astarya.commands.owner.Shutdown;
import me.dannynguyen.astarya.commands.utility.*;
import me.dannynguyen.astarya.enums.BotMessage;
import me.dannynguyen.astarya.utility.TextReader;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

/**
 * Bot represents the Discord bot application. Through event listeners and the command
 * client (an object representing the bot's various command modules), the bot can process various
 * Discord API requests given to it by users in Discord chat through the usage of its bot token.
 *
 * @author Danny Nguyen
 * @version 1.8.1
 * @since 1.0
 */
public class Bot {
  private static JDA api;

  /**
   * Initializes Astarya and associates all of its necessary components together as a singular application.
   *
   * @param args command line parameters
   * @throws Exception unknown error
   */
  public static void main(String[] args) {
    Dotenv dotenv = Dotenv.load();
    try {
      api = JDABuilder.createDefault(dotenv.get("BOT_TOKEN")).
          setMemberCachePolicy(MemberCachePolicy.ALL).
          enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_EMOJIS_AND_STICKERS,
              GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_PRESENCES,
              GatewayIntent.GUILD_VOICE_STATES, GatewayIntent.SCHEDULED_EVENTS).
          enableCache(CacheFlag.ACTIVITY, CacheFlag.ONLINE_STATUS).
          build().awaitReady();
      System.out.println("[" + api.getSelfUser().getName() + "#" +
          api.getSelfUser().getDiscriminator() + " " + BotMessage.Success.VERSION.text + "] Online");
    } catch (Exception e) {
      e.printStackTrace();
    }
    api.getPresence().setStatus(OnlineStatus.DO_NOT_DISTURB);
    api.getPresence().setActivity(Activity.listening("Nothing"));

    EventWaiter waiter = new EventWaiter();
    api.addEventListener(createCommandClient(waiter), waiter, new MessageEvent());
  }

  /**
   * Creates a command listener.
   *
   * @param waiter event waiter
   * @return command listener
   */
  private static CommandClient createCommandClient(EventWaiter waiter) {
    CommandClientBuilder commands = new CommandClientBuilder();

    String prefix = "<";
    String alternativePrefix = "A:";

    commands.setOwnerId("204448598539239424"); // Bam#6561
    commands.setHelpWord("commands");
    commands.setPrefix(prefix);
    commands.setAlternativePrefix(alternativePrefix);
    commands.addCommands(new ColorRole(loadColorRoles()), new Emote(), new Poll(waiter), new Profile(),
        new Remind(), new Server(), new Delete(), new Settings(prefix, alternativePrefix),
        new Shutdown(), new Ping(), new Choose(), new CoinFlip(), new HighOrLow(waiter),
        new PandorasBox(loadPandorasBoxPrompts()), new Roll(), new ClearQueue(), new Join(),
        new Leave(), new Loop(), new Lyrics(), new NowPlaying(), new Pause(), new Play(), new PlayNext(),
        new Queue(), new Remove(), new Return(), new SearchTrack(waiter), new SetPosition(),
        new Shuffle(), new Skip(), new Swap(), new Credits(), new Help(), new Info());
    return commands.build();
  }

  /**
   * Loads prompts for Pandora's Box command into memory from a text
   * file, with each line representing a different individual prompt.
   *
   * @return Pandora's Box prompts
   */
  private static List<String> loadPandorasBoxPrompts() {
    try {
      File file = new File(".\\resources\\pandoras_box_prompts.txt");
      Scanner scanner = new Scanner(file);
      List<String> prompts = new ArrayList<>();

      while (scanner.hasNextLine()) {
        prompts.add(scanner.nextLine());
      }
      scanner.close();

      System.out.println(Success.PANDORAS_BOX_LOAD.text);
      return prompts;
    } catch (FileNotFoundException e) {
      System.out.println(Failure.PANDORAS_BOX_NOT_FOUND.text);
      return null;
    }
  }

  /**
   * Loads the server's color role names into memory and deletes empty color roles if they exist.
   *
   * @return color role names
   * @throws InsufficientPermissionException unable to manage roles
   */
  private static Set<String> loadColorRoles() {
    Set<String> colorRoles = new HashSet<>();

    for (Role role : Bot.api.getRoles()) {
      String roleName = role.getName();
      if (TextReader.isHexColorCode(roleName.toUpperCase())) {
        if (!api.getMutualGuilds().get(0).getMembersWithRoles(role).isEmpty()) {
          colorRoles.add(roleName);
        } else {
          try {
            role.delete().queue();
          } catch (InsufficientPermissionException ignored) {
          }
        }
      }
    }
    return colorRoles;
  }

  /**
   * Returns a JDA object that can be used to access the Discord API.
   *
   * @return Discord API
   */
  public static JDA getApi() {
    return api;
  }

  private enum Success {
    PANDORAS_BOX_LOAD("Pandora's Box prompts loaded.");

    public final String text;

    Success(String text) {
      this.text = text;
    }
  }

  private enum Failure {
    PANDORAS_BOX_NOT_FOUND("Pandora's Box prompts not found.");

    public final String text;

    Failure(String text) {
      this.text = text;
    }
  }
}