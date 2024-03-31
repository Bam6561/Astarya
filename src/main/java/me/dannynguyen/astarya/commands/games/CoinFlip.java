package me.dannynguyen.astarya.commands.games;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import me.dannynguyen.astarya.commands.owner.Settings;
import me.dannynguyen.astarya.enums.BotMessage;
import net.dv8tion.jda.api.EmbedBuilder;

import java.util.Random;

/**
 * Command invocation that simulates coin flips.
 *
 * @author Danny Nguyen
 * @version 1.8.13
 * @since 1.0
 */
public class CoinFlip extends Command {
  /**
   * Associates the command with its properties.
   */
  public CoinFlip() {
    this.name = "flip";
    this.aliases = new String[]{"coinflip", "coin", "flip"};
    this.arguments = "[0]Once [1]NumberOfFlips";
    this.help = "Flips a coin.";
  }

  /**
   * Either flips a coin once or multiple times.
   *
   * @param ce command event
   */
  @Override
  protected void execute(CommandEvent ce) {
    Settings.deleteInvoke(ce);

    String[] parameters = ce.getMessage().getContentRaw().split("\\s");
    int numberOfParameters = parameters.length - 1;

    switch (numberOfParameters) {
      case 0 -> oneCoinFlip(ce);
      case 1 -> multipleCoinFlips(ce, parameters);
      default -> ce.getChannel().sendMessage(BotMessage.INVALID_NUMBER_OF_PARAMETERS.getMessage()).queue();
    }
  }

  /**
   * Flips a coin once.
   *
   * @param ce command event
   */
  private void oneCoinFlip(CommandEvent ce) {
    String flipResult;
    if (new Random().nextInt(2) == 0) {
      flipResult = "The coin landed on **Heads**.";
    } else {
      flipResult = "The coin landed on **Tails**.";
    }

    EmbedBuilder embed = new EmbedBuilder();
    embed.setAuthor("Coin Flip");
    embed.setDescription(flipResult);
    Settings.sendEmbed(ce, embed);
  }

  /**
   * Checks if the user requested number of flips is an integer and is in range of 1-10.
   *
   * @param ce         command event
   * @param parameters user provided parameters
   */
  private void multipleCoinFlips(CommandEvent ce, String[] parameters) {
    try {
      int numberOfFlips = Integer.parseInt(parameters[1]);
      boolean validNumberOfFlips = (numberOfFlips >= 1) && (numberOfFlips <= 10);
      if (validNumberOfFlips) {
        multipleFlipResults(ce, numberOfFlips);
      } else {
        ce.getChannel().sendMessage(Error.EXCEED_RANGE.message).queue();
      }
    } catch (NumberFormatException e) {
      ce.getChannel().sendMessage(Error.EXCEED_RANGE.message).queue();
    }
  }

  /**
   * Sends the results of flipping a coin multiple times.
   *
   * @param ce            command event
   * @param numberOfFlips number of times to flip a coin
   */
  private void multipleFlipResults(CommandEvent ce, int numberOfFlips) {
    Random rand = new Random();
    StringBuilder flipResults = new StringBuilder();

    // Generate list of flip results
    for (int i = 0; i < numberOfFlips; i++) {
      if (rand.nextInt(2) == 0) {
        flipResults.append("\n").append(i + 1).append(": **(Heads)** ");
      } else {
        flipResults.append("\n").append(i + 1).append(": **(Tails)** ");
      }
    }

    EmbedBuilder embed = new EmbedBuilder();
    embed.setAuthor("Coin Flips");
    embed.setDescription(flipResults.toString());
    Settings.sendEmbed(ce, embed);
  }

  /**
   * Types of errors.
   */
  private enum Error {
    /**
     * Out of range.
     */
    EXCEED_RANGE("Provide between 1-10 times to flip coin.");

    /**
     * Message.
     */
    public final String message;

    /**
     * Associates an error with its message.
     *
     * @param message message
     */
    Error(String message) {
      this.message = message;
    }
  }
}
