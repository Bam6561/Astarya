package commands.games;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import commands.owner.Settings;
import net.dv8tion.jda.api.EmbedBuilder;

import java.util.Random;

public class Choose extends Command {
  private String optionsString;

  public Choose() {
    this.name = "choose";
    this.aliases = new String[]{"choose", "choice", "pick", "option"};
    this.arguments = "[1, ++]Options";
    this.help = "Chooses randomly between any number of options.";
  }

  // Sends an embed containing a random choice from user provided options
  @Override
  protected void execute(CommandEvent ce) {
    Settings.deleteInvoke(ce);

    // Parse message for arguments
    String[] arguments = ce.getMessage().getContentRaw().split("\\s");
    int numberOfArguments = arguments.length - 1;

    if (numberOfArguments != 0) { // Options provided
      chooseOption(ce, arguments);
    } else { // No arguments provided
      ce.getChannel().sendMessage("Options need to be separated by a comma.").queue();
    }
  }

  // Sends an embed that presents a random choice from options provided
  private void chooseOption(CommandEvent ce, String[] arguments) {
    String[] options = separateOptionsFromArguments(arguments);
    boolean noEmptyOptionProvided = !checkIfEmptyOptionProvided(options);
    if (noEmptyOptionProvided) { // Randomly choose an option from provided
      Random random = new Random();
      int randomOption = random.nextInt(options.length);

      EmbedBuilder display = new EmbedBuilder();
      display.setTitle("__Choice__");
      display.setDescription("Based on the options you provided... \n\n" + getOptionsString()
          + "\n\n**I have chosen:** \n||" + options[randomOption] + "||");

      Settings.sendEmbed(ce, display);
    } else { // Empty option
      ce.getChannel().sendMessage("None of the options provided can be empty.").queue();
    }
  }

  // Parse arguments for options
  private String[] separateOptionsFromArguments(String[] arguments) {
    StringBuilder optionsStringBuilder = new StringBuilder();
    for (int i = 1; i < arguments.length; i++) {
      optionsStringBuilder.append(arguments[i]).append(" ");
    }

    setOptionsString(optionsStringBuilder.toString()); // Store user input options
    return optionsString.split(","); // Split options provided for processing
  }

  // Check for blank options
  private boolean checkIfEmptyOptionProvided(String[] options) {
    for (String option : options) { // Find the first blank option (if any)
      if (option.equals(" ")) return true;
    }
    return false;
  }

  // Get and set optionsString
  private String getOptionsString() {
    return this.optionsString;
  }

  private void setOptionsString(String optionsString) {
    this.optionsString = optionsString;
  }
}
