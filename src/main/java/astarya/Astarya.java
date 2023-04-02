package astarya;

import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import commands.about.Credits;
import commands.about.Help;
import commands.about.Info;
import commands.about.Ping;
import commands.audio.*;
import commands.games.Choose;
import commands.games.CoinFlip;
import commands.games.HighOrLow;
import commands.games.Roll;
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

/**
 * Astarya represents the Discord bot application as an object. Through
 * event listeners and the command client (an object representing the bot's
 * various command modules), the bot can process various Discord API requests
 * given to it by users in Discord chat through the usage of its bot token.
 *
 * @author Danny Nguyen
 * @version 1.6.1
 * @since 1.0
 */
public class Astarya {
  private static JDA api;

  /**
   * Initializes Astarya and associates all of its
   * necessary components together as a singular application.
   *
   * @param args Command line parameters
   * @throws Exception unknown error
   */
  public static void main(String[] args) {
    // Login
    Dotenv dotenv = Dotenv.load();
    try {
      api = JDABuilder.createDefault(dotenv.get("BOT_TOKEN")).enableIntents(GatewayIntent.MESSAGE_CONTENT,
          GatewayIntent.GUILD_EMOJIS_AND_STICKERS, GatewayIntent.GUILD_VOICE_STATES,
          GatewayIntent.SCHEDULED_EVENTS).build().awaitReady();
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
    String alternativePrefix = "L:";

    // Command Client settings
    commands.setOwnerId("204448598539239424"); // Bam#3531
    commands.setHelpWord("commands");
    commands.setPrefix(prefix);
    commands.setAlternativePrefix(alternativePrefix);
    commands.addCommands(new Avatar(), new Emote(), new Poll(waiter), new Remind(),
        new Server(), new Profile(), new Delete(), new Settings(prefix, alternativePrefix),
        new Shutdown(), new Ping(), new Choose(), new CoinFlip(), new HighOrLow(waiter), new Roll(),
        new ClearQueue(), new Join(), new Leave(), new Loop(), new NowPlaying(), new Pause(),
        new Play(), new PlayNext(), new Queue(), new Remove(), new Return(), new SearchTrack(waiter),
        new SetPosition(), new Shuffle(), new Skip(), new Swap(), new Credits(), new Help(), new Info());
    CommandClient commandClient = commands.build();

    // Initialize AstaryaBot
    api.addEventListener(commandClient, waiter, new MessageLog());
  }

  /**
   * Returns a JDA object that can be
   * used to access the Discord API
   *
   * @return Discord API
   */
  public JDA getApi() {
    return api;
  }
}