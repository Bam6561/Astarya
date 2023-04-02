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
import java.util.ArrayList;

/**
 * Profile is a command invocation that provides information on the user.
 *
 * @author Danny Nguyen
 * @version 1.6.1
 * @since 1.0
 */
public class Profile extends Command {
  public Profile() {
    this.name = "whois";
    this.aliases = new String[]{"profile", "whois", "user"};
    this.arguments = "[0]Self [1]Mention/UserID";
    this.help = "Provides information on the user.";
  }

  /**
   * Processes user provided arguments to determine what type of profile command interaction is being
   * requested. Available options are to look up the self-user, mentioned user, or a user via their ID.
   *
   * @param ce object containing information about the command event
   */
  @Override
  protected void execute(CommandEvent ce) {
    Settings.deleteInvoke(ce);

    // Parse message for arguments
    String[] arguments = ce.getMessage().getContentRaw().split("\\s");
    int numberOfArguments = arguments.length - 1;

    EmbedBuilder display = new EmbedBuilder();
    display.setTitle("__Profile__");

    switch (numberOfArguments) {
      case 0 -> setEmbedToDisplaySelf(ce, display, ce.getMember().getUser());
      case 1 -> setEmbedToDisplayMentionedUserOrProcessUserID(ce, display, arguments);
      default -> ce.getChannel().sendMessage("Invalid number of arguments.").queue();
    }
  }

  /**
   * Sends an embed containing information about the self-user who invoked the command.
   *
   * @param ce       object containing information about the command event
   * @param display  object representing the embed
   * @param selfUser user object representing the user who invoked the command
   */
  private void setEmbedToDisplaySelf(CommandEvent ce, EmbedBuilder display, User selfUser) {
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm");
    display.setThumbnail(selfUser.getAvatarUrl());
    display.setDescription("**Tag:** " + selfUser.getAsMention() + "\n**Discord:** `" + selfUser.getAsTag()
        + "`\n**User ID:** `" + selfUser.getId() + "`\n**Created:** `" + selfUser.getTimeCreated().format(dtf)
        + " GMT`\n**Joined:** `" + ce.getMember().getTimeJoined().format(dtf) + " GMT`");
    display.addField("**Roles:**", returnUserRolesAsMentionable(ce.getMember()), false);
    Settings.sendEmbed(ce, display);
  }

  /**
   * Determines from user provided arguments whether the user intends
   * to look up a mentioned user or a user through their user ID.
   *
   * @param ce        object containing information about the command event
   * @param display   object representing the embed
   * @param arguments user provided arguments
   * @throws Exception unknown error
   */
  private void setEmbedToDisplayMentionedUserOrProcessUserID(CommandEvent ce, EmbedBuilder display, String[] arguments) {
    boolean mentionedAnyUsers = !ce.getMessage().getMentions().getUsers().isEmpty();
    if (mentionedAnyUsers) { // Mention
      setEmbedToDisplayMentionedUser(ce, display, ce.getMessage().getMentions().getUsers().get(0));
    } else { // UserID
      try {
        processUserIDBeforeSettingEmbed(ce, display, arguments);
      } catch (Exception e) {
        ce.getChannel().sendMessage("User ID not recognized.").queue();
      }
    }
  }

  /**
   * Sends an embed containing information about the mentioned user.
   *
   * @param ce            object containing information about the command event
   * @param display       object representing the embed
   * @param mentionedUser user object representing the mentioned user
   */
  private void setEmbedToDisplayMentionedUser(CommandEvent ce, EmbedBuilder display, User mentionedUser) {
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm");
    display.setThumbnail(mentionedUser.getAvatarUrl());
    display.setDescription("**Tag:** " + mentionedUser.getAsMention()
        + "\n**Discord:** `" + mentionedUser.getAsTag()
        + "`\n**User ID:** `" + mentionedUser.getId()
        + "`\n**Created:** `" + mentionedUser.getTimeCreated().format(dtf) + " GMT`"
        + "\n**Joined:** `" + retrieveServerMemberInServer(ce, mentionedUser.getId()).getTimeJoined().format(dtf) + " GMT`");
    display.addField("**Roles:**", returnUserRolesAsMentionable(ce.getMessage().getMentions().getMembers().get(0)), false);
    Settings.sendEmbed(ce, display);
  }

  /**
   * Processes the user ID provided to a uniform format for look up.
   * <p>
   * Method is divided into two categories as users can provide the user ID by
   * itself or make it mentionable by formatting it as <@UserID>. Some user IDs are
   * 19 characters long instead of 18, requiring the need for multiple similar switch cases.
   * </p>
   *
   * @param ce        object containing information about the command event
   * @param display   object representing the embed
   * @param arguments user provided arguments
   */
  private void processUserIDBeforeSettingEmbed(CommandEvent ce, EmbedBuilder display, String[] arguments) {
    switch (arguments[1].length()) {
      case 18, 19 -> // User ID
          setEmbedToDisplayUserID(ce, display, arguments[1]);
      case 21 -> // <@UserID> (18 characters)
          setEmbedToDisplayUserID(ce, display, arguments[1].substring(2, 20));
      case 22 -> // <@UserID> (19 characters)
          setEmbedToDisplayUserID(ce, display, arguments[1].substring(2, 21));
      default -> ce.getChannel().sendMessage("Invalid User ID input.").queue();
    }
  }

  /**
   * Sends an embed containing information about the user mentioned by user ID.
   *
   * @param ce      object containing information about the command event
   * @param display object representing the embed
   * @param userID  user ID to lookup
   * @throws ErrorResponseException not a member of the Discord server
   */
  private void setEmbedToDisplayUserID(CommandEvent ce, EmbedBuilder display, String userID) {
    Member serverMember = null;
    boolean isServerMember = false;

    try { // Add user's roles to embed only if they are a server member
      serverMember = retrieveServerMemberInServer(ce, userID);
      isServerMember = true;
      display.addField("**Roles:**", returnUserRolesAsMentionable(serverMember), false);
    } catch (ErrorResponseException e) {
    }

    User user = ce.getJDA().retrieveUserById(userID).complete();
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm");

    display.setThumbnail(user.getAvatarUrl());
    display.setDescription("**Tag:** " + user.getAsMention() + "\n**Discord:** `" + user.getAsTag()
        + "`\n**User ID:** `" + user.getId() + "`\n**Created:** `" + user.getTimeCreated().format(dtf) + " GMT`"
        + (isServerMember ? "\n**Joined:** `" + serverMember.getTimeJoined().format(dtf) + " GMT`" : ""));
    Settings.sendEmbed(ce, display);
  }

  /**
   * Determines whether the user is a member of the server as the command invoked.
   *
   * @param ce     object containing information about the command event
   * @param userID user ID to lookup
   * @return whether the user is a member of the Discord server
   */
  private Member retrieveServerMemberInServer(CommandEvent ce, String userID) {
    return ce.getGuild().retrieveMemberById(userID).complete();
  }

  /**
   * Retrieves roles from user, then combines all the role names into a String.
   *
   * @param member object representing member of the Discord server
   * @return list of role names belonging to the member
   */
  private String returnUserRolesAsMentionable(Member member) {
    ArrayList<Role> roleList = new ArrayList<>();
    roleList.addAll(member.getRoles());
    StringBuilder roleNames = new StringBuilder();
    int i = 0;
    while (i != roleList.size()-1){
      roleNames.append(roleList.get(i).getName()).append(", ");
      i++;
    }
    roleNames.append(roleList.get(roleList.size()-1).getName());
    return roleNames.toString();
  }
}
