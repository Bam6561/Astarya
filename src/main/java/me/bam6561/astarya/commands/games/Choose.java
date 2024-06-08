package me.bam6561.astarya.commands.games;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import me.bam6561.astarya.commands.owner.Settings;
import net.dv8tion.jda.api.EmbedBuilder;

import java.util.Random;

/**
 * Command invocation that chooses randomly between any number of options.
 *
 * @author Danny Nguyen
 * @version 1.8.13
 * @since 1.0
 */
public class Choose extends Command {
  /**
   * Associates the command with its properties.
   */
  public Choose() {
    this.name = "choose";
    this.aliases = new String[]{"choose", "pick"};
    this.arguments = "[1, ++]Options";
    this.help = "Chooses randomly between any number of options.";
  }

  /**
   * Checks if user provided any parameters to read the command request.
   * <p>
   * Users can provide any number of options separated by a comma.
   *
   * @param ce command event
   */
  @Override
  protected void execute(CommandEvent ce) {
    Settings.deleteInvoke(ce);

    String[] parameters = ce.getMessage().getContentRaw().split("\\s");
    int numberOfParameters = parameters.length - 1;

    if (numberOfParameters == 0) {
      ce.getChannel().sendMessage("Separate options with a comma.").queue();
      return;
    }

    new ChooseRequest(ce).interpretRequest(parameters);
  }

  /**
   * Represents a choose options query.
   *
   * @author Danny Nguyen
   * @version 1.8.13
   * @since 1.8.13
   */
  private static class ChooseRequest {
    /**
     * Command event.
     */
    private final CommandEvent ce;

    /**
     * User provided options.
     */
    private String optionsString;

    /**
     * Associates the choose request with its command event.
     *
     * @param ce command event
     */
    ChooseRequest(CommandEvent ce) {
      this.ce = ce;
    }

    /**
     * Checks if the command request was formatted correctly before
     * sending an embed containing a random choice from user provided options.
     *
     * @param parameters user provided parameters
     */
    private void interpretRequest(String[] parameters) {
      String[] options = readOptions(parameters);
      if (!emptyOptionExists(options)) {
        int randomOption = new Random().nextInt(options.length);

        EmbedBuilder embed = new EmbedBuilder();
        embed.setAuthor("Choice");
        embed.setDescription("Based on the options you provided... \n\n" + optionsString + "\n\n**I have chosen:** \n||" + options[randomOption] + "||");
        Settings.sendEmbed(ce, embed);
      } else {
        ce.getChannel().sendMessage("Empty option.").queue();
      }
    }

    /**
     * Splits user provided parameters into an array of options with the comma character as a delimiter.
     *
     * @param parameters user provided parameters
     * @return array of options
     */
    private String[] readOptions(String[] parameters) {
      StringBuilder options = new StringBuilder();
      for (int i = 1; i < parameters.length; i++) {
        options.append(parameters[i]).append(" ");
      }
      optionsString = options.toString(); // Store user input options
      return optionsString.split(","); // Split options provided
    }

    /**
     * Checks for empty options.
     *
     * @param options array of options provided by the user
     * @return if there exists an empty option in the array
     */
    private boolean emptyOptionExists(String[] options) {
      for (String option : options) { // Find the first blank option (if any)
        if (option.equals(" ")) return true;
      }
      return false;
    }
  }
}
