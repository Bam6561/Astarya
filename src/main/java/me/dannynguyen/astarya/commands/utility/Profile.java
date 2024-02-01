package me.dannynguyen.astarya.commands.utility;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import me.dannynguyen.astarya.commands.owner.Settings;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Profile sends an embed containing information about a
 * user and adds additional details if they're in the guild.
 *
 * @author Danny Nguyen
 * @version 1.8.1
 * @since 1.6.3
 */
public class Profile extends Command {
  public Profile() {
    this.name = "profile";
    this.aliases = new String[]{"profile", "whois", "user"};
    this.arguments = "[0]Self [1]Mention/UserId/<@UserId> [1+]Name/Nickname";
    this.help = "Sends information about a user.";
  }

  /**
   * Sets the target for the profile command request.
   * <p>
   * No parameters provided default to the user.
   * Targets can be set by providing a mention, user id, nickname or name.
   * </p>
   *
   * @param ce command event
   */
  @Override
  protected void execute(CommandEvent ce) {
    Settings.deleteInvoke(ce);

    String parameters = ce.getArgs();
    if (parameters.isBlank()) { // Target: Self
      sendProfileEmbed(ce, ce.getMember(), ce.getMember().getUser());
    } else {
      List<Member> mentions = ce.getMessage().getMentions().getMembers();
      if (!mentions.isEmpty()) { // Target: Mention
        Member member = mentions.get(0);
        sendProfileEmbed(ce, member, member.getUser());
      } else { // Target: User Id, Nickname, or Name
        interpretUserIdOrName(ce, parameters);
      }
    }
  }

  /**
   * Sends an embed containing information about a user
   * and adds additional details if they're in the guild.
   * <p>
   * If the user is in the guild, the following details are added:
   * Online Status, Activity, Mention, Joined, Boosted, Timed Out, Avatar: Server, & Roles.
   * </p>
   *
   * @param ce     command event
   * @param member the guild member
   * @param user   the Discord user
   */
  private void sendProfileEmbed(CommandEvent ce, Member member, User user) {
    EmbedBuilder display = new EmbedBuilder();
    boolean isGuildMember = member != null;

    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("M/d/yy hh:mm");
    DateTimeFormatter dtf2 = DateTimeFormatter.ofPattern("M/d hh:mm");

    buildEmbed(display, member, user, isGuildMember, dtf, dtf2);
    Settings.sendEmbed(ce, display);
  }

  /**
   * Sets the target for the command by either a user id, nickname, or name.
   *
   * @param ce         command event
   * @param parameters user provided parameters
   * @throws NumberFormatException  parameter not user id
   * @throws ErrorResponseException invalid user id
   */
  private void interpretUserIdOrName(CommandEvent ce, String parameters) {
    try { // Target: User Id
      User user = ce.getJDA().retrieveUserById(parameters).complete();
      Member member = ce.getGuild().getMemberById(user.getId());
      sendProfileEmbed(ce, member, user);
    } catch (NumberFormatException | ErrorResponseException invalidUserId) {
      interpretNameOrNickname(ce, parameters);
    }
  }

  private void interpretNameOrNickname(CommandEvent ce, String parameters) {
    // Attempt to match a member by nickname or name
    List<Member> members = ce.getGuild().getMembersByNickname(parameters, true);
    if (members.isEmpty()) {
      members = ce.getGuild().getMembersByName(parameters, true);
    }
    if (!members.isEmpty()) { // Target: Nickname or Name
      sendProfileEmbed(ce, members.get(0), members.get(0).getUser());
    } else {
      try { // Target: <@UserId>
        User user = ce.getJDA().retrieveUserById(parameters.substring(2, parameters.length() - 1)).complete();
        sendProfileEmbed(ce, null, user);
      } catch (NumberFormatException | ErrorResponseException invalidUserId2) {
        ce.getTextChannel().sendMessage(Failure.USER_NOT_FOUND.text).queue();
      }
    }
  }

  /**
   * Builds the profile embed.
   *
   * @param display       the embed builder
   * @param member        the guild member
   * @param user          the Discord user
   * @param isGuildMember whether the user is in the same guild
   * @param dtf           M/d/yy hh:mm
   * @param dtf2          M/d hh:mm
   */
  private void buildEmbed(EmbedBuilder display, Member member, User user, boolean isGuildMember,
                          DateTimeFormatter dtf, DateTimeFormatter dtf2) {
    // User Tag, Profile Picture
    display.setAuthor("Profile");
    display.setTitle(user.getAsTag());
    display.setThumbnail(isGuildMember ?
        member.getEffectiveAvatarUrl() + "?size=1024" : user.getEffectiveAvatarUrl() + "?size=1024");

    // Status, Activity, Mention
    if (isGuildMember) {
      display.appendDescription(getOnlineStatusAsEmoji(member) + "\n");
      if (!member.getActivities().isEmpty()) {
        display.appendDescription(member.getActivities().get(0).getName() + "\n");
      }
      display.appendDescription("**Mention:** " + member.getAsMention() + "\n");
    }

    // Id, Created
    display.appendDescription("**Id:** `" + user.getId() + "`\n");
    display.appendDescription("**Created:** `" + user.getTimeCreated().format(dtf) + "`\n");

    // Joined, Boosted, Timed Out
    if (isGuildMember) {
      display.appendDescription("**Joined:** `" + member.getTimeJoined().format(dtf) + "`\n");
      if (member.isBoosting()) {
        display.appendDescription("**Boosted:** `" + member.getTimeBoosted().format(dtf) + "`\n");
      }
      if (member.isTimedOut()) {
        display.appendDescription("**Timed Out:** `" + member.getTimeOutEnd().format(dtf2) + "`\n");
      }
    }

    // Avatar: Global | Server
    display.addField("Avatar", "[Global](" + user.getEffectiveAvatarUrl() + "?size=1024) " +
        (isGuildMember ? "| [Server](" + member.getEffectiveAvatarUrl() + "?size=1024)" : ""), false);

    // Banner: Global
    String userBannerUrl = user.retrieveProfile().complete().getBannerUrl();
    if (userBannerUrl != null) {
      display.addField("Banner", "[Global](" + userBannerUrl + "?size=2048)", false);
    }

    // Roles
    if ((isGuildMember) && (!member.getRoles().isEmpty())) {
      display.addField("Roles", getRolesAsMentions(member), false);
    }
  }

  /**
   * Gets the member's online status as an emoji.
   *
   * @param member the guild member
   * @return text containing an online status's associated emoji and name
   */
  private String getOnlineStatusAsEmoji(Member member) {
    switch (member.getOnlineStatus().toString()) {
      case "ONLINE" -> {
        return Emoji.fromUnicode("U+1F7E2").getFormatted() + " Online";
      }
      case "IDLE" -> {
        return Emoji.fromUnicode("U+1F7E0").getFormatted() + " Idle";
      }
      case "DO_NOT_DISTURB" -> {
        return Emoji.fromUnicode("U+1F534").getFormatted() + " Do Not Disturb";
      }
      case "OFFLINE" -> {
        return Emoji.fromUnicode("U+25CF").getFormatted() + " Offline";
      }
      default -> {
        return Emoji.fromUnicode("U+2753").getFormatted() + " Unknown";
      }
    }
  }

  /**
   * Gets the member's roles as mentionables.
   *
   * @param member the guild member
   * @return text containing the member's roles as mentionables
   */
  private String getRolesAsMentions(Member member) {
    List<Role> roles = member.getRoles();
    StringBuilder rolesAsMentions = new StringBuilder();
    for (Role role : roles) {
      rolesAsMentions.append(role.getAsMention()).append(" ");
    }
    return rolesAsMentions.toString().trim();
  }

  private enum Failure {
    USER_NOT_FOUND("User not found.");

    public final String text;

    Failure(String text) {
      this.text = text;
    }
  }
}
