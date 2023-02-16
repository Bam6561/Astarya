package commands.utility;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import commands.owner.Settings;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;

public class Avatar extends Command {
  public Avatar() {
    this.name = "avatar";
    this.aliases = new String[]{"avatar", "pfp"};
    this.arguments = "[0]Self [1]Mention/UserID/Size [2]Size";
    this.help = "Provides the user's profile picture.";
  }

  // Sends an embed containing mentioned user's profile picture
  @Override
  protected void execute(CommandEvent ce) {
    Settings.deleteInvoke(ce);

    // Parse message for arguments
    String[] arguments = ce.getMessage().getContentRaw().split("\\s");
    int numberOfArguments = arguments.length;

    String avatarSize = "1024"; // Choice between 128, 256, 512, 1024
    EmbedBuilder display = new EmbedBuilder();

    switch (numberOfArguments) {
      case 1 -> { // Self
        setEmbedToSelf(ce, display, avatarSize);
      }

      case 2 -> { // Mention || UserID || Self & Size
        boolean mentionedUser = !ce.getMessage().getMentionedMembers().isEmpty();
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
        boolean mentionedUser = !ce.getMessage().getMentionedMembers().isEmpty();
        boolean validUserID = (arguments[1].length() == 18) || (arguments[1].length() == 19);
        if (mentionedUser) {
          setEmbedToMentionAndSize(ce, display, arguments, avatarSize);
        } else if (validUserID) {
          setEmbedToUserIDAndSize(ce, display, arguments, avatarSize);
        } else {
          ce.getChannel().sendMessage("Invalid User ID.").queue();
        }
      }

      default -> ce.getChannel().sendMessage("Invalid number of arguments.").queue(); // Invalid arguments
    }
  }

  // Set embed to self user's profile picture
  private void setEmbedToSelf(CommandEvent ce, EmbedBuilder display, String avatarSize) {
    User self = ce.getMember().getUser();
    sendEmbed(ce, display, self.getName(), "Resolution: " + avatarSize + "x" + avatarSize,
        self.getAvatarUrl() + "?size=" + avatarSize);
  }

  // Set embed to mentioned user's profile picture
  private void setEmbedToMentionedUser(CommandEvent ce, EmbedBuilder display, String avatarSize) {
    Member member = ce.getMessage().getMentionedMembers().get(0);
    sendEmbed(ce, display, member.getEffectiveName(), "Resolution: " + avatarSize + "x" + avatarSize,
        member.getUser().getAvatarUrl() + "?size=" + avatarSize);
  }

  // Set embed to user ID's profile picture
  private void setEmbedToUserID(CommandEvent ce, EmbedBuilder display, String[] arguments, String avatarSize) {
    User user = ce.getJDA().retrieveUserById(arguments[1]).complete();
    sendEmbed(ce, display, user.getName(), "Resolution: " + avatarSize + "x" + avatarSize,
        user.getAvatarUrl() + "?size=" + avatarSize);
  }

  // Set embed to self user and change avatar size
  private void setEmbedToSelfAndSize(CommandEvent ce, EmbedBuilder display, String[] arguments, String avatarSize) {
    User user = ce.getMember().getUser();
    sendEmbed(ce, display, user.getName(),
        user.getAvatarUrl() + "?size=" + setImageSize(ce, arguments, 1, avatarSize, display));
  }

  // Set embed to mentioned user and change avatar size
  private void setEmbedToMentionAndSize(CommandEvent ce, EmbedBuilder display, String[] arguments, String avatarSize) {
    Member member = ce.getMessage().getMentionedMembers().get(0);
    sendEmbed(ce, display, member.getEffectiveName(),
        member.getUser().getAvatarUrl() + "?size=" + setImageSize(ce, arguments, 2, avatarSize, display));
  }

  // Set embed to user ID and change avatar size
  private void setEmbedToUserIDAndSize(CommandEvent ce, EmbedBuilder display, String[] arguments, String avatarSize) {
    try { // Verify UserID
      User user = ce.getJDA().retrieveUserById(arguments[1]).complete();
      sendEmbed(ce, display, user.getName(),
          user.getAvatarUrl() + "?size=" + setImageSize(ce, arguments, 2, avatarSize, display));
    } catch (ErrorResponseException error) { // Invalid UserID
      ce.getChannel().sendMessage("Invalid User ID.").queue();
    }
  }

  // Embed without avatar size provided
  private void sendEmbed(CommandEvent ce, EmbedBuilder display, String title, String description, String image) {
    display.setTitle(title);
    display.setDescription(description);
    display.setImage(image);

    Settings.sendEmbed(ce, display);
  }

  // Embed with avatar size provided
  private void sendEmbed(CommandEvent ce, EmbedBuilder display, String title, String image) {
    display.setTitle(title);
    display.setImage(image);

    Settings.sendEmbed(ce, display);
  }

  // Changes default avatar size
  private String setImageSize(CommandEvent ce, String[] args, int argument, String avatarSize, EmbedBuilder display) {
    switch (args[argument]) {
      case "128", "256", "512", "1024" -> avatarSize = args[argument];
      default -> ce.getChannel().sendMessage("Image only come in square sizes of 128, 256, 512, & 1024.").queue();
    }
    display.setDescription("Resolution: " + avatarSize + "x" + avatarSize);
    return avatarSize;
  }
}