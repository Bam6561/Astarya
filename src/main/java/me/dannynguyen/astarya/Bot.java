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
import me.dannynguyen.astarya.utils.TextReader;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.managers.Presence;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

/**
 * Represents the Discord bot application.
 * <p>
 * Through event listeners and the command client, the bot can
 * process various Discord API requests given to it by users
 * in Discord chat through the usage of its bot token.
 *
 * @author Danny Nguyen
 * @version 1.8.6
 * @since 1.0
 */
public class Bot {
  /**
   * Bot version.
   */
  public static final String version = "V1.8.16";

  /**
   * Discord API.
   */
  private static JDA api;

  /**
   * Command client.
   */
  private static CommandClient commandClient;

  /**
   * No parameter constructor.
   */
  public Bot() {
  }

  /**
   * Initializes Astarya and associates all of its
   * necessary components together as a singular application.
   *
   * @param args command line parameters
   */
  public static void main(String[] args) {
    try {
      api = JDABuilder.createDefault(Dotenv.load().get("BOT_TOKEN"))
          .setMemberCachePolicy(MemberCachePolicy.ALL)
          .enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_EMOJIS_AND_STICKERS, GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_PRESENCES, GatewayIntent.GUILD_VOICE_STATES, GatewayIntent.SCHEDULED_EVENTS)
          .enableCache(CacheFlag.ACTIVITY, CacheFlag.ONLINE_STATUS)
          .build().awaitReady();
      System.out.println("[" + api.getSelfUser().getName() + "#" + api.getSelfUser().getDiscriminator() + " " + Bot.version + "] Online");
    } catch (Exception e) {
      e.printStackTrace();
    }

    Presence presence = api.getPresence();
    presence.setStatus(OnlineStatus.DO_NOT_DISTURB);
    presence.setActivity(Activity.listening("Nothing"));

    EventWaiter waiter = new EventWaiter();
    commandClient = createCommandClient(waiter);
    api.addEventListener(commandClient, waiter, new MessageEvent());
  }

  /**
   * Creates the command listener.
   *
   * @param waiter event waiter
   * @return command listener
   */
  private static CommandClient createCommandClient(EventWaiter waiter) {
    String prefix = "<";
    String alternativePrefix = "A:";

    return new CommandClientBuilder()
        .setOwnerId("204448598539239424") // Bam6561
        .setHelpWord("commands")
        .setPrefix(prefix)
        .setAlternativePrefix(alternativePrefix)
        .addCommands(new ColorRole(loadColorRoles()), new Emote(), new Poll(waiter), new Profile(),
            new Remind(), new Server(), new Delete(), new Settings(prefix, alternativePrefix),
            new Shutdown(), new Ping(), new Choose(), new CoinFlip(), new HighOrLow(waiter),
            new PandorasBox(loadPandorasBoxPrompts()), new Roll(), new ClearQueue(), new Join(),
            new Leave(), new Loop(), new Lyrics(), new NowPlaying(), new Pause(), new Play(), new PlayNext(),
            new Queue(), new Remove(), new Return(), new SearchTrack(waiter), new SetPosition(),
            new Shuffle(), new Skip(), new Swap(), new Credits(), new Help(), new Info())
        .build();
  }

  /**
   * Loads prompts for Pandora's Box command into memory from a text
   * file, with each line representing a different individual prompt.
   *
   * @return Pandora's Box prompts
   */
  private static List<String> loadPandorasBoxPrompts() {
    try {
      Scanner scanner = new Scanner(new File(".\\resources\\pandoras_box_prompts.txt"));
      List<String> prompts = new ArrayList<>();

      while (scanner.hasNextLine()) {
        prompts.add(scanner.nextLine());
      }
      scanner.close();

      System.out.println("Pandora's Box prompts loaded.");
      return prompts;
    } catch (FileNotFoundException e) {
      System.out.println("Pandora's Box prompts not found.");
      return null;
    }
  }

  /**
   * Loads the server's color role names into memory
   * and deletes empty color roles if they exist.
   *
   * @return color role names
   */
  private static Set<String> loadColorRoles() {
    Set<String> colorRoles = new HashSet<>();
    Guild guild = api.getMutualGuilds().get(0);

    for (Role role : Bot.api.getRoles()) {
      String roleName = role.getName();
      if (TextReader.isHexColorCode(roleName.toUpperCase())) {
        if (!guild.getMembersWithRoles(role).isEmpty()) {
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
  @NotNull
  public static JDA getApi() {
    return api;
  }

  /**
   * Gets the bot's command client.
   *
   * @return bot's command client
   */
  @NotNull
  public static CommandClient getCommandClient() {
    return commandClient;
  }
}