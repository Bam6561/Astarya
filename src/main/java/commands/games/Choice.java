package commands.games;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import commands.owner.Settings;
import net.dv8tion.jda.api.EmbedBuilder;

import java.util.Random;

public class Choice extends Command {
  private String choicesString;

  public Choice() {
    this.name = "choice";
    this.aliases = new String[]{"choice", "choose", "pick"};
    this.arguments = "[1, ++]Option";
    this.help = "Chooses randomly between any number of options.";
  }

  @Override
  protected void execute(CommandEvent ce) {
    Settings.deleteInvoke(ce);
    String[] args = ce.getMessage().getContentRaw().split("\\s"); // Parse message for arguments
    int arguments = args.length;
    // No arguments provided
    if (arguments == 1) {
      ce.getChannel().sendMessage("You need to provide some choices separated by a comma.").queue();
    } else {// Parse arguments for choices provided
      String[] choices = parseChoices(args);
      if (checkForEmptyChoices(choices)) { // Empty choice
        ce.getChannel().sendMessage("None of the choices provided can be empty.").queue();
      } else { // Randomly pick a choice
        randomChoice(ce, choices);
      }
    }
  }

  // Parse arguments for choices
  private String[] parseChoices(String[] args) {
    StringBuilder choicesStringBuilder = new StringBuilder();
    for (int i = 1; i < args.length; i++) {
      choicesStringBuilder.append(" ").append(args[i]);
    }
    setChoicesString(choicesStringBuilder.toString());
    return choicesString.split(","); // Split choices provided
  }

  // Check for empty spaces in choices
  private boolean checkForEmptyChoices(String[] choices) {
    boolean emptyChoiceExists = false;
    int pointer = 0;
    while ((!emptyChoiceExists) && (pointer < choices.length)) {
      if (choices[pointer].equals(" ")) {
        emptyChoiceExists = true;
      }
      pointer++;
    }
    return emptyChoiceExists;
  }

  // Pick from choices given
  private void randomChoice(CommandEvent ce, String[] choices) {
    Random rand = new Random();
    int chosen = rand.nextInt(choices.length);
    EmbedBuilder display = new EmbedBuilder();
    display.setTitle("__Choice__");
    display.setDescription("Based on the choices you provided... \n\n" + getChoicesString().substring(1)
        + "\n\n**I have chosen:** \n||" + choices[chosen] + "||");
    Settings.sendEmbed(ce, display);
  }

  // Access choicesString outside of method
  private String getChoicesString() {
    return this.choicesString;
  }

  private void setChoicesString(String choicesString) {
    this.choicesString = choicesString;
  }
}
