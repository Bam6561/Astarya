package commands.games;

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
 * @version 1.5.4
 * @since 1.0
 */
public class Roll extends Command {
  public Roll() {
    this.name = "roll";
    this.aliases = new String[]{"roll", "rng", "dice", "random"};
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
   * @param ce object containing information about the command event
   */
  @Override
  protected void execute(CommandEvent ce) {
    Settings.deleteInvoke(ce);

    // Parse message for arguments
    String[] arguments = ce.getMessage().getContentRaw().split("\\s");
    int numberOfArguments = arguments.length - 1;

    switch (numberOfArguments) {
      case 0 -> dieRoll(ce);
      case 1 -> multipleDieRolls(ce, arguments);
      case 3 -> customRangeRolls(ce, arguments);
      default -> ce.getChannel().sendMessage("Invalid argument format.").queue();
    }
  }

  /**
   * Rolls a six sided die once.
   *
   * @param ce object containing information about the command event
   */
  private void dieRoll(CommandEvent ce) {
    Random rand = new Random();
    EmbedBuilder display = new EmbedBuilder();
    display.setTitle("__Roll__");
    display.setDescription("You rolled a **(" + (rand.nextInt(6) + 1) + ")**.");
    Settings.sendEmbed(ce, display);
  }

  /**
   * Checks whether the user requested number of rolls is an integer and within a valid range of 1-10.
   *
   * @param ce        object containing information about the command event
   * @param arguments user provided arguments
   * @throws NumberFormatException user provided non-integer value
   */
  private void multipleDieRolls(CommandEvent ce, String[] arguments) {
    try {
      int numberOfRolls = Integer.parseInt(arguments[1]);
      boolean validNumberOfRolls = (numberOfRolls >= 1) && (numberOfRolls <= 10);
      if (validNumberOfRolls) {
        multipleDieRollsResults(ce, numberOfRolls);
      } else {
        ce.getChannel().sendMessage("Specify an integer between (1-10) times to roll the dice.").queue();
      }
    } catch (NumberFormatException e) {
      ce.getChannel().sendMessage("Specify an integer between (1-10) times to roll the dice.").queue();
    }
  }

  /**
   * Sends the results of rolling a six sided die multiple times.
   *
   * @param ce            object containing information about the command event
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
    display.setTitle("__Rolls__");
    display.setDescription(rollResults.toString());
    Settings.sendEmbed(ce, display);
  }

  /**
   * Checks whether the user requested number of rolls are an integer and
   * that their minimum and maximum values are zero or positive, are not
   * equal to each other, and the minimum is not larger than the maximum value.
   *
   * @param ce        object containing information about the command event
   * @param arguments user provided arguments
   * @throws NumberFormatException user provided non-integer value
   */
  private void customRangeRolls(CommandEvent ce, String[] arguments) {
    try {
      int numberOfRolls = Integer.parseInt(arguments[1]);
      int min = Integer.parseInt(arguments[2]);
      int max = Integer.parseInt(arguments[3]);

      boolean validNumberOfRolls = (numberOfRolls >= 1) && (numberOfRolls <= 10);
      boolean minAndMaxAreZeroOrPositive = (min >= 0) && (max >= 0);
      boolean minAndMaxAreNotEqual = min != max;
      boolean minIsNotLargerThanMax = !(min > max);

      boolean validRNGConstraints = validNumberOfRolls && minAndMaxAreZeroOrPositive
          && minAndMaxAreNotEqual && minIsNotLargerThanMax;

      if (validRNGConstraints) {
        handleCustomRangeRolls(ce, numberOfRolls, min, max);
      } else {
        handleErrorMessages(ce, validNumberOfRolls, minAndMaxAreZeroOrPositive,
            minAndMaxAreNotEqual, minIsNotLargerThanMax);
      }
    } catch (NumberFormatException error) {
      ce.getChannel().sendMessage("Specify integers between (1-10) for the number of rolls and range.").queue();
    }
  }

  /**
   * Checks whether to generate one or multiple custom range results.
   *
   * @param ce            object that contains information about the command event
   * @param numberOfRolls number of times to generate integers
   * @param min           minimum value in custom range
   * @param max           maximum value in custom range
   */
  private void handleCustomRangeRolls(CommandEvent ce, int numberOfRolls, int min, int max) {
    StringBuilder rollResults = new StringBuilder();

    if (numberOfRolls == 1) {
      oneCustomRangeRoll(min, max, rollResults);
    } else {
      multipleCustomRangeRolls(numberOfRolls, min, max, rollResults);
    }

    EmbedBuilder display = new EmbedBuilder();
    display.setTitle("__RNG__");
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
    for (int i = 0; i < numberOfRolls; i++) { // Generate a list of roll results
      String rollResult = Integer.toString(random.nextInt(max - min + 1) + min);
      rollResults.append("\n").append(i + 1).append(": **(").append(rollResult).append(")** ");
    }
  }

  /**
   * Sends error messages for invalid arguments provided by the user.
   *
   * @param ce                         object containing information about the command event
   * @param validNumberOfRolls         number of rolls is between 1-10
   * @param minAndMaxAreZeroOrPositive minimum and maximum values in custom range are zero or positive
   * @param minAndMaxAreNotEqual       minimum and maximum values in custom range are not equal
   * @param minIsNotLargerThanMax      minimum value is not larger than maximum value
   */
  private void handleErrorMessages(CommandEvent ce, boolean validNumberOfRolls, boolean minAndMaxAreZeroOrPositive,
                                   boolean minAndMaxAreNotEqual, boolean minIsNotLargerThanMax) {
    if (!validNumberOfRolls) {
      ce.getChannel().sendMessage("Specify an integer between (1-10) times to generate numbers.").queue();
    }
    if (!minAndMaxAreZeroOrPositive) {
      ce.getChannel().sendMessage("Minimum and maximum values cannot be negative.").queue();
    }
    if (!minAndMaxAreNotEqual) {
      ce.getChannel().sendMessage("Minimum value cannot be larger than the maximum value.").queue();
    }
    if (minIsNotLargerThanMax) {
      ce.getChannel().sendMessage("The lower bound cannot be larger than the upper bound.").queue();
    }
  }
}
