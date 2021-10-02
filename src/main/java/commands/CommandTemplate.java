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

  @Override
  protected void execute(CommandEvent ce) {
    Settings.deleteInvoke(ce);
    String[] args = ce.getMessage().getContentRaw().split("\\s"); // Parse message for arguments
    int arguments = args.length;
  }
}
