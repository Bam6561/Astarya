package commands.games;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import commands.owner.Settings;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * HighOrLow is a command invocation that allows the user
 * to guess whether the next number will be higher or lower.
 *
 * @author Danny Nguyen
 * @version 1.6.6
 * @since 1.0
 */
public class HighOrLow extends Command {
  private EventWaiter waiter;
  private int firstNumber;
  private int secondNumber;
  private long playerId;
  private boolean ongoingGame;

  public HighOrLow(EventWaiter waiter) {
    this.name = "highorlow";
    this.aliases = new String[]{"highorlow", "guess"};
    this.help = "Guess whether the next number will be higher or lower!";
    this.waiter = waiter;
    this.firstNumber = 0;
    this.secondNumber = 0;
    this.playerId = 0;
    this.ongoingGame = false;
  }


  /**
   * Sends an embed that only the user who invoked the command can react to.
   * When the same user reacts to the embed, the results screen is sent.
   * After a period of inactivity, the locked embed will time out.
   *
   * @param ce object containing information about the command event
   */
  @Override
  protected void execute(CommandEvent ce) {
    Settings.deleteInvoke(ce);

    if (!ongoingGame()) {
      startGame(ce);
      displayGameScreen(ce);
      handleGameReactions(ce);
      handleGameTimeout(ce);
    } else {
      ce.getChannel()
          .sendMessage("A high or low game is currently being played. Please wait until it finishes or expires.")
          .queue();
    }
  }

  /**
   * Starts and locks the game to the user who invoked the command.
   *
   * @param ce object containing information about the command event
   */
  private void startGame(CommandEvent ce) {
    setOngoingGame(true);
    setPlayerId(Long.parseLong(ce.getMember().getUser().getId()));
    generateRandomPairOfNumbers();
  }

  /**
   * Generates two random numbers used for the game.
   */
  private void generateRandomPairOfNumbers() {
    Random rand = new Random();
    setFirstNumber(rand.nextInt(101) + 1);
    setSecondNumber(rand.nextInt(101) + 1);

    // Ensure the numbers are not equal
    while (getFirstNumber() == getSecondNumber()) {
      setSecondNumber(rand.nextInt(101) + 1);
    }
  }

  /**
   * Sends an embed containing instructions on how to play.
   *
   * @param ce object containing information about the command event
   */
  private void displayGameScreen(CommandEvent ce) {
    EmbedBuilder display = new EmbedBuilder();
    display.setAuthor("High or Low");
    display.setDescription("My number is (" + getFirstNumber() + ") from a range of numbers from 1 - 100. "
        + "\nWill the next number I think of be higher or lower?");
    Settings.sendEmbed(ce, display);
  }

  /**
   * Reacts to the game screen embed with reactions.
   *
   * @param ce object containing information about the command event
   */
  private void handleGameReactions(CommandEvent ce) {
    // Add reactions
    waiter.waitForEvent(MessageReceivedEvent.class,
        w -> !w.getMessage().getEmbeds().isEmpty()
            && (w.getMessage().getEmbeds().get(0).getDescription().contains("Will the next number I think of be higher or lower?")),
        w -> {
          w.getMessage().addReaction(Emoji.fromFormatted("🔼")).queue();
          w.getMessage().addReaction(Emoji.fromFormatted("🔽")).queue();
        }, 15, TimeUnit.SECONDS, () -> {
        });

    // Wait for the original user to react to the embed
    waiter.waitForEvent(MessageReactionAddEvent.class,
        w -> Long.parseLong(w.getMember().getUser().getId()) == (getPlayerId())
            && (w.getReaction().getEmoji().getName().equals("🔼")
            || (w.getReaction().getEmoji().getName().equals("🔽"))),
        w -> displayGameResults(ce), 15, TimeUnit.SECONDS, () -> {
        });
  }

  /**
   * Sends an embed that the user didn't react within 15s and removes
   * the locked game status from the user who invoked the command.
   *
   * @param ce object containing information about the command event
   */
  private void handleGameTimeout(CommandEvent ce) {
    new java.util.Timer().schedule(new java.util.TimerTask() { // Game Non-action Timeout (15s)
      public void run() {
        if (ongoingGame()) {
          setOngoingGame(false);
          EmbedBuilder display = new EmbedBuilder();
          display.setAuthor("High or Low");
          display
              .setDescription(ce.getMember().getAsMention() + " took too long to choose, and the game has expired!");
          Settings.sendEmbed(ce, display);
        }
      }
    }, 15000);
  }

  /**
   * Sends an embed containing the results of the randomly generated numbers
   * and removes the locked game status from the user who invoked the command.
   *
   * @param ce object containing information about the command event
   */
  private void displayGameResults(CommandEvent ce) {
    setOngoingGame(false);
    setPlayerId(0);

    EmbedBuilder display = new EmbedBuilder();
    if (getFirstNumber() > getSecondNumber()) {
      display.setAuthor("High or Low");
      display.setDescription("||I thought of (" + getSecondNumber() + "). The number was lower!||");
    } else {
      display.setAuthor("High or Low");
      display.setDescription("||I thought of (" + getSecondNumber() + "). The number was higher!||");
    }
    Settings.sendEmbed(ce, display);
  }

  private int getFirstNumber() {
    return this.firstNumber;
  }

  private int getSecondNumber() {
    return this.secondNumber;
  }

  private long getPlayerId() {
    return this.playerId;
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

  private void setPlayerId(long playerId) {
    this.playerId = playerId;
  }

  private void setOngoingGame(boolean status) {
    this.ongoingGame = status;
  }

}
