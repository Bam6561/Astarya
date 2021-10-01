package commands.games;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import commands.owner.Settings;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;

import java.util.Random;
import java.util.concurrent.TimeUnit;

public class HighOrLow extends Command {
  private final EventWaiter waiter;
  private static int firstNumber = 0;
  private static int secondNumber = 0;
  private static String playerID;
  private static boolean currentlyPlaying = false;

  public HighOrLow(EventWaiter waiter) {
    this.name = "highorlow";
    this.aliases = new String[]{"highorlow", "hol"};
    this.help = "Guess whether the next number will be higher or lower!";
    this.waiter = waiter;
  }

  @Override
  protected void execute(CommandEvent ce) {
    Settings.deleteInvoke(ce);
    if (!getCurrentlyPlaying()) { // No ongoing game
      gameSet(ce);
      gameScreen(ce);
      gameReactions(ce);
      gameTimeout(ce);
    } else { // Ongoing game
      ce.getChannel()
          .sendMessage("A high or low game is currently being played. Please wait until it finishes or expires.")
          .queue();
    }
  }

  // Lock game to one player
  private void gameSet(CommandEvent ce) {
    Random rand = new Random();
    setFirstNumber(rand.nextInt(101) + 1);
    setSecondNumber(rand.nextInt(101) + 1);
    setPlayerID(ce.getMember().getUser().getId());
    setCurrentlyPlaying(true);
    while (getFirstNumber() == getSecondNumber()) { // Ensure numbers are not equal
      setSecondNumber(rand.nextInt(101) + 1);
    }
  }

  private void gameScreen(CommandEvent ce) {
    EmbedBuilder display = new EmbedBuilder();
    display.setTitle("__HighOrLow__");
    display.setDescription("My number is (" + getFirstNumber() + ") from a range of numbers from 1 - 100. "
        + "\nWill the next number I think of be higher or lower?");
    Settings.sendEmbed(ce, display);
  }

  // Embed reactions
  public void gameReactions(CommandEvent ce) {
    waiter.waitForEvent(GuildMessageReceivedEvent.class, // Add reactions
        w -> !w.getMessage().getEmbeds().isEmpty()
            && (w.getMessage().getEmbeds().get(0).getTitle().equals("__HighOrLow__")),
        w -> {
          w.getMessage().addReaction("🔼").queue();
          w.getMessage().addReaction("🔽").queue();
        }, 15, TimeUnit.SECONDS, () -> {
        });
    waiter.waitForEvent(GuildMessageReactionAddEvent.class, // Lock reactions to player
        w -> w.getMember().getUser().getId().equals(getPlayerID())
            && (w.getReactionEmote().getName().equals("🔼")
            || (w.getReactionEmote().getName().equals("🔽"))),
        w -> gameResults(ce), 15, TimeUnit.SECONDS, () -> {
        });
  }

  public void gameResults(CommandEvent e) {
    EmbedBuilder display = new EmbedBuilder();
    if (getFirstNumber() > getSecondNumber()) {
      display.setTitle("__HighOrLow__");
      display.setDescription("||I thought of (" + getSecondNumber() + "). The number was lower!||");
    } else {
      display.setTitle("__HighOrLow__");
      display.setDescription("||I thought of (" + getSecondNumber() + "). The number was higher!||");
    }
    Settings.sendEmbed(e, display);
    setCurrentlyPlaying(false);
    setPlayerID("");
  }

  // User doesn't react within 15s
  public void gameTimeout(CommandEvent ce) {
    new java.util.Timer().schedule(new java.util.TimerTask() { // Game Non-action Timeout (15s)
      public void run() {
        if (getCurrentlyPlaying()) {
          EmbedBuilder display = new EmbedBuilder();
          display.setTitle("__HighOrLow__");
          display
              .setDescription(ce.getMember().getAsMention() + " took too long to choose, and the game has expired!");
          Settings.sendEmbed(ce, display);
          setCurrentlyPlaying(false);
        }
      }
    }, 15000);
  }

  private static int getFirstNumber() {
    return HighOrLow.firstNumber;
  }

  private static int getSecondNumber() {
    return HighOrLow.secondNumber;
  }

  public static String getPlayerID() {
    return HighOrLow.playerID;
  }

  private static boolean getCurrentlyPlaying() {
    return HighOrLow.currentlyPlaying;
  }

  private static void setFirstNumber(int firstNumber) {
    HighOrLow.firstNumber = firstNumber;
  }

  private static void setSecondNumber(int secondNumber) {
    HighOrLow.secondNumber = secondNumber;
  }

  private static void setPlayerID(String playerID) {
    HighOrLow.playerID = playerID;
  }

  private static void setCurrentlyPlaying(boolean status) {
    HighOrLow.currentlyPlaying = status;
  }

}
