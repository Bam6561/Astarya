package lucyfer;

import commands.owner.Settings;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MessageLog extends ListenerAdapter {
  public void onGuildMessageReceived(GuildMessageReceivedEvent ce) {
    boolean isHuman = !ce.getMessage().isWebhookMessage() && (!ce.getMessage().getAuthor().isBot());
    if (isHuman) {
      sendMessageLog(ce);
      if (Settings.getModeratePotentialPhishing()) {
        moderatePotentialPhishing(ce);
      }
    }
  }

  // Logs sent messages in application console, used for debugging
  // MM/dd(HH:mm)<Server>#channel[UserTag]:Text(MessageAttachment)
  private void sendMessageLog(GuildMessageReceivedEvent ce) {
    System.out.println(getTime() + getGuildName(ce) + getChannelName(ce) + getAuthorTag(ce) + getMessageContent(ce)
        + (!ce.getMessage().getAttachments().isEmpty() ? getMessageAttachment(ce) : ""));
  }

  // Get various variables for formatting messages logged
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

  // Delete potential phishing attempts
  private void moderatePotentialPhishing(GuildMessageReceivedEvent ce) {
    String message = ce.getMessage().getContentRaw().toLowerCase();

    boolean containsLink = message.contains("https://") || message.contains("http://");
    while (containsLink) { // Filter links from message
      if (message.contains("https://")) { // Found https://
        if (!message.equals("https://")) { // Doesn't contain scheme only
          message = message.substring(message.indexOf("https://") + 8); // Separate scheme from URL
          containsLink = isSafeURL(ce, message); // Unsafe URL content signals message deletion & ends loop immediately
        } else { // Contains scheme only
          containsLink = false;
        }
      } else if (message.contains("http://")) { // Found http://
        if (!message.equals("http://")) { // Doesn't contain scheme only
          message = message.substring(message.indexOf("http://") + 7); // Separate scheme from URL
          containsLink = isSafeURL(ce, message); // Unsafe URL content signals message deletion & ends loop immediately
        } else { // Contains scheme only
          containsLink = false;
        }
      } else { // No more links in message
        containsLink = false;
      }
    }
  }

  // Deletes message if any unsafe links are found
  private boolean isSafeURL(GuildMessageReceivedEvent ce, String message) {
    String link;
    try { // Delimit url by space
      link = message.substring(0, message.indexOf(" "));
    } catch (StringIndexOutOfBoundsException error) { // No space
      link = message;
    }

    if (isPotentialPhishingLink(link)) { // Screen link
      ce.getMessage().delete().queue();
      ce.getChannel().sendMessage("**Potentially Dangerous Link!** "
          + ce.getAuthor().getAsMention()).queue();
      return false;
    }

    return true; // Continue to next link if URL is deemed safe
  }

  // Definition for potentially risky links
  private boolean isPotentialPhishingLink(String link) {
    String domain;

    // Separate subdirectories from domain
    try {
      domain = link.substring(0, link.indexOf("/"));
    } catch (IndexOutOfBoundsException error) { // No subdirectories
      domain = link;
    }

    // Contains discord
    if (domain.contains("discord")) {
      int discordIndex = domain.indexOf("discord");

      if (!domain.contains("discordapp")) { // Discord
        domain = domain.substring(discordIndex + 7);
      } else { // Discordapp
        domain = domain.substring(discordIndex + 10);
      }

      switch (domain.length()) { // Validate top level domain
        case 0 -> {
          return false;
        }
        case 3 -> {
          if (domain.startsWith(".gg")) {
            return false;
          }
        }
        case 4 -> {
          if (domain.startsWith(".com") ||
              domain.startsWith(".net")) {
            return false;
          }
        }
        case 5 -> {
          if (domain.startsWith(".gift")) {
            return false;
          }
        }
      }
      return true; // Top level domain doesn't exist
    } else if (domain.contains("nitro") || domain.contains("gift")) {
      return true;
    }

    // Irregular patterns
    return domain.contains("d1sc") || domain.contains("dlsc") || domain.contains("discod") ||
        domain.contains("discorcl") || domain.contains("discond") || domain.contains("discrond") ||
        domain.contains("discrod");
  }
}