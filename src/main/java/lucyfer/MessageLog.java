package lucyfer;

import commands.owner.Settings;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MessageLog extends ListenerAdapter {
  public void onGuildMessageReceived(GuildMessageReceivedEvent ce) {
    // MM/dd(HH:mm)<Server>#channel[UserTag]:Text(MessageAttachment)
    if (isHuman(ce)) {
      sendMessageLog(ce);
      if (Settings.getModeratePotentialPhishing()) {
        moderatePotentialPhishing(ce);
      }
    }
  }

  private void sendMessageLog(GuildMessageReceivedEvent ce) {
    System.out.println(getTime() + getGuildName(ce) + getChannelName(ce) + getAuthorTag(ce) + getMessageContent(ce)
        + (!ce.getMessage().getAttachments().isEmpty() ? getMessageAttachment(ce) : ""));
  }

  private boolean isHuman(GuildMessageReceivedEvent ce) {
    return (!ce.getMessage().isWebhookMessage() && (!ce.getMessage().getAuthor().isBot()));
  }

  private static String getTime() {
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM/dd(HH:mm)");
    LocalDateTime currentDateTime = LocalDateTime.now();
    return currentDateTime.format(dtf);
  }

  private static String getGuildName(GuildMessageReceivedEvent ce) {
    return "<" + ce.getGuild().getName() + ">";
  }

  private static String getChannelName(GuildMessageReceivedEvent ce) {
    return "#" + ce.getChannel().getName() + "";
  }

  private static String getAuthorTag(GuildMessageReceivedEvent ce) {
    return "[" + ce.getAuthor().getAsTag() + "]:";
  }

  private static String getMessageContent(GuildMessageReceivedEvent ce) {
    return (!ce.getMessage().getContentDisplay().isEmpty() ? ce.getMessage().getContentDisplay() + " " : "");
  }

  private static String getMessageAttachment(GuildMessageReceivedEvent event) {
    return "(" + event.getMessage().getAttachments().get(0).getUrl() + ")";
  }

  private void moderatePotentialPhishing(GuildMessageReceivedEvent ce) {
    String message = ce.getMessage().getContentRaw().toLowerCase();
    if (message.contains("http://") || message.contains("https://")) { // Contains a link
      if (message.contains("discord")) { // Contains discord
        String domain;
        int firstOccurrence = message.indexOf("discord");
        try {
          if (!message.contains("discordapp")) { // Discord
            domain = message.substring(firstOccurrence + 7);
            if (!(domain.substring(0, 4).equals(".com") || domain.substring(0, 3).equals(".gg"))) {
              ce.getMessage().delete().queue();
              ce.getChannel().sendMessage("**Potentially Dangerous Link!** " +ce.getAuthor().getAsMention()).queue();
            }
          } else { // Discordapp
            domain = message.substring(firstOccurrence + 10);
            if (!(domain.substring(0, 4).equals(".com") || domain.substring(0, 3).equals(".gg"))) {
              ce.getMessage().delete().queue();
              ce.getChannel().sendMessage("**Potentially Dangerous Link!** " +ce.getAuthor().getAsMention()).queue();
            }
          }
        } catch (IndexOutOfBoundsException error) {
        }
      } else if (message.contains(".gift")) { // Gift link
        int firstOccurrence = message.indexOf("gift");
        try { // Invalid gift link
          if (!(message.substring(firstOccurrence - 8, firstOccurrence).equals("discord.") ||
              (message.substring(firstOccurrence - 11, firstOccurrence).equals("discordapp.")))) {
            ce.getMessage().delete().queue();
            ce.getChannel().sendMessage("**Potentially Dangerous Link!** " +ce.getAuthor().getAsMention()).queue();
          }
        } catch (IndexOutOfBoundsException error) {
        }
      } else if (message.contains("nitro") || message.contains("gift")) {
        ce.getMessage().delete().queue();
        ce.getChannel().sendMessage("**Potentially Dangerous Link!** " +ce.getAuthor().getAsMention()).queue();
      }
    }
  }
}