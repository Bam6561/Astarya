package commands.utility;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import commands.owner.Settings;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.util.concurrent.TimeUnit;

/**
 * Poll is a command invocation that creates a reaction vote with up to 10 options.
 *
 * @author Danny Nguyen
 * @version 1.5.4
 * @since 1.0
 */
public class Poll extends Command {
  private EventWaiter waiter;

  public Poll(EventWaiter waiter) {
    this.name = "poll";
    this.aliases = new String[]{"poll", "vote", "react"};
    this.arguments = "[2, ++]PollOptions";
    this.help = "Creates a reaction vote with up to 10 options.";
    this.waiter = waiter;
  }

  /**
   * Processes user provided arguments to determine whether the poll command request was formatted correctly.
   * <p>
   * Users can provide up to 10 options in the poll, separated by commas, but no more than one.
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

    boolean optionsProvided = numberOfArguments != 0;
    if (optionsProvided) {
      String[] options = parseOptions(arguments);

      boolean noEmptyOptions = !checkForEmptyPollOptions(options);
      if (noEmptyOptions) { // Prepare poll
        int numberOfOptions = options.length;
        boolean moreThanOneOption = numberOfOptions > 1;
        boolean noMoreThanTenOptions = numberOfOptions < 11;
        boolean validNumberOfOptions = moreThanOneOption && noMoreThanTenOptions;

        if (validNumberOfOptions) {
          createPoll(ce, options);
          setPollOptions(options);
        } else {
          if (!moreThanOneOption) {
            ce.getChannel().sendMessage("Specify more than 1 option.").queue();
          }
          if (!noMoreThanTenOptions) {
            ce.getChannel().sendMessage("Specify only up to 10 options.").queue();
          }
        }
      } else {
        ce.getChannel().sendMessage("None of the options provided can be empty.").queue();
      }
    } else {
      ce.getChannel().sendMessage("Specify options separated by a comma.").queue();
    }
  }

  /**
   * Splits user provided arguments into an array of poll options with the comma character as a delimiter.
   *
   * @param arguments user provided arguments
   * @return array of poll options
   */
  private String[] parseOptions(String[] arguments) {
    StringBuilder optionsStringBuilder = new StringBuilder();
    for (int i = 1; i < arguments.length; i++) {
      optionsStringBuilder.append(arguments[i]).append(" ");
    }
    return optionsStringBuilder.toString().split(","); // Split options provided
  }

  /**
   * Checks for empty options.
   *
   * @param options array of options provided by the user
   * @return whether there exists an empty option in the array
   */
  private boolean checkForEmptyPollOptions(String[] options) {
    for (String option : options) { // Find the first blank option (if any)
      if (option.equals(" ")) return true;
    }
    return false;
  }

  /**
   * Sends an embed containing poll information.
   *
   * @param ce      object containing information about the command event
   * @param options user provided options
   */
  private void createPoll(CommandEvent ce, String[] options) {
    EmbedBuilder display = new EmbedBuilder();
    display.setTitle("__Poll__");

    StringBuilder displayOptions = new StringBuilder();
    displayOptions.append("It's time to vote!\n");
    for (int i = 0; i < options.length; i++) {
      displayOptions.append("**[").append(i + 1).append("]**").append(" ").append(options[i]).append("\n");
    }
    display.setDescription(displayOptions);

    Settings.sendEmbed(ce, display);
  }

  /**
   * Adds reactions to the poll embed.
   *
   * @param options user provided options
   */
  private void setPollOptions(String[] options) {
    int numberOfOptions = options.length;

    waiter.waitForEvent(GuildMessageReceivedEvent.class,
        w -> !w.getMessage().getEmbeds().isEmpty()
            && (w.getMessage().getEmbeds().get(0).getTitle().equals("__Poll__")),
        w -> {
          w.getMessage().addReaction("1️⃣").queue();
          if (numberOfOptions >= 2) w.getMessage().addReaction("2️⃣").queue();
          if (numberOfOptions >= 3) w.getMessage().addReaction("3️⃣").queue();
          if (numberOfOptions >= 4) w.getMessage().addReaction("4️⃣").queue();
          if (numberOfOptions >= 5) w.getMessage().addReaction("5️⃣").queue();
          if (numberOfOptions >= 6) w.getMessage().addReaction("6️⃣").queue();
          if (numberOfOptions >= 7) w.getMessage().addReaction("7️⃣").queue();
          if (numberOfOptions >= 8) w.getMessage().addReaction("8️⃣").queue();
          if (numberOfOptions >= 9) w.getMessage().addReaction("9️⃣").queue();
          if (numberOfOptions == 10) w.getMessage().addReaction("0️⃣").queue();
        }, 15, TimeUnit.SECONDS, () -> {
        });
  }
}

