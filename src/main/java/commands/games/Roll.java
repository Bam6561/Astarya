package commands.games;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import commands.owner.Settings;
import net.dv8tion.jda.api.EmbedBuilder;

import java.util.Random;

public class Roll extends Command {
  public Roll() {
    this.name = "roll";
    this.aliases = new String[]{"roll", "rng", "dice", "random"};
    this.arguments = "[0]Once [1]NumberOfRolls [2]Min [3]Max";
    this.help = "Dice roll and random integer generator.";
  }

  // Sends an embed with randomly generated numbers whose range varies depending on number of arguments
  @Override
  protected void execute(CommandEvent ce) {
    Settings.deleteInvoke(ce);

    // Parse message for arguments
    String[] arguments = ce.getMessage().getContentRaw().split("\\s");
    int numberOfArguments = arguments.length;

    switch (numberOfArguments) {
      case 1 -> dieRoll(ce); // Roll six sided die once
      case 2 -> multipleDieRolls(ce, arguments); // Roll six sided die multiple times
      case 4 -> customRangeRolls(ce, arguments); // Custom RNG range
      default -> ce.getChannel().sendMessage("Invalid argument format.").queue(); // Invalid number of arguments
    }
  }

  // Roll a six sided dice once
  private void dieRoll(CommandEvent ce) {
    Random rand = new Random();

    EmbedBuilder display = new EmbedBuilder();
    display.setTitle("__Roll__");
    display.setDescription("You rolled a **(" + (rand.nextInt(6) + 1) + ")**.");

    Settings.sendEmbed(ce, display);
  }

  // Roll six sided dice multiple times
  private void multipleDieRolls(CommandEvent ce, String[] arguments) {
    try { // Ensure argument is an integer
      int numberOfRolls = Integer.parseInt(arguments[1]);
      boolean validNumberOfRolls = (numberOfRolls >= 1) && (numberOfRolls <= 10);
      if (validNumberOfRolls) {
        multipleDieRollsResults(ce, numberOfRolls);
      } else {
        ce.getChannel().sendMessage("Speicy an integer between (1-10) times to roll the dice.").queue();
      }

    } catch (NumberFormatException e) { // Non-integer input
      ce.getChannel().sendMessage("Specify an integer between (1-10) times to roll the dice.").queue();
    }
  }

  // Generates multiple die roll results
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

  // User defined number of rolls and range
  private void customRangeRolls(CommandEvent ce, String[] arguments) {
    try { // Ensure arguments are integers
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
    } catch (NumberFormatException error) { // Invalid input
      ce.getChannel().sendMessage("Specify integers between (1-10) for the number of rolls and range.").queue();
    }
  }

  // Generates one or multiple custom range results
  private void handleCustomRangeRolls(CommandEvent ce, int numberOfRolls, int min, int max) {
    StringBuilder rollResults = new StringBuilder();

    if (numberOfRolls == 1) { // Roll once
      oneCustomRangeRoll(min, max, rollResults);
    } else { // Multiple rolls
      multipleCustomRangeRolls(numberOfRolls, min, max, rollResults);
    }

    EmbedBuilder display = new EmbedBuilder();

    display.setTitle("__RNG__");
    display.setDescription(rollResults.toString());

    Settings.sendEmbed(ce, display);
  }

  // Generates one custom range result
  private void oneCustomRangeRoll(int min, int max, StringBuilder rollResults) {
    Random random = new Random();

    rollResults.append("You rolled a **(" + (random.nextInt(max - min + 1) + min) + ")**.");
  }

  // Generates multiple custom range results
  private void multipleCustomRangeRolls(int numberOfRolls, int min, int max, StringBuilder rollResults) {
    Random random = new Random();

    for (int i = 0; i < numberOfRolls; i++) { // Generate list of roll results
      String rollResult = Integer.toString(random.nextInt(max - min + 1) + min);
      rollResults.append("\n").append(i + 1).append(": **(").append(rollResult).append(")** ");
    }
  }

  // Invalid arguments provided for custom range rolls
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
