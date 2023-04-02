package commands.games;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import commands.owner.Settings;
import net.dv8tion.jda.api.EmbedBuilder;

import java.util.Random;

/**
 * Choose is a command invocation that chooses randomly between any number of options.
 *
 * @author Danny Nguyen
 * @version 1.6.1
 * @since 1.0
 */
public class Choose extends Command {
  private String optionsString;

  public Choose() {
    this.name = "choose";
    this.aliases = new String[]{"choose", "pick"};
    this.arguments = "[1, ++]Options";
    this.help = "Chooses randomly between any number of options.";
  }

  /**
   * Processes whether user provided any arguments outside the command invocation.
   * <p>
   * Users can provide any number of options separated by a comma.
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

    if (numberOfArguments != 0) {
      chooseOption(ce, arguments);
    } else {
      ce.getChannel().sendMessage("Options need to be separated by a comma.").queue();
    }
  }

  /**
   * Sends an embed containing a random choice from user provided options.
   *
   * @param ce        object containing information about the command event
   * @param arguments user provided arguments
   */
  private void chooseOption(CommandEvent ce, String[] arguments) {
    String[] options = separateOptionsFromArguments(arguments);
    boolean noEmptyOptionProvided = !checkIfEmptyOptionProvided(options);
    if (noEmptyOptionProvided) {
      // Randomly choose an option from provided options
      Random random = new Random();
      int randomOption = random.nextInt(options.length);

      EmbedBuilder display = new EmbedBuilder();
      display.setTitle("__Choice__");
      display.setDescription("Based on the options you provided... \n\n" + getOptionsString()
          + "\n\n**I have chosen:** \n||" + options[randomOption] + "||");
      Settings.sendEmbed(ce, display);
    } else {
      ce.getChannel().sendMessage("None of the options provided can be empty.").queue();
    }
  }

  /**
   * Splits user provided arguments into an array of options with the comma character as a delimiter.
   *
   * @param arguments user provided arguments
   * @return array of options
   */
  private String[] separateOptionsFromArguments(String[] arguments) {
    StringBuilder optionsStringBuilder = new StringBuilder();
    for (int i = 1; i < arguments.length; i++) {
      optionsStringBuilder.append(arguments[i]).append(" ");
    }
    setOptionsString(optionsStringBuilder.toString()); // Store user input options
    return optionsString.split(","); // Split options provided
  }

  /**
   * Checks for empty options.
   *
   * @param options array of options provided by the user
   * @return whether there exists an empty option in the array
   */
  private boolean checkIfEmptyOptionProvided(String[] options) {
    for (String option : options) { // Find the first blank option (if any)
      if (option.equals(" ")) return true;
    }
    return false;
  }

  private String getOptionsString() {
    return this.optionsString;
  }

  private void setOptionsString(String optionsString) {
    this.optionsString = optionsString;
  }
}
