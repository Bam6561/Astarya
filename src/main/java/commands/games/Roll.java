package commands.games;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import commands.owner.Settings;
import net.dv8tion.jda.api.EmbedBuilder;

import java.util.Random;

public class Roll extends Command {
  public Roll() {
    this.name = "roll";
    this.aliases = new String[]{"roll", "rng", "dice"};
    this.arguments = "[0]Once [1]Number [2]LowerBound [3]UpperBound";
    this.help = "Dice roll and integer RNG (random number generator).";
  }

  @Override
  protected void execute(CommandEvent ce) {
    Settings.deleteInvoke(ce);
    String[] args = ce.getMessage().getContentRaw().split("\\s"); // Parse message for arguments
    int arguments = args.length;
    switch (arguments) {
      case 1 -> randomRoll(ce); // Roll six sided die once
      case 2 -> multipleRandomRolls(ce, args); // Roll six sided die multiple times
      case 4 -> customRoll(ce, args); // Custom RNG roll
      // Invalid number of arguments
      default -> ce.getChannel().sendMessage("Invalid argument format.").queue();
    }
  }

  // Roll six sided dice once
  private void randomRoll(CommandEvent ce) {
    Random rand = new Random();
    EmbedBuilder display = new EmbedBuilder();
    display.setTitle("__Roll__");
    display.setDescription("You rolled a **(" + (rand.nextInt(6) + 1) + ")**.");
    Settings.sendEmbed(ce, display);
  }

  // Roll six sided dice multiple times
  private void multipleRandomRolls(CommandEvent ce, String[] args) {
    try { // Ensure argument is an integer
      int rolls = Integer.parseInt(args[1]);
      if ((rolls >= 1) && (rolls <= 10)) { // Between 1 - 10
        Random rand = new Random();
        StringBuilder rollResults = new StringBuilder();
        for (int i = 0; i < rolls; i++) { // Generate list of roll results
          rollResults.append("\n").append(i + 1).append(": **(").append(rand.nextInt(6) + 1).append(")** ");
        }
        EmbedBuilder display = new EmbedBuilder();
        display.setTitle("__Rolls__");
        display.setDescription(rollResults.toString());
        Settings.sendEmbed(ce, display);
      } else { // Outside range of 1 - 10
        ce.getChannel().sendMessage("You need to specify a number between (1-10) times to roll the dice.").queue();
      }
    } catch (NumberFormatException error) { // Input mismatch
      ce.getChannel().sendMessage("You need to specify a number between (1-10) times to roll the dice.").queue();
    }
  }

  // Custom user roll
  private void customRoll(CommandEvent ce, String[] args) {
    try { // Ensure arguments are integers
      int rolls = Integer.parseInt(args[1]);
      int lowerBound = Integer.parseInt(args[2]);
      int upperBound = Integer.parseInt(args[3]);
      if ((lowerBound >= 0) && (upperBound >= 0)) { // Non-zero & positive
        if (lowerBound != upperBound) { // lowerBound & upperBound cannot match
          if ((rolls >= 1) && (rolls <= 10)) { // Between 1 - 10
            try {
              Random rand = new Random();
              StringBuilder rollResults = new StringBuilder();
              if (rolls == 1) { // Roll once
                rollResults = new StringBuilder("You rolled a **(" + (rand.nextInt(upperBound - lowerBound + 1) + lowerBound) + ")**.");
              } else { // Multiple rolls
                for (int i = 0; i < rolls; i++) { // Generate list of roll results
                  String rollResult = Integer.toString(rand.nextInt(upperBound - lowerBound + 1) + lowerBound);
                  rollResults.append("\n").append(i + 1).append(": **(").append(rollResult).append(")** ");
                }
              }
              EmbedBuilder display = new EmbedBuilder();
              display.setTitle("__RNG__");
              display.setDescription(rollResults.toString());
              Settings.sendEmbed(ce, display);
            } catch (IllegalArgumentException error) { // lowerBound larger than upperBound
              ce.getChannel().sendMessage("You cannot set the lower bound higher than the upper bound.").queue();
            }
          } else { // Outside range 1 - 10
            ce.getChannel().sendMessage("You need to specify a number between (1-10) times to roll.").queue();
          }
        } else { // lowerBound & upperBound match
          ce.getChannel().sendMessage("You cannot set the lower and upper bound as the same values.").queue();
        }
      } else { // Zero or negative
        ce.getChannel().sendMessage("You can only use positive integers.").queue();
      }
    } catch (NumberFormatException error) { // Input mismatch
      ce.getChannel().sendMessage("One of the arguments is invalid.").queue();
    }
  }
}
