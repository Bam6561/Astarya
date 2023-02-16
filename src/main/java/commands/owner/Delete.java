package commands.owner;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;

import java.util.List;

public class Delete extends Command {
  public Delete() {
    this.name = "delete";
    this.aliases = new String[]{"delete", "purge", "wipe"};
    this.arguments = "[1]NumberOfMessagesToDelete";
    this.help = "Clears a number of [2-100] recent messages.";
    this.ownerCommand = true;
  }

  // Deletes a number of recent messages sent in the channel
  @Override
  protected void execute(CommandEvent ce) {
    Settings.deleteInvoke(ce);

    // Parse message for arguments
    String[] arguments = ce.getMessage().getContentRaw().split("\\s");
    int numberOfArguments = arguments.length - 1;

    if (numberOfArguments == 1) { // Delete messages
      parseDeleteMessagesRequest(ce, arguments);
    } else { // Invalid arguments
      ce.getChannel().sendMessage("Invalid number of arguments.").queue();
    }
  }

  // Validates whether the number of messages to delete is valid and within range
  private void parseDeleteMessagesRequest(CommandEvent ce, String[] arguments) {
    try { // Ensure argument is an integer
      int numberOfMessagesToDelete = Integer.parseInt(arguments[1]);
      boolean validNumberOfMessagesToDelete = (numberOfMessagesToDelete >= 2) && (numberOfMessagesToDelete <= 100);
      if (validNumberOfMessagesToDelete) {
        deleteRecentMessages(ce, numberOfMessagesToDelete);
      } else {
        ce.getChannel().sendMessage("Specify an integer between (2-100) messages to clear.").queue();
      }

    } catch (NumberFormatException error) { // Non-integer input
      ce.getChannel().sendMessage("Specify an integer between (2-100) messages to clear.").queue();
    }
  }

  // Deletes user defined amount of messages from the text channel
  private void deleteRecentMessages(CommandEvent ce, int numberOfMessagesToDelete) {
    MessageChannel textChannel = ce.getChannel();
    List<Message> recentMessages = textChannel.getHistory().retrievePast(numberOfMessagesToDelete).complete();

    textChannel.purgeMessages(recentMessages);
    textChannel.sendMessage("Previous (" + numberOfMessagesToDelete + ") messages cleared.").queue();
  }
}
