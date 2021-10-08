package commands.games;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import commands.owner.Settings;
import net.dv8tion.jda.api.EmbedBuilder;

import java.util.Random;

public class Flip extends Command {
  public Flip() {
    this.name = "flip";
    this.aliases = new String[]{"flip"};
    this.arguments = "[0]Once [1]Number";
    this.help = "Flips a coin.";
  }

  @Override
  protected void execute(CommandEvent ce) {
    Settings.deleteInvoke(ce);
    String[] args = ce.getMessage().getContentRaw().split("\\s"); // Parse message for arguments
    int arguments = args.length;
    switch (arguments) {
      case 1 -> randomFlip(ce); // Flip coin once
      case 2 -> multipleRandomFlips(ce, args); // Flip coin multiple times
      // Invalid arguments
      default -> ce.getChannel().sendMessage("Invalid number of arguments.").queue();
    }
  }

  private void randomFlip(CommandEvent ce) {
    Random rand = new Random();
    String flipResult = "";
    if (rand.nextInt(2) == 0) {
      flipResult += "You flipped a **Heads**.";
    } else {
      flipResult += "You flipped a **Tails**.";
    }
    EmbedBuilder display = new EmbedBuilder();
    display.setTitle("__Flip__");
    display.setDescription(flipResult);
    Settings.sendEmbed(ce, display);
  }

  private void multipleRandomFlips(CommandEvent ce, String[] args) {
    try { // Ensure argument is an integer
      int flips = Integer.parseInt(args[1]);
      if ((flips >= 1) && (flips <= 10)) { // Between 1 - 10
        Random rand = new Random();
        StringBuilder flipResults = new StringBuilder();
        for (int i = 0; i < flips; i++) { // Generate list of flip results
          if (rand.nextInt(2) == 0) {
            flipResults.append("\n").append(i + 1).append(": **(Heads)** ");
          } else {
            flipResults.append("\n").append(i + 1).append(": **(Tails)** ");
          }
        }
        EmbedBuilder display = new EmbedBuilder();
        display.setTitle("__Flips__");
        display.setDescription(flipResults.toString());
        Settings.sendEmbed(ce, display);
      } else { // Outside range of 1 - 10
        ce.getChannel().sendMessage("You need to specify a number between (1-10) times to flip the coin.").queue();
      }
    } catch (NumberFormatException error) { // Input mismatch
      ce.getChannel().sendMessage("You need to specify a number between (1-10) times to flip the coin.").queue();
    }
  }
}
