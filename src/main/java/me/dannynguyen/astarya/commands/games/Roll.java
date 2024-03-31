package me.dannynguyen.astarya.commands.games;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import me.dannynguyen.astarya.commands.owner.Settings;
import net.dv8tion.jda.api.EmbedBuilder;

import java.util.Random;

/**
 * Command invocation that simulates dice rolls and
 * randomly generates integers based on a user provided range.
 *
 * @author Danny Nguyen
 * @version 1.9.0
 * @since 1.0
 */
public class Roll extends Command {
  /**
   * Associates the command with its properties.
   */
  public Roll() {
    this.name = "roll";
    this.aliases = new String[]{"roll", "rng", "dice"};
    this.arguments = "[0]Once [1]NumberOfRolls [2]Min [3]Max";
    this.help = "Dice roll and random integer generator.";
  }

  /**
   * Either:
   * <ul>
   *  <li> sends the results of rolling a six sided die once
   *  <li> the result of rolling a die multiple times
   *  <li> the results of multiple rolls of random integers between a user provided custom range
   * </ul>
   * <p>
   * For multiple die rolls, the user only provides the number of times to roll. For multiple custom range
   * rolls, the user provides the number of times to roll, followed by a minimum and maximum range.
   *
   * @param ce command event
   */
  @Override
  protected void execute(CommandEvent ce) {
    Settings.deleteInvoke(ce);

    String[] parameters = ce.getMessage().getContentRaw().split("\\s");
    int numberOfParameters = parameters.length - 1;

    switch (numberOfParameters) {
      case 0 -> rollDieOnce(ce);
      case 1 -> rollDieMultiple(ce, parameters);
      case 3 -> customRangeRolls(ce, parameters);
      default -> ce.getChannel().sendMessage("Invalid parameter format.").queue();
    }
  }

  /**
   * Rolls a six sided die once.
   *
   * @param ce command event
   */
  private void rollDieOnce(CommandEvent ce) {
    Random rand = new Random();
    EmbedBuilder embed = new EmbedBuilder();
    embed.setAuthor("Roll");
    embed.setDescription("You rolled a **(" + (rand.nextInt(6) + 1) + ")**.");
    Settings.sendEmbed(ce, embed);
  }

  /**
   * Checks if the user requested number of rolls is an integer
   * and within a valid range of 1-10 before rolling a die multiple times.
   *
   * @param ce         command event
   * @param parameters user provided parameters
   */
  private void rollDieMultiple(CommandEvent ce, String[] parameters) {
    try {
      int numberOfRolls = Integer.parseInt(parameters[1]);
      if ((numberOfRolls >= 1) && (numberOfRolls <= 10)) {
        Random rand = new Random();
        StringBuilder rollResults = new StringBuilder();

        // Generate list of roll results
        for (int i = 0; i < numberOfRolls; i++) {
          rollResults.append("\n").append(i + 1).append(": **(").append(rand.nextInt(6) + 1).append(")** ");
        }

        EmbedBuilder embed = new EmbedBuilder();
        embed.setAuthor("Rolls");
        embed.setDescription(rollResults.toString());
        Settings.sendEmbed(ce, embed);
      } else {
        ce.getChannel().sendMessage(Error.EXCEED_RANGE.message).queue();
      }
    } catch (NumberFormatException e) {
      ce.getChannel().sendMessage(Error.EXCEED_RANGE.message).queue();
    }
  }

  /**
   * Checks if the user requested number of rolls are an integer and that their minimum
   * and maximum values are zero or positive, are not equal to each other, and the minimum
   * is not larger than the maximum value before rolling a die with custom values.
   *
   * @param ce         command event
   * @param parameters user provided parameters
   */
  private void customRangeRolls(CommandEvent ce, String[] parameters) {
    try {
      int numberOfRolls = Integer.parseInt(parameters[1]);
      int min = Integer.parseInt(parameters[2]);
      int max = Integer.parseInt(parameters[3]);

      boolean validNumberOfRolls = (numberOfRolls >= 1) && (numberOfRolls <= 10);
      boolean minAndMaxAreZeroOrPositive = (min >= 0) && (max >= 0);
      boolean minAndMaxAreNotEqual = min != max;
      boolean minIsNotLargerThanMax = !(min > max);

      boolean validRNGConstraints = validNumberOfRolls && minAndMaxAreZeroOrPositive && minAndMaxAreNotEqual && minIsNotLargerThanMax;

      if (validRNGConstraints) {
        StringBuilder rollResults = new StringBuilder();

        if (numberOfRolls == 1) {
          rollResults.append("You rolled a **(").append(new Random().nextInt(max - min + 1) + min).append(")**.");
        } else {
          Random random = new Random();
          for (int i = 0; i < numberOfRolls; i++) { // Generate list of roll results
            String rollResult = Integer.toString(random.nextInt(max - min + 1) + min);
            rollResults.append("\n").append(i + 1).append(": **(").append(rollResult).append(")** ");
          }
        }

        EmbedBuilder embed = new EmbedBuilder();
        embed.setAuthor("RNG");
        embed.setDescription(rollResults.toString());
        Settings.sendEmbed(ce, embed);
      } else {
        if (!validNumberOfRolls) {
          ce.getChannel().sendMessage("Provide between 1-10 times to generate numbers.").queue();
        }
        if (!minAndMaxAreZeroOrPositive) {
          ce.getChannel().sendMessage("Minimum and maximum cannot be negative.").queue();
        }
        if (!minAndMaxAreNotEqual) {
          ce.getChannel().sendMessage("Minimum cannot be equal to maximum.").queue();
        }
        if (!minIsNotLargerThanMax) {
          ce.getChannel().sendMessage("Minimum cannot be larger than maximum.").queue();
        }
      }
    } catch (NumberFormatException e) {
      ce.getChannel().sendMessage("Provide between 1-10 for number of rolls and range.").queue();
    }
  }

  /**
   * Types of errors.
   */
  private enum Error {
    /**
     * Out of range.
     */
    EXCEED_RANGE("Provide between 1-10 times to roll dice.");

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
