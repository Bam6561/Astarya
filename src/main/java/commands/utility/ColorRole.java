package commands.utility;

import astarya.Astarya;
import astarya.Text;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import commands.owner.Settings;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;

import java.awt.*;
import java.util.HashSet;

/**
 * ColorRole is a command invocation that assigns or removes color roles from the user.
 *
 * @author Danny Nguyen
 * @version 1.7.10
 * @since 1.7.4
 */
public class ColorRole extends Command {
  private final HashSet<String> colorRoles;

  public ColorRole(HashSet<String> colorRoles) {
    this.name = "color";
    this.aliases = new String[]{"color"};
    this.arguments = "[1]<#HexColor>";
    this.help = "Assigns a colored role to the user.";
    this.colorRoles = colorRoles;
  }

  /**
   * Checks if user provided a parameter to interpret a color roles command request.
   *
   * @param ce command event
   */
  @Override
  protected void execute(CommandEvent ce) {
    Settings.deleteInvoke(ce);

    String[] parameters = ce.getMessage().getContentRaw().split("\\s");
    int numberOfParameters = parameters.length - 1;

    if (numberOfParameters == 1) {
      interpretColorRoleRequest(ce, parameters[1].toLowerCase());
    } else {
      ce.getChannel().sendMessage("Provide a hex color code `#ffffff` or `clear`.").queue();
    }
  }

  /**
   * Either:
   * - cleans up empty color roles
   * - assigns a color role
   * - clears existing color roles
   *
   * @param ce        command event
   * @param parameter user provided parameter
   * @throws InsufficientPermissionException unable to manage roles
   */
  private void interpretColorRoleRequest(CommandEvent ce, String parameter) {
    try {
      switch (parameter) {
        case "clean" -> {
          if (ce.getMember().isOwner()) {
            reloadColorRoles(ce);
          }
        }
        case "clear" -> {
          removeColorRoles(ce);
          ce.getChannel().sendMessage("Cleared all color roles.").queue();
        }
        default -> {
          parameter = parameter.toUpperCase();
          if (isHexColorCode(parameter)) {
            assignColorRole(ce, parameter);
          } else {
            ce.getChannel().sendMessage("Invalid color code.").queue();
          }
        }
      }
    } catch (InsufficientPermissionException ex) {
      ce.getChannel().sendMessage(Text.MISSING_MANAGE_ROLES_PERMISSION.value()).queue();
    }
  }

  /**
   * Clears existing color roles and assigns a new color role to the user.
   *
   * @param ce        command event
   * @param colorCode color code (hex)
   */
  private void assignColorRole(CommandEvent ce, String colorCode) {
    removeColorRoles(ce);

    Guild guild = ce.getGuild();
    if (!colorRoles.contains(colorCode)) {
      colorRoles.add(colorCode);
      guild.createRole().setName(colorCode).setColor(Color.decode(colorCode)).complete();
    }
    guild.addRoleToMember(ce.getMember(), guild.getRolesByName(colorCode, true).get(0)).queue();
    ce.getChannel().sendMessage("Assigned color: `" + colorCode + "`.").queue();
  }

  /**
   * Reloads the server's color role names into memory and deletes empty color roles if they exist.
   *
   * @param ce command event
   * @return color role names
   */
  private HashSet<String> reloadColorRoles(CommandEvent ce) {
    HashSet<String> colorRoles = this.colorRoles;

    for (Role role : Astarya.getApi().getRoles()) {
      String roleName = role.getName();

      // Hex Color Code Format: #ffffff
      if (isHexColorCode(roleName.toUpperCase())) {
        if (!Astarya.getApi().getMutualGuilds().get(0).getMembersWithRoles(role).isEmpty()) {
          colorRoles.add(roleName);
        } else {
          role.delete().queue();
        }
      }
    }
    ce.getChannel().sendMessage("Cleaned up all empty color roles.").queue();
    return colorRoles;
  }

  /**
   * Removes all color roles from the user.
   *
   * @param ce command event
   */
  private void removeColorRoles(CommandEvent ce) {
    Guild guild = ce.getGuild();
    Member member = ce.getMember();

    for (Role role : member.getRoles()) {
      String roleName = role.getName();

      if (isHexColorCode(roleName)) {
        guild.removeRoleFromMember(member, role).queue();
      }
    }
  }

  /**
   * Determines if a string is a hex color code.
   *
   * @param parameter user provided parameter
   * @return is a hex color code
   */
  private boolean isHexColorCode(String parameter) {
    if (!parameter.startsWith("#") || parameter.length() != 7) {
      return false;
    }

    for (char c : parameter.substring(1).toCharArray()) {
      switch (c) {
        case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' -> {
        }
        default -> {
          return false;
        }
      }
    }
    return true;
  }
}
