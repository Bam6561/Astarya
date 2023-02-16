package commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import commands.owner.Settings;

public class CommandTemplate extends Command {
  public CommandTemplate() {
    this.name = "";
    this.aliases = new String[]{""};
    this.arguments = "";
    this.help = "";
    this.ownerCommand = false;
  }

  // Method does something
  @Override
  protected void execute(CommandEvent ce) {
    Settings.deleteInvoke(ce);

    // Parse raw message for arguments (if necessary)
    String[] arguments = ce.getMessage().getContentRaw().split("\\s");
    int numberOfArguments = arguments.length;
  }
}
