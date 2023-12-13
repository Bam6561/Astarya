package astarya;

import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import commands.about.Credits;
import commands.about.Help;
import commands.about.Info;
import commands.about.Ping;
import commands.audio.*;
import commands.games.*;
import commands.owner.Delete;
import commands.owner.Settings;
import commands.owner.Shutdown;
import commands.utility.*;
import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Astarya represents the Discord bot application as an object. Through event listeners and the
 * command client (an object representing the bot's various command modules), the bot can process
 * various Discord API requests given to it by users in Discord chat through the usage of its bot token.
 *
 * @author Danny Nguyen
 * @version 1.6.11
 * @since 1.0
 */
public class Astarya {
  private static JDA api;

  /**
   * Initializes Astarya and associates all of its necessary components together as a singular application.
   *
   * @param args Command line parameters
   * @throws Exception unknown error
   */
  public static void main(String[] args) {
    // Login
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
          api.getSelfUser().getDiscriminator() + "] is online.");
    } catch (Exception e) {
      e.printStackTrace();
    }

    // Bot Presence
    api.getPresence().setStatus(OnlineStatus.DO_NOT_DISTURB);
    api.getPresence().setActivity(Activity.listening("Nothing"));

    // Command Manager
    CommandClientBuilder commands = new CommandClientBuilder();
    EventWaiter waiter = new EventWaiter();

    // Prefixes
    String prefix = "<";
    String alternativePrefix = "A:";

    // Command Client settings
    commands.setOwnerId("204448598539239424"); // Bam#6561
    commands.setHelpWord("commands");
    commands.setPrefix(prefix);
    commands.setAlternativePrefix(alternativePrefix);
    commands.addCommands(new Emote(), new Poll(waiter), new Profile(), new Remind(),
        new Server(), new Delete(), new Settings(prefix, alternativePrefix),
        new Shutdown(), new Ping(), new Choose(), new CoinFlip(), new HighOrLow(waiter),
        new PandorasBox(loadPandorasBoxPrompts(".\\resources\\pandoras_box_prompts.txt")),
        new Roll(), new ClearQueue(), new Join(), new Leave(), new Loop(), new Lyrics(), new NowPlaying(),
        new Pause(), new Play(), new PlayNext(), new Queue(), new Remove(), new Return(), new SearchTrack(waiter),
        new SetPosition(), new Shuffle(), new Skip(), new Swap(), new Credits(), new Help(), new Info());
    CommandClient commandClient = commands.build();

    // Initialize Astarya
    api.addEventListener(commandClient, waiter, new MessageLog());
  }

  /**
   * Loads prompts for Pandora's Box command into memory from a text
   * file, with each line representing a different individual prompt.
   *
   * @return Pandora's Box prompts
   */
  private static ArrayList<String> loadPandorasBoxPrompts(String filePath) {
    try {
      File file = new File(filePath);
      Scanner scanner = new Scanner(file);
      ArrayList<String> prompts = new ArrayList<>();

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
   * Returns a JDA object that can be used to access the Discord API.
   *
   * @return Discord API
   */
  public JDA getApi() {
    return api;
  }
}