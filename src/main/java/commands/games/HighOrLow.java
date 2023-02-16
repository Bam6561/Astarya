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
  private EventWaiter waiter;
  private int firstNumber;
  private int secondNumber;
  private long playerID;
  private boolean ongoingGame;

  public HighOrLow(EventWaiter waiter) {
    this.name = "highorlow";
    this.aliases = new String[]{"highorlow", "guess"};
    this.help = "Guess whether the next number will be higher or lower!";
    this.waiter = waiter;
    this.firstNumber = 0;
    this.secondNumber = 0;
    this.playerID = 0;
    this.ongoingGame = false;
  }

  /*
   Sends an embed that only the user who invoked the command can react to,
   reacting sends the next screen, and without a reaction, the locked embed times out
   */
  @Override
  protected void execute(CommandEvent ce) {
    Settings.deleteInvoke(ce);

    if (!ongoingGame()) { // No ongoing game
      startGame(ce);
      displayGameScreen(ce);
      handleGameReactions(ce);
      handleGameTimeout(ce);
    } else { // Ongoing game
      ce.getChannel()
          .sendMessage("A high or low game is currently being played. Please wait until it finishes or expires.")
          .queue();
    }
  }

  // Start and lock the game to the user who invoked the command
  private void startGame(CommandEvent ce) {
    setOngoingGame(true);
    setPlayerID(Long.parseLong(ce.getMember().getUser().getId()));
    generateRandomPairOfNumbers();
  }

  // Generate two random numbers used for the game
  private void generateRandomPairOfNumbers() {
    Random rand = new Random();
    setFirstNumber(rand.nextInt(101) + 1);
    setSecondNumber(rand.nextInt(101) + 1);

    while (getFirstNumber() == getSecondNumber()) { // Ensure the numbers are not equal
      setSecondNumber(rand.nextInt(101) + 1);
    }
  }

  // Sends an embed to display to the user instructions on how to play
  private void displayGameScreen(CommandEvent ce) {
    EmbedBuilder display = new EmbedBuilder();
    display.setTitle("__HighOrLow__");
    display.setDescription("My number is (" + getFirstNumber() + ") from a range of numbers from 1 - 100. "
        + "\nWill the next number I think of be higher or lower?");

    Settings.sendEmbed(ce, display);
  }

  // React to the game screen with reactions
  private void handleGameReactions(CommandEvent ce) {
    // Add reactions
    waiter.waitForEvent(GuildMessageReceivedEvent.class,
        w -> !w.getMessage().getEmbeds().isEmpty()
            && (w.getMessage().getEmbeds().get(0).getTitle().equals("__HighOrLow__")),
        w -> {
          w.getMessage().addReaction("🔼").queue();
          w.getMessage().addReaction("🔽").queue();
        }, 15, TimeUnit.SECONDS, () -> {
        });

    // Wait for the original user to react to the embed
    waiter.waitForEvent(GuildMessageReactionAddEvent.class,
        w -> Long.parseLong(w.getMember().getUser().getId()) == (getPlayerID())
            && (w.getReactionEmote().getName().equals("🔼")
            || (w.getReactionEmote().getName().equals("🔽"))),
        w -> displayGameResults(ce), 15, TimeUnit.SECONDS, () -> {
        });
  }

  // Sends an embed that the user didn't react within 15s
  private void handleGameTimeout(CommandEvent ce) {
    new java.util.Timer().schedule(new java.util.TimerTask() { // Game Non-action Timeout (15s)
      public void run() {
        if (ongoingGame()) {
          setOngoingGame(false);

          EmbedBuilder display = new EmbedBuilder();
          display.setTitle("__HighOrLow__");
          display
              .setDescription(ce.getMember().getAsMention() + " took too long to choose, and the game has expired!");

          Settings.sendEmbed(ce, display);
        }
      }
    }, 15000);
  }

  // Sends an embed to display the results of the randomly generated numbers and clear out the locked game status
  private void displayGameResults(CommandEvent e) {
    setOngoingGame(false);
    setPlayerID(0);

    EmbedBuilder display = new EmbedBuilder();
    if (getFirstNumber() > getSecondNumber()) {
      display.setTitle("__HighOrLow__");
      display.setDescription("||I thought of (" + getSecondNumber() + "). The number was lower!||");
    } else {
      display.setTitle("__HighOrLow__");
      display.setDescription("||I thought of (" + getSecondNumber() + "). The number was higher!||");
    }

    Settings.sendEmbed(e, display);
  }

  // Get and set various variables
  private int getFirstNumber() {
    return this.firstNumber;
  }

  private int getSecondNumber() {
    return this.secondNumber;
  }

  private long getPlayerID() {
    return this.playerID;
  }

  private boolean ongoingGame() {
    return this.ongoingGame;
  }

  private void setFirstNumber(int firstNumber) {
    this.firstNumber = firstNumber;
  }

  private void setSecondNumber(int secondNumber) {
    this.secondNumber = secondNumber;
  }

  private void setPlayerID(long playerID) {
    this.playerID = playerID;
  }

  private void setOngoingGame(boolean status) {
    this.ongoingGame = status;
  }

}
