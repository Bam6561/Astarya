package commands.utility;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import commands.owner.Settings;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;

/**
 * Avatar is a command invocation that provides the user's profile picture.
 *
 * @author Danny Nguyen
 * @version 1.6
 * @since 1.0
 */
public class Avatar extends Command {
  public Avatar() {
    this.name = "avatar";
    this.aliases = new String[]{"avatar", "pfp"};
    this.arguments = "[0]Self [1]Mention/UserID/Size [2]Size";
    this.help = "Provides the user's profile picture.";
  }

  /**
   * Processes user provided arguments to determine whether the avatar
   * command request is for the self-user, mentioned user, or user ID.
   * <p>
   * Users can provide an additional argument after the primary avatar request that changes the avatar's size.
   * </p>
   *
   * @param ce object containing information about the command event
   */
  @Override
  protected void execute(CommandEvent ce) {
    Settings.deleteInvoke(ce);

    // Parse message for arguments
    String[] arguments = ce.getMessage().getContentRaw().split("\\s");
    int numberOfArguments = arguments.length;

    String avatarSize = "1024"; // Choice between 128, 256, 512, 1024
    EmbedBuilder display = new EmbedBuilder();

    switch (numberOfArguments) {
      case 1 -> // Self
          setEmbedToSelf(ce, display, avatarSize);

      case 2 -> { // Mention || UserID || Self & Size
        boolean mentionedUser = !ce.getMessage().getMentions().getMembers().isEmpty();
        boolean validUserID = (arguments[1].length() == 18) || (arguments[1].length() == 19);
        if (mentionedUser) {
          setEmbedToMentionedUser(ce, display, avatarSize);
        } else if (validUserID) {
          setEmbedToUserID(ce, display, arguments, avatarSize);
        } else {
          setEmbedToSelfAndSize(ce, display, arguments, avatarSize);
        }
      }

      case 3 -> { // Mention & Size || UserID & Size
        boolean mentionedUser = !ce.getMessage().getMentions().getMembers().isEmpty();
        boolean validUserID = (arguments[1].length() == 18) || (arguments[1].length() == 19);
        if (mentionedUser) {
          setEmbedToMentionAndSize(ce, display, arguments, avatarSize);
        } else if (validUserID) {
          setEmbedToUserIDAndSize(ce, display, arguments, avatarSize);
        } else {
          ce.getChannel().sendMessage("Invalid User ID.").queue();
        }
      }

      default -> ce.getChannel().sendMessage("Invalid number of arguments.").queue();
    }
  }

  /**
   * Sets embed to self-user's profile picture.
   *
   * @param ce         object containing information about the command event
   * @param display    object representing the embed
   * @param avatarSize requested size of the avatar image
   */
  private void setEmbedToSelf(CommandEvent ce, EmbedBuilder display, String avatarSize) {
    User self = ce.getMember().getUser();
    sendEmbed(ce, display, self.getName(), "Resolution: " + avatarSize + "x" + avatarSize,
        self.getAvatarUrl() + "?size=" + avatarSize);
  }

  /**
   * Sets embed to mentioned users profile picture.
   *
   * @param ce         object containing information about the command event
   * @param display    object representing the embed
   * @param avatarSize requested size of the avatar image
   */
  private void setEmbedToMentionedUser(CommandEvent ce, EmbedBuilder display, String avatarSize) {
    Member member = ce.getMessage().getMentions().getMembers().get(0);
    sendEmbed(ce, display, member.getEffectiveName(), "Resolution: " + avatarSize + "x" + avatarSize,
        member.getUser().getAvatarUrl() + "?size=" + avatarSize);
  }

  /**
   * Sets embed to identified user by user ID's profile picture.
   *
   * @param ce         object containing information about the command event
   * @param display    object representing the embed
   * @param avatarSize requested size of the avatar image
   */
  private void setEmbedToUserID(CommandEvent ce, EmbedBuilder display, String[] arguments, String avatarSize) {
    User user = ce.getJDA().retrieveUserById(arguments[1]).complete();
    sendEmbed(ce, display, user.getName(), "Resolution: " + avatarSize + "x" + avatarSize,
        user.getAvatarUrl() + "?size=" + avatarSize);
  }

  /**
   * Sets embed to self-user's profile picture and changes the avatar image size.
   *
   * @param ce         object containing information about the command event
   * @param display    object representing the embed
   * @param arguments  user provided arguments
   * @param avatarSize requested size of the avatar image
   */
  private void setEmbedToSelfAndSize(CommandEvent ce, EmbedBuilder display, String[] arguments, String avatarSize) {
    User user = ce.getMember().getUser();
    sendEmbed(ce, display, user.getName(),
        user.getAvatarUrl() + "?size=" + setAvatarImageSize(ce, arguments, 1, avatarSize, display));
  }

  /**
   * Sets embed to mentioned user's profile picture and changes the avatar image size.
   *
   * @param ce         object containing information about the command event
   * @param display    object representing the embed
   * @param arguments  user provided arguments
   * @param avatarSize requested size of the image
   */
  private void setEmbedToMentionAndSize(CommandEvent ce, EmbedBuilder display, String[] arguments, String avatarSize) {
    Member member = ce.getMessage().getMentions().getMembers().get(0);
    sendEmbed(ce, display, member.getEffectiveName(),
        member.getUser().getAvatarUrl() + "?size=" + setAvatarImageSize(ce, arguments, 2, avatarSize, display));
  }

  /**
   * Sets embed to identified user by user ID's profile picture and changes the avatar size.
   *
   * @param ce         object containing information about the command event
   * @param display    object representing the embed
   * @param arguments  user provided arguments
   * @param avatarSize requested size of the avatar image
   * @throws ErrorResponseException user ID does not exist
   */
  private void setEmbedToUserIDAndSize(CommandEvent ce, EmbedBuilder display, String[] arguments, String avatarSize) {
    try { // Verify UserID
      User user = ce.getJDA().retrieveUserById(arguments[1]).complete();
      sendEmbed(ce, display, user.getName(),
          user.getAvatarUrl() + "?size=" + setAvatarImageSize(ce, arguments, 2, avatarSize, display));
    } catch (ErrorResponseException e) {
      ce.getChannel().sendMessage("Invalid User ID.").queue();
    }
  }

  /**
   * Sends an embed with the default sized avatar (x1024).
   *
   * @param ce          object containing information about the command event
   * @param display     object representing the embed
   * @param title       user's name
   * @param description avatar size
   * @param avatarImage user's avatar image
   */
  private void sendEmbed(CommandEvent ce, EmbedBuilder display, String title, String description, String avatarImage) {
    display.setTitle(title);
    display.setDescription(description);
    display.setImage(avatarImage);
    Settings.sendEmbed(ce, display);
  }

  /**
   * Sends an embed with a custom avatar size provided (x128, x256, x512, x1024).
   *
   * @param ce          object containing information about the command event
   * @param display     object representing the embed
   * @param title       user's name
   * @param avatarImage user's avatar image
   */
  private void sendEmbed(CommandEvent ce, EmbedBuilder display, String title, String avatarImage) {
    display.setTitle(title);
    display.setImage(avatarImage);
    Settings.sendEmbed(ce, display);
  }

  /**
   * Changes the avatar image size if it is an accepted size. Otherwise, remain the default size (x1024).
   *
   * @param ce                          object containing information about the command event
   * @param arguments                   user provided arguments
   * @param requestedAvatarSizeLocation which user provided argument is the avatar size request
   * @param avatarSize                  requested size of the avatar
   * @param display                     object representing the embed
   * @return resolution of the avatar image
   */
  private String setAvatarImageSize(CommandEvent ce, String[] arguments, int requestedAvatarSizeLocation,
                                    String avatarSize, EmbedBuilder display) {
    switch (arguments[requestedAvatarSizeLocation]) {
      case "128", "256", "512", "1024" -> avatarSize = arguments[requestedAvatarSizeLocation];
      default -> ce.getChannel().sendMessage("Avatars only come in square sizes of 128, 256, 512, & 1024.").queue();
    }
    display.setDescription("Resolution: " + avatarSize + "x" + avatarSize);
    return avatarSize;
  }
}