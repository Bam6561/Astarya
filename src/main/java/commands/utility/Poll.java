package commands.utility;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import commands.owner.Settings;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.util.concurrent.TimeUnit;

public class Poll extends Command {
  private EventWaiter waiter;

  public Poll(EventWaiter waiter) {
    this.name = "poll";
    this.aliases = new String[]{"poll", "polls", "vote"};
    this.arguments = "[2, ++]PollOptions";
    this.help = "Creates a reaction vote with up to 10 options.";
    this.waiter = waiter;
  }

  @Override
  protected void execute(CommandEvent ce) {
    Settings.deleteInvoke(ce);
    String[] args = ce.getMessage().getContentRaw().split("\\s"); // Parse message for arguments
    int arguments = args.length;
    // No arguments provided
    if (arguments == 1) {
      ce.getChannel().sendMessage("You need to provide some options separated by a comma.").queue();
    } else {// Parse arguments for options provided
      String[] options = parseOptions(args);
      if (checkForEmptyOptions(options)) { // Empty option
        ce.getChannel().sendMessage("None of the choices provided can be empty.").queue();
      } else { // Prepare poll
        if ((options.length > 1) && (options.length < 11)) { // 1 - 10 options
          createPoll(ce, options);
          setPollOptions(options);
        } else if (options.length > 10) { // More than 10 options
          ce.getChannel().sendMessage("You can only provide up to 10 options.").queue();
        }  else { // Less than 2 options
          ce.getChannel().sendMessage("You need to provide more than 1 option.").queue();
        }
      }
    }
  }

  // Parse arguments for choices
  private String[] parseOptions(String[] args) {
    StringBuilder optionsStringBuilder = new StringBuilder();
    for (int i = 1; i < args.length; i++) {
      optionsStringBuilder.append(args[i]).append(" ");
    }
    return optionsStringBuilder.toString().split(","); // Split choices provided
  }

  // Check for empty spaces in options
  private boolean checkForEmptyOptions(String[] choices) {
    boolean emptyOptionsExists = false;
    int pointer = 0;
    while ((!emptyOptionsExists) && (pointer < choices.length)) {
      if (choices[pointer].equals(" ")) {
        emptyOptionsExists = true;
      }
      pointer++;
    }
    return emptyOptionsExists;
  }

  // Create poll
  private void createPoll(CommandEvent ce, String[] options) {
    EmbedBuilder display = new EmbedBuilder();
    display.setTitle("__Poll__");
    StringBuilder displayOptions = new StringBuilder();
    displayOptions.append("It's time to vote!\n");
    for (int i = 0; i < options.length; i++) {
      displayOptions.append("**[").append(i+1).append("]**").append(" ").append(options[i]).append("\n");
    }
    display.setDescription(displayOptions);
    Settings.sendEmbed(ce, display);
  }

  // Add reactions to poll
  private void setPollOptions(String[] options) {
    int numberOfOptions = options.length;
    waiter.waitForEvent(GuildMessageReceivedEvent.class,
        w -> !w.getMessage().getEmbeds().isEmpty()
            && (w.getMessage().getEmbeds().get(0).getTitle().equals("__Poll__")),
        w -> {
          if (1 <= numberOfOptions) w.getMessage().addReaction("1️⃣").queue();
          if (2 <= numberOfOptions) w.getMessage().addReaction("2️⃣").queue();
          if (3 <= numberOfOptions) w.getMessage().addReaction("3️⃣").queue();
          if (4 <= numberOfOptions) w.getMessage().addReaction("4️⃣").queue();
          if (5 <= numberOfOptions) w.getMessage().addReaction("5️⃣").queue();
          if (6 <= numberOfOptions) w.getMessage().addReaction("6️⃣").queue();
          if (7 <= numberOfOptions) w.getMessage().addReaction("7️⃣").queue();
          if (8 <= numberOfOptions) w.getMessage().addReaction("8️⃣").queue();
          if (9 <= numberOfOptions) w.getMessage().addReaction("9️⃣").queue();
          if (10 == numberOfOptions) w.getMessage().addReaction("0️⃣").queue();
        }, 15, TimeUnit.SECONDS, () -> {
        });
  }
}

