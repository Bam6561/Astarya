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

public class Profile extends Command {
  public Profile() {
    this.name = "whois";
    this.aliases = new String[]{"profile", "whois", "who", "user"};
    this.arguments = "[0]Self [1]Mention/UserID";
    this.help = "Provides information on the user.";
  }

  // Sends an embed containing information about a Discord user
  @Override
  protected void execute(CommandEvent ce) {
    Settings.deleteInvoke(ce);

    // Parse message for arguments
    String[] arguments = ce.getMessage().getContentRaw().split("\\s");
    int numberOfArguments = arguments.length - 1;

    EmbedBuilder display = new EmbedBuilder();
    display.setTitle("__Profile__");

    switch (numberOfArguments) {
      case 0 -> setEmbedToDisplaySelf(ce, display, ce.getMember().getUser()); // Self
      case 1 -> setEmbedToDisplayMentionedUserOrProcessUserID(ce, display, arguments); // Mention or User ID provided
      default -> ce.getChannel().sendMessage("Invalid number of arguments.").queue();
    }
  }

  // Sends an embed containing information about the self user who invoked the command
  private void setEmbedToDisplaySelf(CommandEvent ce, EmbedBuilder display, User selfUser) {
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm");

    display.setThumbnail(selfUser.getAvatarUrl());
    display.setDescription("**Tag:** " + selfUser.getAsMention() + "\n**Discord:** `" + selfUser.getAsTag()
        + "`\n**User ID:** `" + selfUser.getId() + "`\n**Created:** `" + selfUser.getTimeCreated().format(dtf)
        + " GMT`\n**Joined:** `" + ce.getMember().getTimeJoined().format(dtf) + " GMT`");
    display.addField("**Roles:**", returnUserRolesAsMentionable(ce.getMember()), false);

    Settings.sendEmbed(ce, display);
  }

  // Determines whether provided information from the command's arguments was a mention or a user ID
  private void setEmbedToDisplayMentionedUserOrProcessUserID(CommandEvent ce, EmbedBuilder display, String[] arguments) {
    boolean mentionedAnyUsers = !ce.getMessage().getMentionedUsers().isEmpty();
    if (mentionedAnyUsers) { // Mention
      setEmbedToDisplayMentionedUser(ce, display, ce.getMessage().getMentionedUsers().get(0));
    } else { // UserID
      try {
        processUserIDBeforeSettingEmbed(ce, display, arguments);
      } catch (Exception e) {
        ce.getChannel().sendMessage("User ID not recognized.").queue();
      }
    }
  }

  // Sends an embed containing information about the mentioned user
  private void setEmbedToDisplayMentionedUser(CommandEvent ce, EmbedBuilder display, User mentionedUser) {
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm");

    display.setThumbnail(mentionedUser.getAvatarUrl());
    display.setDescription("**Tag:** " + mentionedUser.getAsMention()
        + "\n**Discord:** `" + mentionedUser.getAsTag()
        + "`\n**User ID:** `" + mentionedUser.getId()
        + "`\n**Created:** `" + mentionedUser.getTimeCreated().format(dtf) + " GMT`"
        + "\n**Joined:** `" + retrieveServerMemberInServer(ce, mentionedUser.getId()).getTimeJoined().format(dtf) + " GMT`");
    display.addField("**Roles:**", returnUserRolesAsMentionable(ce.getMessage().getMentionedMembers().get(0)), false);

    Settings.sendEmbed(ce, display);
  }

  /*
   Method is divided into two categories as users can provide the user
   ID by itself or make it mentionable by formatting it as <@UserID>.
   Note: Some user IDs are 19 characters long instead of 18,
   requiring the need for multiple similar switch cases.
   */
  private void processUserIDBeforeSettingEmbed(CommandEvent ce, EmbedBuilder display, String[] arguments) {
    switch (arguments[1].length()) {
      case 18, 19 -> { // User ID
        setEmbedToDisplayUserID(ce, display, arguments[1]);
      }

      case 21 -> { // <@UserID> (18 characters)
        setEmbedToDisplayUserID(ce, display, arguments[1].substring(2, 20));
      }
      case 22 -> { // <@UserID> (19 characters)
        setEmbedToDisplayUserID(ce, display, arguments[1].substring(2, 21));
      }

      default -> ce.getChannel().sendMessage("Invalid User ID input.").queue();
    }
  }

  // Retrieves user based on user ID provided and sets the embed content
  private void setEmbedToDisplayUserID(CommandEvent ce, EmbedBuilder display, String userID) {
    Member serverMember = null;
    boolean isServerMember = false;

    try { // Add user's roles to embed only if they are a server member
      serverMember = retrieveServerMemberInServer(ce, userID);
      isServerMember = true;
      display.addField("**Roles:**", returnUserRolesAsMentionable(serverMember), false);
    } catch (ErrorResponseException notAMemberOfServer) {
    }

    User user = ce.getJDA().retrieveUserById(userID).complete();
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm");

    display.setThumbnail(user.getAvatarUrl());
    display.setDescription("**Tag:** " + user.getAsMention() + "\n**Discord:** `" + user.getAsTag()
        + "`\n**User ID:** `" + user.getId() + "`\n**Created:** `" + user.getTimeCreated().format(dtf) + " GMT`"
        + (isServerMember ? "\n**Joined:** `" + serverMember.getTimeJoined().format(dtf) + " GMT`" : ""));

    Settings.sendEmbed(ce, display);
  }

  // Returns user object if they are a member of the server as the command invoked
  private Member retrieveServerMemberInServer(CommandEvent ce, String userID) {
    return ce.getGuild().retrieveMemberById(userID).complete();
  }

  // Retrieve role IDs from user as strings, then make the roles mentionable
  private String returnUserRolesAsMentionable(Member member) {
    StringBuilder roleList = new StringBuilder();
    for (Role roleLongID : member.getRoles()) {
      String roleID = roleLongID.toString();
      roleList.append("<@&").append(roleID, roleID.length() - 19, roleID.length() - 1).append("> ");
    }
    return roleList.toString();
  }
}
