package commands.utility;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import commands.owner.Settings;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Mentions;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Profile sends an embed containing information about a user
 * and adds additional details if they're a mutual guild member.
 *
 * @author Danny Nguyen
 * @version 1.6.3
 * @since 1.0
 */
public class Profile extends Command {
  public Profile() {
    this.name = "profile";
    this.aliases = new String[]{"profile", "whois", "user"};
    this.arguments = "[0]Self [1]Mention/UserId [1+]Name/Nickname";
    this.help = "Provides information about a user.";
  }

  /**
   * Sets the target for the Profile command.
   * <p>
   * No parameters set the command invoker as the target.
   * Parameters can target a user by: mention, user id, name, or nickname.
   * </p>
   *
   * @param ce the command event
   */
  @Override
  protected void execute(CommandEvent ce) {
    Settings.deleteInvoke(ce);

    if (ce.getArgs().isBlank()) { // Target: Self
      sendProfileEmbed(ce, ce.getMember(), ce.getMember().getUser());
    } else {
      Mentions mentions = ce.getMessage().getMentions();
      if (!mentions.getMembers().isEmpty()) { // Target: Mention
        Member member = mentions.getMembers().get(0);
        sendProfileEmbed(ce, mentions.getMembers().get(0), member.getUser());
      } else { // Target: User Id | Nickname | Name
        useUserIdOrNameAsTarget(ce);
      }
    }
  }

  /**
   * Sets the target for the Profile command by either a user id, nickname, or name.
   *
   * @param ce the command event
   * @throws NumberFormatException  parameter not user id
   * @throws ErrorResponseException invalid user id
   */
  private void useUserIdOrNameAsTarget(CommandEvent ce) {
    String parameters = ce.getArgs();
    try { // Target: User Id
      User user = ce.getJDA().retrieveUserById(parameters).complete();
      Member member = ce.getGuild().getMemberById(user.getId());
      sendProfileEmbed(ce, member, user);
    } catch (NumberFormatException | ErrorResponseException invalidUserId) {
      // Attempt to match a member by nickname or name
      List<Member> members;
      members = ce.getGuild().getMembersByNickname(parameters, true);
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
          ce.getTextChannel().sendMessage("User not found.").queue();
        }
      }
    }
  }

  /**
   * Sends an embed containing information about a user and
   * adds additional details if they're a mutual guild member.
   * <p>
   * If the user is a mutual guild member, the following details are added:
   * Online Status, Activity, Mention, Joined, Boosted, Timed Out, Avatar: Server, & Roles.
   * </p>
   *
   * @param ce     the command event
   * @param member the guild member
   * @param user   the Discord user
   */
  private void sendProfileEmbed(CommandEvent ce, Member member, User user) {
    EmbedBuilder embed = new EmbedBuilder();
    boolean isGuildMember = member != null;

    // Embed Header
    embed.setAuthor("Profile");
    embed.setTitle(user.getAsTag());
    embed.setThumbnail(isGuildMember ?
        member.getEffectiveAvatarUrl() + "?size=1024" : user.getEffectiveAvatarUrl() + "?size=1024");

    // Online Status, Activity, Mention
    if (isGuildMember) {
      embed.appendDescription(getOnlineStatusAsEmoji(member) + "\n");
      if (!member.getActivities().isEmpty()) {
        embed.appendDescription(member.getActivities().get(0).getName() + "\n");
      }
      embed.appendDescription("**Mention:** " + member.getAsMention() + "\n");
    }

    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("M/d/yy hh:mm");
    DateTimeFormatter dtf2 = DateTimeFormatter.ofPattern("M/d hh:mm");

    // Id, Created
    embed.appendDescription("**Id:** `" + user.getId() + "`\n");
    embed.appendDescription("**Created:** `" + user.getTimeCreated().format(dtf) + "`\n");

    // Joined, Boosted, Timed Out
    if (isGuildMember) {
      embed.appendDescription("**Joined:** `" + member.getTimeJoined().format(dtf) + "`\n");
      if (member.isBoosting()) {
        embed.appendDescription("**Boosted:** `" + member.getTimeBoosted().format(dtf) + "`\n");
      }
      if (member.isTimedOut()) {
        embed.appendDescription("**Timed Out:** `" + member.getTimeOutEnd().format(dtf2) + "`\n");
      }
    }

    // Avatar: Global | Server
    embed.addField("Avatar", "[Global](" + user.getEffectiveAvatarUrl() + "?size=1024) " +
        (isGuildMember ? "| [Server](" + member.getEffectiveAvatarUrl() + "?size=1024)" : ""), false);

    // Banner: Global
    String userBannerURL = user.retrieveProfile().complete().getBannerUrl();
    if (userBannerURL != null) {
      embed.addField("Banner", "[Global](" + userBannerURL + "?size=2048)", false);
    }

    // Roles
    if ((isGuildMember) && (!member.getRoles().isEmpty()))
      embed.addField("Roles", getRolesAsMentions(member), false);

    Settings.sendEmbed(ce, embed);
  }

  /**
   * @param member the guild member
   * @return String containing an online status's associated emoji and name
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
   * @param member the guild member
   * @return String containing the member's roles as mentions
   */
  private String getRolesAsMentions(Member member) {
    List<Role> roles = member.getRoles();
    StringBuilder rolesAsMentions = new StringBuilder();
    int i = 0;
    while (i != roles.size()) {
      rolesAsMentions.append(roles.get(i).getAsMention()).append(" ");
      i++;
    }
    return rolesAsMentions.toString().trim();
  }
}
