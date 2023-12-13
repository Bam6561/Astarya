package commands.owner;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;

import java.util.List;

/**
 * Delete is a command invocation that clears a number of 2-100 recent messages.
 *
 * @author Danny Nguyen
 * @version 1.6.6
 * @since 1.0
 */
public class Delete extends Command {
  public Delete() {
    this.name = "delete";
    this.aliases = new String[]{"delete", "purge"};
    this.arguments = "[1]NumberOfMessages";
    this.help = "Clears a number of 2-100 recent messages.";
    this.ownerCommand = true;
  }

  /**
   * Checks if the user provided a parameter before reading the delete command request.
   *
   * @param ce object containing information about the command event
   */
  @Override
  protected void execute(CommandEvent ce) {
    Settings.deleteInvoke(ce);

    String[] parameters = ce.getMessage().getContentRaw().split("\\s");
    int numberOfParameters = parameters.length - 1;

    if (numberOfParameters == 1) {
      readDeleteMessagesRequest(ce, parameters);
    } else {
      ce.getChannel().sendMessage("Invalid number of parameters.").queue();
    }
  }

  /**
   * Checks if the number of messages to delete is an
   * integer and within the valid range before deleting messages.
   *
   * @param ce         object containing information about the command event
   * @param parameters user provided parameters
   * @throws NumberFormatException user provided non-integer value
   */
  private void readDeleteMessagesRequest(CommandEvent ce, String[] parameters) {
    try {
      int numberOfMessagesToDelete = Integer.parseInt(parameters[1]);
      boolean validNumberOfMessagesToDelete = (numberOfMessagesToDelete >= 2) && (numberOfMessagesToDelete <= 100);
      if (validNumberOfMessagesToDelete) {
        deleteRecentMessages(ce, numberOfMessagesToDelete);
      } else {
        ce.getChannel().sendMessage("Specify an integer between (2-100) messages to clear.").queue();
      }
    } catch (NumberFormatException e) {
      ce.getChannel().sendMessage("Specify an integer between (2-100) messages to clear.").queue();
    }
  }

  /**
   * Deletes user defined amount of recent messages from the text channel.
   *
   * @param ce                       object containing information about the command event
   * @param numberOfMessagesToDelete number of messages to delete
   */
  private void deleteRecentMessages(CommandEvent ce, int numberOfMessagesToDelete) {
    MessageChannel textChannel = ce.getChannel();
    List<Message> recentMessages = textChannel.getHistory().retrievePast(numberOfMessagesToDelete).complete();
    textChannel.purgeMessages(recentMessages);
    textChannel.sendMessage("Previous (" + numberOfMessagesToDelete + ") messages cleared.").queue();
  }
}
