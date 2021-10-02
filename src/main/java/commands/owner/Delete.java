package commands.owner;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;

import java.util.List;

public class Delete extends Command {
  public Delete() {
    this.name = "delete";
    this.aliases = new String[]{"delete", "purge"};
    this.arguments = "[1]Number";
    this.help = "Clears a number of [2-100] messages.";
    this.ownerCommand = true;
  }

  @Override
  protected void execute(CommandEvent ce) {
    Settings.deleteInvoke(ce);
    String[] args = ce.getMessage().getContentRaw().split("\\s"); // Parse message for arguments
    int arguments = args.length;
    if (arguments == 2) {
      try { // Ensure argument is an integer
        int number = Integer.parseInt(args[1]);
        MessageChannel channel = ce.getChannel();
        deleteMessages(channel, number);
      } catch (NumberFormatException error) { // Input mismatch
        ce.getChannel().sendMessage("You must provide a number of (2-100) messages to clear.").queue();
      }
    } else { // Invalid arguments
      ce.getChannel().sendMessage("Invalid number of arguments.").queue();
    }
  }

  private void deleteMessages(MessageChannel channel, int number) {
    if (number >= 2 && number <= 100) { // Range of 2 - 100
      List<Message> messages = channel.getHistory().retrievePast(number).complete();
      channel.purgeMessages(messages);
      channel.sendMessage("Previous (" + number + ") messages cleared.").queue();
    } else { // Outside of range 2 - 100
      channel.sendMessage("You must provide a number of (2-100) messages to clear.").queue();
    }
  }
}
