package commands.games;

import astarya.BotMessage;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import commands.owner.Settings;
import net.dv8tion.jda.api.EmbedBuilder;

import java.util.Random;

/**
 * Roll is a command invocation that simulates dice rolls and
 * randomly generates integers based on a user provided range.
 *
 * @author Danny Nguyen
 * @version 1.7.12
 * @since 1.0
 */
public class Roll extends Command {
  public Roll() {
    this.name = "roll";
    this.aliases = new String[]{"roll", "rng", "dice"};
    this.arguments = "[0]Once [1]NumberOfRolls [2]Min [3]Max";
    this.help = "Dice roll and random integer generator.";
  }

  /**
   * Either sends the results of rolling a six sided die once, the result of rolling a die multiple
   * times, or the results of multiple rolls of random integers between a user provided custom range.
   * <p>
   * For multiple die rolls, the user only provides the number of times to roll. For multiple custom range
   * rolls, the user provides the number of times to roll, followed by a minimum and maximum range.
   * </p>
   *
   * @param ce command event
   */
  @Override
  protected void execute(CommandEvent ce) {
    Settings.deleteInvoke(ce);

    String[] parameters = ce.getMessage().getContentRaw().split("\\s");
    int numberOfParameters = parameters.length - 1;

    switch (numberOfParameters) {
      case 0 -> dieRoll(ce);
      case 1 -> multipleDieRolls(ce, parameters);
      case 3 -> customRangeRolls(ce, parameters);
      default -> ce.getChannel().sendMessage(BotMessage.Failure.ROLL_INVALID_INPUT.text).queue();
    }
  }

  /**
   * Rolls a six sided die once.
   *
   * @param ce command event
   */
  private void dieRoll(CommandEvent ce) {
    Random rand = new Random();
    EmbedBuilder display = new EmbedBuilder();
    display.setAuthor("Roll");
    display.setDescription("You rolled a **(" + (rand.nextInt(6) + 1) + ")**.");
    Settings.sendEmbed(ce, display);
  }

  /**
   * Checks if the user requested number of rolls is an integer
   * and within a valid range of 1-10 before rolling a die multiple times.
   *
   * @param ce         command event
   * @param parameters user provided parameters
   * @throws NumberFormatException user provided non-integer value
   */
  private void multipleDieRolls(CommandEvent ce, String[] parameters) {
    try {
      int numberOfRolls = Integer.parseInt(parameters[1]);
      boolean validNumberOfRolls = (numberOfRolls >= 1) && (numberOfRolls <= 10);
      if (validNumberOfRolls) {
        multipleDieRollsResults(ce, numberOfRolls);
      } else {
        ce.getChannel().sendMessage(BotMessage.Failure.ROLL_RANGE.text).queue();
      }
    } catch (NumberFormatException e) {
      ce.getChannel().sendMessage(BotMessage.Failure.ROLL_RANGE.text).queue();
    }
  }

  /**
   * Sends the results of rolling a six sided die multiple times.
   *
   * @param ce            command event
   * @param numberOfRolls number of times to roll the die
   */
  private void multipleDieRollsResults(CommandEvent ce, int numberOfRolls) {
    Random rand = new Random();
    StringBuilder rollResults = new StringBuilder();

    // Generate list of roll results
    for (int i = 0; i < numberOfRolls; i++) {
      rollResults.append("\n").append(i + 1).append(": **(").append(rand.nextInt(6) + 1).append(")** ");
    }

    EmbedBuilder display = new EmbedBuilder();
    display.setAuthor("Rolls");
    display.setDescription(rollResults.toString());
    Settings.sendEmbed(ce, display);
  }

  /**
   * Checks if the user requested number of rolls are an integer and that their minimum
   * and maximum values are zero or positive, are not equal to each other, and the minimum
   * is not larger than the maximum value before rolling a die with custom values.
   *
   * @param ce         command event
   * @param parameters user provided parameters
   * @throws NumberFormatException user provided non-integer value
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

      boolean validRNGConstraints = validNumberOfRolls && minAndMaxAreZeroOrPositive
          && minAndMaxAreNotEqual && minIsNotLargerThanMax;

      if (validRNGConstraints) {
        interpretCustomRangeRolls(ce, numberOfRolls, min, max);
      } else {
        processErrorMessages(ce, validNumberOfRolls, minAndMaxAreZeroOrPositive,
            minAndMaxAreNotEqual, minIsNotLargerThanMax);
      }
    } catch (NumberFormatException e) {
      ce.getChannel().sendMessage(BotMessage.Failure.ROLL_RANGES.text).queue();
    }
  }

  /**
   * Either generates one or multiple custom range results.
   *
   * @param ce            command event
   * @param numberOfRolls number of times to generate integers
   * @param min           minimum value in custom range
   * @param max           maximum value in custom range
   */
  private void interpretCustomRangeRolls(CommandEvent ce, int numberOfRolls, int min, int max) {
    StringBuilder rollResults = new StringBuilder();

    if (numberOfRolls == 1) {
      oneCustomRangeRoll(min, max, rollResults);
    } else {
      multipleCustomRangeRolls(numberOfRolls, min, max, rollResults);
    }

    EmbedBuilder display = new EmbedBuilder();
    display.setAuthor("RNG");
    display.setDescription(rollResults.toString());
    Settings.sendEmbed(ce, display);
  }

  /**
   * Generates one custom range result.
   *
   * @param min         minimum value in custom range
   * @param max         maximum value in custom range
   * @param rollResults results of the roll
   */
  private void oneCustomRangeRoll(int min, int max, StringBuilder rollResults) {
    Random random = new Random();
    rollResults.append("You rolled a **(").append(random.nextInt(max - min + 1) + min).append(")**.");
  }

  /**
   * Generates multiple custom range results.
   *
   * @param numberOfRolls number of times to generate integers
   * @param min           minimum value in custom range
   * @param max           maximum value in custom range
   * @param rollResults   results of the rolls
   */
  private void multipleCustomRangeRolls(int numberOfRolls, int min, int max, StringBuilder rollResults) {
    Random random = new Random();
    for (int i = 0; i < numberOfRolls; i++) { // Generate list of roll results
      String rollResult = Integer.toString(random.nextInt(max - min + 1) + min);
      rollResults.append("\n").append(i + 1).append(": **(").append(rollResult).append(")** ");
    }
  }

  /**
   * Sends error messages for invalid parameters provided by the user.
   *
   * @param ce                         command event
   * @param validNumberOfRolls         number of rolls is between 1-10
   * @param minAndMaxAreZeroOrPositive minimum and maximum values in custom range are zero or positive
   * @param minAndMaxAreNotEqual       minimum and maximum values in custom range are not equal
   * @param minIsNotLargerThanMax      minimum value is not larger than maximum value
   */
  private void processErrorMessages(CommandEvent ce, boolean validNumberOfRolls, boolean minAndMaxAreZeroOrPositive,
                                    boolean minAndMaxAreNotEqual, boolean minIsNotLargerThanMax) {
    if (!validNumberOfRolls) {
      ce.getChannel().sendMessage(BotMessage.Failure.ROLL_RANGE_RNG.text).queue();
    }
    if (!minAndMaxAreZeroOrPositive) {
      ce.getChannel().sendMessage(BotMessage.Failure.ROLL_NEGATIVE.text).queue();
    }
    if (!minAndMaxAreNotEqual) {
      ce.getChannel().sendMessage(BotMessage.Failure.ROLL_EQUAL.text).queue();
    }
    if (minIsNotLargerThanMax) {
      ce.getChannel().sendMessage(BotMessage.Failure.ROLL_LARGER.text).queue();
    }
  }
}
