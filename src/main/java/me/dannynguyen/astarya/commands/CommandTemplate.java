package me.dannynguyen.astarya.commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import me.dannynguyen.astarya.commands.owner.Settings;

/**
 * CommandTemplate does something. We just don't know what yet.
 *
 * @author Danny Nguyen
 * @version 1.6.11
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
   * @param ce command event
   */
  @Override
  protected void execute(CommandEvent ce) {
    Settings.deleteInvoke(ce);

    String[] parameters = ce.getMessage().getContentRaw().split("\\s");
    int numberOfParameters = parameters.length - 1;
  }
}
