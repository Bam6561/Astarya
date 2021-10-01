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

  @Override
  protected void execute(CommandEvent ce) {
    Settings.deleteInvoke(ce);
    String[] args = ce.getMessage().getContentRaw().split("\\s"); // Parse message for arguments
    int arguments = args.length;
    String avatarSize = "1024"; // Choice between 128, 256, 512, 1024
    EmbedBuilder display = new EmbedBuilder();
    switch (arguments) {
      case 1 -> { // Self
        User self = ce.getMember().getUser();
        sendEmbed(ce, display, self.getName(), "Resolution: " + avatarSize + "x" + avatarSize,
            self.getAvatarUrl() + "?size=" + avatarSize);
      }
      case 2 -> { // Mention || UserID || Self & Size
        if (!ce.getMessage().getMentionedMembers().isEmpty()) { // Mention
          Member member = ce.getMessage().getMentionedMembers().get(0);
          sendEmbed(ce, display, member.getEffectiveName(), "Resolution: " + avatarSize + "x" + avatarSize,
              member.getUser().getAvatarUrl() + "?size=" + avatarSize);
        } else { // UserID || Self & Size
          if (args[1].length() == 18) { // UserID
            try { // Verify valid UserID
              User user = ce.getJDA().retrieveUserById(args[1]).complete();
              sendEmbed(ce, display, user.getName(), "Resolution: " + avatarSize + "x" + avatarSize,
                  user.getAvatarUrl() + "?size=" + avatarSize);
            } catch (ErrorResponseException error) { // Invalid UserID
              ce.getChannel().sendMessage("Invalid User ID.").queue();
            }
          } else { // Self & Size
            User user = ce.getMember().getUser();
            sendEmbed(ce, display, user.getName(), "Resolution: " + avatarSize + "x" + avatarSize,
                user.getAvatarUrl() + "?size=" + setImageSize(ce, args, 1, avatarSize, display));
          }
        }
      }
      case 3 -> { // Mention & Size || UserID & Size
        if (!ce.getMessage().getMentionedMembers().isEmpty()) { // Mention
          Member member = ce.getMessage().getMentionedMembers().get(0);
          sendEmbed(ce, display, member.getEffectiveName(), null,
              member.getUser().getAvatarUrl() + "?size=" + setImageSize(ce, args, 2, avatarSize, display));
        } else {
          if (args[1].length() == 18) { // UserID & Size
            try { // Verify UserID
              User user = ce.getJDA().retrieveUserById(args[1]).complete();
              sendEmbed(ce, display, user.getName(), null,
                  user.getAvatarUrl() + "?size=" + setImageSize(ce, args, 2, avatarSize, display));
            } catch (ErrorResponseException error) { // Invalid UserID
              ce.getChannel().sendMessage("Invalid User ID.").queue();
            }
          } else {
            ce.getChannel().sendMessage("Invalid User ID.").queue();
          }
        }
      }
      // Invalid arguments
      default -> ce.getChannel().sendMessage("Invalid number of arguments.").queue();
    }
  }

  private void sendEmbed(CommandEvent ce, EmbedBuilder display, String title, String description, String image) {
    display.setTitle(title);
    display.setDescription(description);
    display.setImage(image);
    Settings.sendEmbed(ce, display);
  }

  private String setImageSize(CommandEvent ce, String[] args, int argument, String avatarSize, EmbedBuilder display) {
    switch (args[argument]) {
      case "128", "256", "512", "1024" -> avatarSize = args[argument];
      default -> ce.getChannel().sendMessage("Image sizes only come in 128, 256, 512, & 1024.").queue();
    }
    display.setDescription("Resolution: " + avatarSize + "x" + avatarSize);
    return avatarSize;
  }
}