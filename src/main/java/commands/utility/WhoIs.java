package commands.utility;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import commands.owner.Settings;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;

import java.time.format.DateTimeFormatter;

public class WhoIs extends Command {
  public WhoIs() {
    this.name = "whois";
    this.aliases = new String[]{"whois", "who", "profile", "user"};
    this.arguments = "[0]Self [1]Mention/UserID";
    this.help = "Provides information on the user.";
  }

  @Override
  protected void execute(CommandEvent ce) {
    Settings.deleteInvoke(ce);
    String[] args = ce.getMessage().getContentRaw().split("\\s"); // Parse message for arguments
    int arguments = args.length;
    EmbedBuilder display = new EmbedBuilder();
    display.setTitle("__Profile__");
    switch (arguments) {
      case 1 -> { // Self
        User user = ce.getMember().getUser();
        selfProfile(ce, display, user);
      }
      case 2 -> { // Mention or userId
        if (!ce.getMessage().getMentionedUsers().isEmpty()) { // Mention
          User user = ce.getMessage().getMentionedUsers().get(0);
          mentionProfile(ce, display, user);
        } else { // UserID
          try {
            switch (args[1].length()) {
              case 18 -> { // UserID
                String userID = args[1];
                User user = ce.getJDA().retrieveUserById(userID).complete();
                userIDProfile(ce, display, user, userID);
              }
              case 22 -> { // Mention by UserId
                String userID = args[1].substring(3, 21);
                User user = ce.getJDA().retrieveUserById(userID).complete();
                userIDProfile(ce, display, user, userID);
              }
              default -> ce.getChannel().sendMessage("Invalid User ID input.").queue();
            }
          } catch (Exception e) {
            ce.getChannel().sendMessage("User ID not recognized.").queue();
          }
        }
      }
      // Invalid arguments
      default -> ce.getChannel().sendMessage("Invalid number of arguments.").queue();
    }
  }

  private void selfProfile(CommandEvent ce, EmbedBuilder display, User user) {
    display.setThumbnail(user.getAvatarUrl());
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm");
    display.setDescription("**Tag:** " + user.getAsMention() + "\n**Discord:** `" + user.getAsTag()
        + "`\n**User ID:** `" + user.getId() + "`\n**Created:** `" + user.getTimeCreated().format(dtf) + " GMT`"
        + "\n**Joined:** `" + ce.getMember().getTimeJoined().format(dtf) + " GMT`");
    display.addField("**Roles:**", parseRoleList(ce.getMember()), false);
    Settings.sendEmbed(ce, display);
  }

  // Lookup by mention
  private void mentionProfile(CommandEvent ce, EmbedBuilder display, User user) {
    display.setThumbnail(user.getAvatarUrl());
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm");
    display.setDescription("**Tag:** " + user.getAsMention() + "\n**Discord:** `" + user.getAsTag()
        + "`\n**User ID:** `" + user.getId() + "`\n**Created:** `" + user.getTimeCreated().format(dtf) + " GMT`"
        + "\n**Joined:** `" + getMemberByID(ce, user.getId()).getTimeJoined().format(dtf) + " GMT`");
    display.addField("**Roles:**", parseRoleList(ce.getMessage().getMentionedMembers().get(0)), false);
    Settings.sendEmbed(ce, display);
  }

  // Lookup by User ID
  private void userIDProfile(CommandEvent ce, EmbedBuilder display, User user, String userID) {
    display.setThumbnail(user.getAvatarUrl());
    Member member = null;
    boolean idInGuild = false;
    try { // Only true is user exists in guild
      member = getMemberByID(ce, userID);
      idInGuild = true;
    } catch (ErrorResponseException ignored) {
    }
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm");
    display.setDescription("**Tag:** " + user.getAsMention() + "\n**Discord:** `" + user.getAsTag()
        + "`\n**User ID:** `" + user.getId() + "`\n**Created:** `" + user.getTimeCreated().format(dtf) + " GMT`"
        + (idInGuild ? "\n**Joined:** `" + member.getTimeJoined().format(dtf) + " GMT`" : ""));
    if (idInGuild) { // User exists in the guild to parse role list
      display.addField("**Roles:**", parseRoleList(member), false);
    }
    Settings.sendEmbed(ce, display);
  }

  private Member getMemberByID(CommandEvent ce, String userID) {
    return ce.getGuild().retrieveMemberById(userID).complete();
  }

  // Loop, extract ID, remove excess data, convert to mentionable
  private String parseRoleList(Member member) {
    StringBuilder roleList = new StringBuilder();
    for (Role roleLongID : member.getRoles()) {
      String roleID = roleLongID.toString();
      roleList.append("<@&").append(roleID, roleID.length() - 19, roleID.length() - 1).append("> ");
    }
    return roleList.toString();
  }
}
