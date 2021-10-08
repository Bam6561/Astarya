package lucyfer;

import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import commands.about.Credits;
import commands.about.Help;
import commands.about.Info;
import commands.audio.*;
import commands.games.Choice;
import commands.games.Flip;
import commands.games.HighOrLow;
import commands.games.Roll;
import commands.miscellaneous.Echo;
import commands.miscellaneous.Ping;
import commands.miscellaneous.Random;
import commands.owner.Shutdown;
import commands.owner.*;
import commands.promotion.DungeonArchives;
import commands.utility.*;
import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;

import javax.security.auth.login.LoginException;

public class LucyferBot {
  private static JDA api;

  public static void main(String[] args) {
    Dotenv dotenv = Dotenv.load();
    try { // Login
      api = JDABuilder.createDefault(dotenv.get("BOT_TOKEN")).build().awaitReady();
      System.out.println("[" + api.getSelfUser().getName() + "#" +
          api.getSelfUser().getDiscriminator() + "] is online.");
    } catch (LoginException | InterruptedException e) {
      e.printStackTrace();
    }
    // Presence
    api.getPresence().setStatus(OnlineStatus.ONLINE);
    api.getPresence().setActivity(Activity.watching("Bam#3531 program me."));
    // Command Manager
    CommandClientBuilder commands = new CommandClientBuilder();
    EventWaiter waiter = new EventWaiter();
    commands.setPrefix("<");
    commands.setAlternativePrefix("L:");
    commands.setHelpWord("commands");
    commands.setOwnerId("204448598539239424"); // Bam#3531
    commands.addCommands(new Avatar(), new Emote(), new Remind(),
        new ServerInfo(), new WhoIs(), new DungeonArchives(), new BuildEmbed(waiter),
        new Delete(), new Settings(), new Shutdown(), new Volume(), new Echo(),
        new Ping(), new Random(), new Choice(), new Flip(), new HighOrLow(waiter),
        new Roll(), new ClearQueue(), new Join(), new Leave(), new Loop(),
        new NowPlaying(), new Pause(), new Play(), new PlayNext(), new Queue(),
        new Remove(), new SetPosition(), new Shuffle(), new Skip(), new Credits(),
        new Help(), new Info());
    CommandClient client = commands.build();
    // Bot
    api.addEventListener(client, waiter, new MessageLog());
  }
}