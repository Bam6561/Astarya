package me.bam6561.astarya.commands.utility;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import me.bam6561.astarya.commands.owner.Settings;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Command invocation that creates a reaction vote with up to 10 options.
 *
 * @author Danny Nguyen
 * @version 1.8.15
 * @since 1.0
 */
public class Poll extends Command {
  /**
   * Event waiter.
   */
  private final EventWaiter waiter;

  /**
   * Associates the command with its properties.
   *
   * @param waiter event waiter
   */
  public Poll(@NotNull EventWaiter waiter) {
    this.waiter = Objects.requireNonNull(waiter, "Null waiter");
    this.name = "poll";
    this.aliases = new String[]{"poll", "vote"};
    this.arguments = "[2, ++]Options";
    this.help = "Creates a reaction vote with up to 10 options.";
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
      new PollRequest(ce).readRequest(parameters);
    } else {
      ce.getChannel().sendMessage("Provide options separated by a comma.").queue();
    }
  }

  /**
   * Represents a poll options query.
   *
   * @author Danny Nguyen
   * @version 1.8.15
   * @since 1.8.15
   */
  private class PollRequest {
    /**
     * Command event.
     */
    private final CommandEvent ce;

    /**
     * User provided options.
     */
    private String[] options;

    /**
     * Associates the poll request with its command event.
     *
     * @param ce command event
     */
    PollRequest(CommandEvent ce) {
      this.ce = ce;
    }

    /**
     * Checks if user provided no empty parameters before creating a poll.
     *
     * @param parameters user provided parameters
     */
    private void readRequest(String[] parameters) {
      setOptions(parameters);
      if (!emptyPollOptions(options)) {
        processPollRequest();
      } else {
        ce.getChannel().sendMessage("Empty option.").queue();
      }
    }

    /**
     * Splits user provided parameters into an array of poll options with the comma character as a delimiter.
     *
     * @param parameters user provided parameters
     */
    private void setOptions(String[] parameters) {
      StringBuilder optionsStringBuilder = new StringBuilder();
      for (int i = 1; i < parameters.length; i++) {
        optionsStringBuilder.append(parameters[i]).append(" ");
      }
      options = optionsStringBuilder.toString().split(",");
    }

    /**
     * Checks for empty options.
     *
     * @param options array of options provided by the user
     * @return if there exists an empty option in the array
     */
    private boolean emptyPollOptions(String[] options) {
      for (String option : options) { // Find the first blank option (if any)
        if (option.equals(" ")) return true;
      }
      return false;
    }

    /**
     * Creates a poll embed with emojis to react to.
     */
    private void processPollRequest() {
      int numberOfOptions = options.length;
      if (numberOfOptions > 1 && numberOfOptions < 11) {
        createPoll();
        setPollOptions();
      } else {
        ce.getChannel().sendMessage("Provide between than 1-10 options.").queue();
      }
    }

    /**
     * Sends an embed containing poll information.
     */
    private void createPoll() {
      EmbedBuilder embed = new EmbedBuilder();
      embed.setAuthor("Poll");

      StringBuilder displayOptions = new StringBuilder();
      displayOptions.append("It's time to vote!\n");
      for (int i = 0; i < options.length; i++) {
        displayOptions.append("**[").append(i + 1).append("]**").append(" ").append(options[i]).append("\n");
      }
      embed.setDescription(displayOptions);

      Settings.sendEmbed(ce, embed);
    }

    /**
     * Adds reactions to the poll embed.
     */
    private void setPollOptions() {
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
  }
}

