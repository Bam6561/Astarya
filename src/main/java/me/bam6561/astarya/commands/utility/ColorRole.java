package me.bam6561.astarya.commands.utility;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import me.bam6561.astarya.Bot;
import me.bam6561.astarya.commands.owner.Settings;
import me.bam6561.astarya.enums.BotMessage;
import me.bam6561.astarya.utils.TextReader;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.Objects;
import java.util.Set;

/**
 * Command invocation that assigns or removes color roles from the user.
 *
 * @author Danny Nguyen
 * @version 1.8.15
 * @since 1.7.4
 */
public class ColorRole extends Command {
  /**
   * Managed color roles.
   */
  private final Set<String> colorRoles;

  /**
   * Associates the command with its properties.
   *
   * @param colorRoles color roles
   */
  public ColorRole(@NotNull Set<String> colorRoles) {
    this.colorRoles = Objects.requireNonNull(colorRoles, "Null color roles");
    this.name = "color";
    this.aliases = new String[]{"color"};
    this.arguments = "[1]<#HexColor>";
    this.help = "Assigns a colored role to the user.";
  }

  /**
   * Checks if user provided a parameter to interpret the command request.
   *
   * @param ce command event
   */
  @Override
  protected void execute(CommandEvent ce) {
    Settings.deleteInvoke(ce);

    String[] parameters = ce.getMessage().getContentRaw().split("\\s");
    int numberOfParameters = parameters.length - 1;

    if (numberOfParameters == 1) {
      new ColorRequest(ce).interpretRequest(parameters[1].toLowerCase());
    } else {
      ce.getChannel().sendMessage("Provide a hex color code `#ffffff` or `clear`.").queue();
    }
  }

  /**
   * Represents a color roles query.
   *
   * @author Danny Nguyen
   * @version 1.8.15
   * @since 1.8.15
   */
  private class ColorRequest {
    /**
     * Command event.
     */
    private final CommandEvent ce;

    /**
     * Associates the color request with its command event.
     *
     * @param ce command event
     */
    ColorRequest(CommandEvent ce) {
      this.ce = ce;
    }

    /**
     * Either:
     * <ul>
     *  <li> cleans up empty color roles
     *  <li> assigns a color role
     *  <li> clears existing color roles
     * </ul>
     *
     * @param parameter user provided parameter
     */
    private void interpretRequest(String parameter) {
      try {
        switch (parameter) {
          case "clean" -> {
            if (ce.getMember().isOwner()) {
              reloadColorRoles();
            } else {
              ce.getChannel().sendMessage("Server owner only command.").queue();
            }
          }
          case "clear" -> {
            removeColorRoles();
            ce.getChannel().sendMessage("Cleared color roles.").queue();
          }
          default -> {
            parameter = parameter.toUpperCase();
            if (TextReader.isHexColorCode(parameter)) {
              assignColorRole(parameter);
            } else {
              ce.getChannel().sendMessage("Invalid color code.").queue();
            }
          }
        }
      } catch (InsufficientPermissionException ex) {
        ce.getChannel().sendMessage(BotMessage.MISSING_PERMISSION_MANAGE_ROLES.getMessage()).queue();
      }
    }

    /**
     * Reloads the server's color role names into memory and deletes empty color roles if they exist.
     */
    private void reloadColorRoles() {
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
      ce.getChannel().sendMessage("Cleaned up empty color roles.").queue();
    }

    /**
     * Removes all color roles from the user.
     */
    private void removeColorRoles() {
      Guild guild = ce.getGuild();
      Member member = ce.getMember();

      for (Role role : member.getRoles()) {
        String roleName = role.getName();

        if (TextReader.isHexColorCode(roleName)) {
          guild.removeRoleFromMember(member, role).queue();
        }
      }
    }

    /**
     * Clears existing color roles and assigns a new color role to the user.
     *
     * @param colorCode color code (hex)
     */
    private void assignColorRole(String colorCode) {
      removeColorRoles();

      Guild guild = ce.getGuild();
      if (!colorRoles.contains(colorCode)) {
        colorRoles.add(colorCode);
        guild.createRole().setName(colorCode).setColor(Color.decode(colorCode)).complete();
      }
      guild.addRoleToMember(ce.getMember(), guild.getRolesByName(colorCode, true).get(0)).queue();
      ce.getChannel().sendMessage("Assigned color: `" + colorCode + "`.").queue();
    }
  }
}
