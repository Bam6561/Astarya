package commands.owner;

import astarya.BotMessage;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;

import java.util.List;

/**
 * Delete is a command invocation that clears a number of 2-100 recent messages.
 *
 * @author Danny Nguyen
 * @version 1.7.12
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
   * @param ce command event
   */
  @Override
  protected void execute(CommandEvent ce) {
    Settings.deleteInvoke(ce);

    String[] parameters = ce.getMessage().getContentRaw().split("\\s");
    int numberOfParameters = parameters.length - 1;

    if (numberOfParameters == 1) {
      readDeleteMessagesRequest(ce, parameters);
    } else {
      ce.getChannel().sendMessage(BotMessage.Failure.INVALID_NUMBER_OF_PARAMETERS.text).queue();
    }
  }

  /**
   * Checks if the number of messages to delete is an
   * integer and within the valid range before deleting messages.
   *
   * @param ce         command event
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
        ce.getChannel().sendMessage(BotMessage.Failure.DELETE_RANGE.text).queue();
      }
    } catch (NumberFormatException e) {
      ce.getChannel().sendMessage(BotMessage.Failure.DELETE_RANGE.text).queue();
    }
  }

  /**
   * Deletes user defined amount of recent messages from the text channel.
   *
   * @param ce                       command event
   * @param numberOfMessagesToDelete number of messages to delete
   * @throws InsufficientPermissionException unable to manage messages
   */
  private void deleteRecentMessages(CommandEvent ce, int numberOfMessagesToDelete) {
    MessageChannel textChannel = ce.getChannel();
    List<Message> recentMessages = textChannel.getHistory().retrievePast(numberOfMessagesToDelete).complete();
    try {
      textChannel.purgeMessages(recentMessages);
      textChannel.sendMessage("Previous (" + numberOfMessagesToDelete + ") messages cleared.").queue();
    } catch (InsufficientPermissionException ex) {
      ce.getChannel().sendMessage(BotMessage.Failure.MISSING_PERMISSION_MANAGE_MESSAGES.text).queue();
    }
  }
}
