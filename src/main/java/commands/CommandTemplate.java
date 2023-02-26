package commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import commands.owner.Settings;

/**
 * CommandTemplate is a command invocation that does something. We just don't know what yet.
 *
 * @author Danny Nguyen
 * @version 1.5.4
 * @since 1.0
 */
public class CommandTemplate extends Command {
  public CommandTemplate() {
    this.name = "";
    this.aliases = new String[]{""};
    this.arguments = "";
    this.help = "";
    this.ownerCommand = false;
  }

  /**
   * This method also does something. Mysterious.
   *
   * @param ce object containing information about the command event
   */
  @Override
  protected void execute(CommandEvent ce) {
    Settings.deleteInvoke(ce);

    // Parse raw message for arguments (if necessary)
    String[] arguments = ce.getMessage().getContentRaw().split("\\s");
    int numberOfArguments = arguments.length;
  }
}
