package commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import commands.owner.Settings;

/**
 * CommandTemplate does something. We just don't know what yet.
 *
 * @author Danny Nguyen
 * @version 1.6.3
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
   * @param ce the command event
   */
  @Override
  protected void execute(CommandEvent ce) {
    Settings.deleteInvoke(ce);
  }
}
