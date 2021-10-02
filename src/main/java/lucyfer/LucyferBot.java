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
import commands.owner.BuildEmbed;
import commands.owner.Delete;
import commands.owner.Settings;
import commands.owner.Shutdown;
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
    commands.setPrefix("?");
    commands.setAlternativePrefix("L:");
    commands.setHelpWord("commands");
    commands.setOwnerId("204448598539239424"); // Bam#3531
    commands.addCommands(new Avatar(), new Random(), new Choice(), new Delete(),
        new Credits(), new DungeonArchives(), new Echo(), new Emote(),
        new Flip(), new Help(), new HighOrLow(waiter), new Info(), new Ping(),
        new Remind(), new Roll(), new ServerInfo(), new Settings(),
        new Shutdown(), new WhoIs(), new BuildEmbed(waiter), new Join(),
        new Play(), new Queue(), new Remove(), new ClearQueue(), new Leave());
    CommandClient client = commands.build();
    // Bot
    api.addEventListener(client, waiter, new MessageLog());
  }
}