package me.dannynguyen.astarya.commands.utility;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import me.dannynguyen.astarya.commands.owner.Settings;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.concurrent.TimeUnit;

/**
 * Command invocation that creates a reaction vote with up to 10 options.
 *
 * @author Danny Nguyen
 * @version 1.8.1
 * @since 1.0
 */
public class Poll extends Command {
  private final EventWaiter waiter;

  public Poll(EventWaiter waiter) {
    this.name = "poll";
    this.aliases = new String[]{"poll", "vote"};
    this.arguments = "[2, ++]Options";
    this.help = "Creates a reaction vote with up to 10 options.";
    this.waiter = waiter;
  }

  /**
   * Checks if the user provided options to read the poll command request.
   * <p>
   * Users can provide up to 10 options in the poll, separated by commas, but no more than one.
   *
   * @param ce command event
   */
  @Override
  protected void execute(CommandEvent ce) {
    Settings.deleteInvoke(ce);

    String[] parameters = ce.getMessage().getContentRaw().split("\\s");
    int numberOfParameters = parameters.length - 1;

    boolean optionsProvided = numberOfParameters != 0;
    if (optionsProvided) {
      readPollRequest(ce, parameters);
    } else {
      ce.getChannel().sendMessage(Failure.SEPARATE_OPTIONS.text).queue();
    }
  }

  /**
   * Checks if user provided no empty parameters before creating a poll.
   *
   * @param ce         command event
   * @param parameters user provided parameters
   */
  private void readPollRequest(CommandEvent ce, String[] parameters) {
    String[] options = readOptions(parameters);

    boolean noEmptyOptions = !checkForEmptyPollOptions(options);
    if (noEmptyOptions) { // Prepare poll
      processPollRequest(ce, options);
    } else {
      ce.getChannel().sendMessage(Failure.EMPTY_OPTION.text).queue();
    }
  }

  /**
   * Splits user provided parameters into an array of poll options with the comma character as a delimiter.
   *
   * @param parameters user provided parameters
   * @return array of poll options
   */
  private String[] readOptions(String[] parameters) {
    StringBuilder optionsStringBuilder = new StringBuilder();
    for (int i = 1; i < parameters.length; i++) {
      optionsStringBuilder.append(parameters[i]).append(" ");
    }
    return optionsStringBuilder.toString().split(","); // Split options provided
  }

  /**
   * Checks for empty options.
   *
   * @param options array of options provided by the user
   * @return if there exists an empty option in the array
   */
  private boolean checkForEmptyPollOptions(String[] options) {
    for (String option : options) { // Find the first blank option (if any)
      if (option.equals(" ")) return true;
    }
    return false;
  }

  /**
   * Creates a poll embed with emojis to react to.
   *
   * @param ce      command event
   * @param options user provided options
   */
  private void processPollRequest(CommandEvent ce, String[] options) {
    int numberOfOptions = options.length;
    boolean moreThanOneOption = numberOfOptions > 1;
    boolean noMoreThanTenOptions = numberOfOptions < 11;
    boolean validNumberOfOptions = moreThanOneOption && noMoreThanTenOptions;

    if (validNumberOfOptions) {
      createPoll(ce, options);
      setPollOptions(options);
    } else {
      ce.getChannel().sendMessage(Failure.EXCEED_RANGE.text).queue();
    }
  }

  /**
   * Sends an embed containing poll information.
   *
   * @param ce      command event
   * @param options user provided options
   */
  private void createPoll(CommandEvent ce, String[] options) {
    EmbedBuilder display = new EmbedBuilder();
    display.setAuthor("Poll");

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

    waiter.waitForEvent(MessageReceivedEvent.class,
        w -> !w.getMessage().getEmbeds().isEmpty()
            && (w.getMessage().getEmbeds().get(0).getDescription().contains("It's time to vote!")),
        w -> {
          w.getMessage().addReaction(Emoji.fromFormatted("1️⃣")).queue();
          if (numberOfOptions >= 2) w.getMessage().addReaction(Emoji.fromFormatted("2️⃣")).queue();
          if (numberOfOptions >= 3) w.getMessage().addReaction(Emoji.fromFormatted("3️⃣")).queue();
          if (numberOfOptions >= 4) w.getMessage().addReaction(Emoji.fromFormatted("4️⃣")).queue();
          if (numberOfOptions >= 5) w.getMessage().addReaction(Emoji.fromFormatted("5️⃣")).queue();
          if (numberOfOptions >= 6) w.getMessage().addReaction(Emoji.fromFormatted("6️⃣")).queue();
          if (numberOfOptions >= 7) w.getMessage().addReaction(Emoji.fromFormatted("7️⃣")).queue();
          if (numberOfOptions >= 8) w.getMessage().addReaction(Emoji.fromFormatted("8️⃣")).queue();
          if (numberOfOptions >= 9) w.getMessage().addReaction(Emoji.fromFormatted("9️⃣")).queue();
          if (numberOfOptions == 10) w.getMessage().addReaction(Emoji.fromFormatted("0️⃣")).queue();
        }, 15, TimeUnit.SECONDS, () -> {
        });
  }

  private enum Failure {
    SEPARATE_OPTIONS("Provide options separated by a comma."),
    EMPTY_OPTION("Empty option."),
    EXCEED_RANGE("Provide between than 1-10 options.");

    public final String text;

    Failure(String text) {
      this.text = text;
    }
  }
}

