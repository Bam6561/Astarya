package me.dannynguyen.astarya.commands.utility;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import me.dannynguyen.astarya.Bot;
import me.dannynguyen.astarya.commands.owner.Settings;
import me.dannynguyen.astarya.enums.BotMessage;
import me.dannynguyen.astarya.utility.TextReader;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;

import java.awt.*;
import java.util.Set;

/**
 * ColorRole is a command invocation that assigns or removes color roles from the user.
 *
 * @author Danny Nguyen
 * @version 1.8.1
 * @since 1.7.4
 */
public class ColorRole extends Command {
  private final Set<String> colorRoles;

  public ColorRole(Set<String> colorRoles) {
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
      ce.getChannel().sendMessage(Failure.PROVIDE_HEX_CODE.text).queue();
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
          } else {
            ce.getChannel().sendMessage(Failure.SERVER_OWNER_ONLY.text).queue();
          }
        }
        case "clear" -> {
          removeColorRoles(ce);
          ce.getChannel().sendMessage(Success.CLEAR_ROLES.text).queue();
        }
        default -> {
          parameter = parameter.toUpperCase();
          if (TextReader.isHexColorCode(parameter)) {
            assignColorRole(ce, parameter);
          } else {
            ce.getChannel().sendMessage(Failure.INVALID_HEX_CODE.text).queue();
          }
        }
      }
    } catch (InsufficientPermissionException ex) {
      ce.getChannel().sendMessage(BotMessage.MISSING_PERMISSION_MANAGE_ROLES.getMessage()).queue();
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
   */
  private void reloadColorRoles(CommandEvent ce) {
    for (Role role : Bot.getApi().getRoles()) {
      String roleName = role.getName();

      // Hex Color Code Format: #ffffff
      if (TextReader.isHexColorCode(roleName.toUpperCase())) {
        if (!Bot.getApi().getMutualGuilds().get(0).getMembersWithRoles(role).isEmpty()) {
          colorRoles.add(roleName);
        } else {
          role.delete().queue();
        }
      }
    }
    ce.getChannel().sendMessage(Success.CLEANED_UP_ROLES.text).queue();
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

      if (TextReader.isHexColorCode(roleName)) {
        guild.removeRoleFromMember(member, role).queue();
      }
    }
  }

  private enum Success {
    CLEAR_ROLES("Cleared color roles."),
    CLEANED_UP_ROLES("Cleaned up empty color roles.");

    public final String text;

    Success(String text) {
      this.text = text;
    }
  }

  private enum Failure {
    PROVIDE_HEX_CODE("Provide a hex color code `#ffffff` or `clear`."),
    INVALID_HEX_CODE("Invalid color code."),
    SERVER_OWNER_ONLY("Server owner only command.");

    public final String text;

    Failure(String text) {
      this.text = text;
    }
  }
}
