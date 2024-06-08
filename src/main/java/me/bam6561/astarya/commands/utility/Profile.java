package me.bam6561.astarya.commands.utility;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import me.bam6561.astarya.commands.owner.Settings;
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
 * @version 1.8.15
 * @since 1.6.3
 */
public class Profile extends Command {
  /**
   * Associates the command with its properties.
   */
  public Profile() {
    this.name = "profile";
    this.aliases = new String[]{"profile", "whois", "user"};
    this.arguments = "[0]Self [1]Mention/UserId/<@UserId> [1+]Name/Nickname";
    this.help = "Sends information about a user.";
  }

  /**
   * Sets the target for the command request.
   * <p>
   * No parameters provided default to the user.
   * <p>
   * Targets can be set by providing a mention, user id, nickname or name.
   *
   * @param ce command event
   */
  @Override
  protected void execute(CommandEvent ce) {
    Settings.deleteInvoke(ce);

    ProfileRequest request = new ProfileRequest(ce);

    String parameters = ce.getArgs();
    if (parameters.isBlank()) { // Target: Self
      request.sendProfileEmbed(ce.getMember(), ce.getMember().getUser());
    } else {
      List<Member> mentions = ce.getMessage().getMentions().getMembers();
      if (!mentions.isEmpty()) { // Target: Mention
        Member member = mentions.get(0);
        request.sendProfileEmbed(member, member.getUser());
      } else { // Target: User Id, Nickname, or Name
        request.interpretUserIdOrName(parameters);
      }
    }
  }

  /**
   * Represents a profile query.
   *
   * @param ce command event
   * @author Danny Nguyen
   * @version 1.8.15
   * @since 1.8.15
   */
  private record ProfileRequest(CommandEvent ce) {
    /**
     * Sends an embed containing information about a user
     * and adds additional details if they're in the guild.
     * <p>
     * If the user is in the guild, the following details are added:
     * <ul>
     *  <li> Online Status
     *  <li> Activity
     *  <li> Mention
     *  <li> Joined
     *  <li> Boosted
     *  <li> Timed Out
     *  <li> Avatar: Server
     *  <li> Roles
     * </ul>
     *
     * @param member the guild member
     * @param user   the Discord user
     */
    private void sendProfileEmbed(Member member, User user) {
      EmbedBuilder embed = new EmbedBuilder();
      boolean isGuildMember = member != null;

      buildProfileEmbed(embed, member, user, isGuildMember);
      Settings.sendEmbed(ce, embed);
    }

    /**
     * Sets the target for the command by either a user id, nickname, or name.
     *
     * @param parameters user provided parameters
     */
    private void interpretUserIdOrName(String parameters) {
      try { // Target: User Id
        User user = ce.getJDA().retrieveUserById(parameters).complete();
        Member member = ce.getGuild().getMemberById(user.getId());
        sendProfileEmbed(member, user);
      } catch (NumberFormatException | ErrorResponseException invalidUserId) {
        // Attempt to match a member by nickname or name
        List<Member> members = ce.getGuild().getMembersByNickname(parameters, true);
        if (members.isEmpty()) {
          members = ce.getGuild().getMembersByName(parameters, true);
        }
        if (!members.isEmpty()) { // Target: Nickname or Name
          sendProfileEmbed(members.get(0), members.get(0).getUser());
        } else {
          try { // Target: <@UserId>
            User user = ce.getJDA().retrieveUserById(parameters.substring(2, parameters.length() - 1)).complete();
            sendProfileEmbed(null, user);
          } catch (NumberFormatException | ErrorResponseException invalidUserId2) {
            ce.getTextChannel().sendMessage("User not found.").queue();
          }
        }
      }
    }

    /**
     * Builds the profile embed.
     *
     * @param embed         the embed builder
     * @param member        the guild member
     * @param user          the Discord user
     * @param isGuildMember if the user is in the same guild
     */
    private void buildProfileEmbed(EmbedBuilder embed, Member member, User user, boolean isGuildMember) {
      DateTimeFormatter dtf = DateTimeFormatter.ofPattern("M/d/yy hh:mm");
      DateTimeFormatter dtf2 = DateTimeFormatter.ofPattern("M/d hh:mm");

      // User Tag, Profile Picture
      embed.setAuthor("Profile");
      embed.setTitle(user.getAsTag());
      embed.setThumbnail(isGuildMember ? member.getEffectiveAvatarUrl() + "?size=1024" : user.getEffectiveAvatarUrl() + "?size=1024");

      // Status, Activity, Mention
      if (isGuildMember) {
        embed.appendDescription(getOnlineStatusAsEmoji(member) + "\n");
        if (!member.getActivities().isEmpty()) {
          embed.appendDescription(member.getActivities().get(0).getName() + "\n");
        }
        embed.appendDescription("**Mention:** " + member.getAsMention() + "\n");
      }

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
      String userBannerUrl = user.retrieveProfile().complete().getBannerUrl();
      if (userBannerUrl != null) {
        embed.addField("Banner", "[Global](" + userBannerUrl + "?size=2048)", false);
      }

      // Roles
      if ((isGuildMember) && (!member.getRoles().isEmpty())) {
        embed.addField("Roles", getRolesAsMentions(member), false);
      }
    }

    /**
     * Gets the member's online status as an emoji.
     *
     * @param member the guild member
     * @return text containing an online status's associated emoji and name
     */
    private String getOnlineStatusAsEmoji(Member member) {
      switch (member.getOnlineStatus()) {
        case ONLINE -> {
          return Emoji.fromUnicode("U+1F7E2").getFormatted() + " Online";
        }
        case IDLE -> {
          return Emoji.fromUnicode("U+1F7E0").getFormatted() + " Idle";
        }
        case DO_NOT_DISTURB -> {
          return Emoji.fromUnicode("U+1F534").getFormatted() + " Do Not Disturb";
        }
        case OFFLINE -> {
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
  }
}
