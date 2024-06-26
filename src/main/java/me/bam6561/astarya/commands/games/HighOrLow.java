package me.bam6561.astarya.commands.games;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import me.bam6561.astarya.commands.owner.Settings;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Command invocation that allows the user to guess if the next number will be higher or lower.
 *
 * @author Danny Nguyen
 * @version 1.9.3
 * @since 1.0
 */
public class HighOrLow extends Command {
  /**
   * Event waiter.
   */
  private final EventWaiter waiter;

  /**
   * First number generated.
   */
  private int firstNumber = -1;

  /**
   * Second number generated.
   */
  private int secondNumber = -1;

  /**
   * Player ID.
   */
  private long playerId = -1;

  /**
   * If a game exists.
   */
  private boolean ongoingGame = false;

  /**
   * Associates the command with its properties.
   *
   * @param waiter event waiter
   */
  public HighOrLow(@NotNull EventWaiter waiter) {
    this.waiter = Objects.requireNonNull(waiter, "Null waiter");
    this.name = "highorlow";
    this.aliases = new String[]{"highorlow", "guess"};
    this.help = "Guess if the next number will be higher or lower!";
  }

  /**
   * Sends an embed that only the user who invoked the command can react to.
   * <p>
   * When the same user reacts to the embed, the results screen is sent.
   * <p>
   * After a period of inactivity, the locked embed will time out.
   *
   * @param ce command event
   */
  @Override
  protected void execute(CommandEvent ce) {
    Settings.deleteInvoke(ce);

    if (!ongoingGame) {
      startGame(ce);
      sendGameScreen(ce);
      processGameReactions(ce);
      processGameTimeout(ce);
    } else {
      ce.getChannel().sendMessage("Please wait until current high or low game finishes or expires.").queue();
    }
  }

  /**
   * Starts and locks the game to the user who invoked the command.
   *
   * @param ce command event
   */
  private void startGame(CommandEvent ce) {
    ongoingGame = true;
    playerId = Long.parseLong(ce.getMember().getUser().getId());
    generateRandomPairOfNumbers();
  }

  /**
   * Generates two random numbers used for the game.
   */
  private void generateRandomPairOfNumbers() {
    Random rand = new Random();
    firstNumber = (rand.nextInt(101) + 1);
    secondNumber = (rand.nextInt(101) + 1);

    // Ensure the numbers are not equal
    while (firstNumber == secondNumber) {
      secondNumber = (rand.nextInt(101) + 1);
    }
  }

  /**
   * Sends an embed containing instructions on how to play.
   *
   * @param ce command event
   */
  private void sendGameScreen(CommandEvent ce) {
    EmbedBuilder embed = new EmbedBuilder();
    embed.setAuthor("High or Low");
    embed.setDescription("My number is (" + firstNumber + ") from a range of numbers from 1 - 100. "
        + "\nWill the next number I think of be higher or lower?");
    Settings.sendEmbed(ce, embed);
  }

  /**
   * Reacts to the game screen embed with reactions.
   *
   * @param ce command event
   */
  private void processGameReactions(CommandEvent ce) {
    // Add reactions
    waiter.waitForEvent(MessageReceivedEvent.class,
        w -> !w.getMessage().getEmbeds().isEmpty()
            && (w.getMessage().getEmbeds().get(0).getAuthor().getName().equals("High or Low")),
        w -> {
          w.getMessage().addReaction(Emoji.fromFormatted("🔼")).queue();
          w.getMessage().addReaction(Emoji.fromFormatted("🔽")).queue();
        }, 15, TimeUnit.SECONDS, () -> {
        });

    // Wait for the original user to react to the embed
    waiter.waitForEvent(MessageReactionAddEvent.class,
        w -> Long.parseLong(w.getMember().getUser().getId()) == (playerId)
            && (w.getReaction().getEmoji().getName().equals("🔼")
            || (w.getReaction().getEmoji().getName().equals("🔽"))),
        w -> displayGameResults(ce), 15, TimeUnit.SECONDS, () -> {
        });
  }

  /**
   * Sends an embed that the user didn't react within 15s and removes
   * the locked game status from the user who invoked the command.
   *
   * @param ce command event
   */
  private void processGameTimeout(CommandEvent ce) {
    new java.util.Timer().schedule(new java.util.TimerTask() { // Game Non-action Timeout (15s)
      public void run() {
        if (ongoingGame) {
          ongoingGame = false;
          EmbedBuilder embed = new EmbedBuilder();
          embed.setAuthor("High or Low");
          embed.setDescription(ce.getMember().getAsMention() + " took too long to choose, and the game has expired!");
          Settings.sendEmbed(ce, embed);
        }
      }
    }, 15000);
  }

  /**
   * Sends an embed containing the results of the randomly generated numbers
   * and removes the locked game status from the user who invoked the command.
   *
   * @param ce command event
   */
  private void displayGameResults(CommandEvent ce) {
    ongoingGame = false;
    playerId = 0;

    EmbedBuilder embed = new EmbedBuilder();
    if (firstNumber > secondNumber) {
      embed.setAuthor("High or Low");
      embed.setDescription("||I thought of (" + secondNumber + "). The number was lower!||");
    } else {
      embed.setAuthor("High or Low");
      embed.setDescription("||I thought of (" + firstNumber + "). The number was higher!||");
    }
    Settings.sendEmbed(ce, embed);
  }
}
