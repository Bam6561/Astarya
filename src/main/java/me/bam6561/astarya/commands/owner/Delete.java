package me.bam6561.astarya.commands.owner;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import me.bam6561.astarya.enums.BotMessage;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;

import java.util.List;

/**
 * Command invocation that clears a number of 2-100 recent messages.
 *
 * @author Danny Nguyen
 * @version 1.8.14
 * @since 1.0
 */
public class Delete extends Command {
  /**
   * Associates command with its properties.
   */
  public Delete() {
    this.name = "delete";
    this.aliases = new String[]{"delete", "purge"};
    this.arguments = "[1]NumberOfMessages";
    this.help = "Clears a number of 2-100 recent messages.";
    this.ownerCommand = true;
  }

  /**
   * Checks if the user provided a parameter before reading the command request.
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
      ce.getChannel().sendMessage(BotMessage.INVALID_NUMBER_OF_PARAMETERS.getMessage()).queue();
    }
  }

  /**
   * Checks if the number of messages to delete is an
   * integer and within the valid range before deleting messages.
   *
   * @param ce         command event
   * @param parameters user provided parameters
   */
  private void readDeleteMessagesRequest(CommandEvent ce, String[] parameters) {
    try {
      int numberOfMessagesToDelete = Integer.parseInt(parameters[1]);
      if ((numberOfMessagesToDelete >= 2) && (numberOfMessagesToDelete <= 100)) {
        MessageChannel textChannel = ce.getChannel();
        List<Message> recentMessages = textChannel.getHistory().retrievePast(numberOfMessagesToDelete).complete();

        try {
          textChannel.purgeMessages(recentMessages);
          textChannel.sendMessage("Previous (" + numberOfMessagesToDelete + ") messages cleared.").queue();
        } catch (InsufficientPermissionException ex) {
          ce.getChannel().sendMessage(BotMessage.MISSING_PERMISSION_MANAGE_MESSAGES.getMessage()).queue();
        }
      } else {
        ce.getChannel().sendMessage(Error.EXCEED_RANGE.text).queue();
      }
    } catch (NumberFormatException e) {
      ce.getChannel().sendMessage(Error.EXCEED_RANGE.text).queue();
    }
  }

  /**
   * Types of errors.
   */
  private enum Error {
    /**
     * Out of range.
     */
    EXCEED_RANGE("Provide between 2-100 messages to clear.");

    /**
     * Message.
     */
    public final String text;

    /**
     * Associates an error with its message.
     *
     * @param message message
     */
    Error(String message) {
      this.text = message;
    }
  }
}
