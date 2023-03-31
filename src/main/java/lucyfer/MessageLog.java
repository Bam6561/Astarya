package lucyfer;

import commands.owner.Settings;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * MessageLog prints messages sent by users in Discord channels
 * that the bot has access to view into the command terminal.
 * <p>
 * Strictly used for debugging purposes only, the log does not write any data into
 * external files, and its content is lost upon closing the application's terminal window.
 * </p>
 *
 * @author Danny Nguyen
 * @version 1.6
 * @since 1.0.0
 */
public class MessageLog extends ListenerAdapter {
  private String messageWithLinks;

  /**
   * Logs messages if they were sent by a human user and optionally flags
   * suspicious links if the setting for moderatePotentialPhishing is true.
   *
   * @param messageE object containing information about the message event
   */
  public void onMessageReceived(MessageReceivedEvent messageE) {
    boolean isHuman = !messageE.getMessage().isWebhookMessage() && !messageE.getMessage().getAuthor().isBot();
    boolean moderatingPotentialPhishing = Settings.getModeratePotentialPhishing();

    if (isHuman) {
      logMessage(messageE);
      if (moderatingPotentialPhishing) {
        scanLinksInMessage(messageE);
      }
    }
  }

  /**
   * Logs human user sent messages into the application's
   * terminal window and is formatted as follows:
   * MM/dd(HH:mm)<Server>#channel[UserTag]:Text(MessageAttachment)
   *
   * @param messageE object containing information about the message event
   */
  private void logMessage(MessageReceivedEvent messageE) {
    System.out.println(getTime() + getGuildName(messageE)
        + getChannelName(messageE) + getAuthorTag(messageE) + getMessageContent(messageE)
        + (!messageE.getMessage().getAttachments().isEmpty() ? getMessageAttachment(messageE) : ""));
  }

  /**
   * Checks if the message contains any links. If a link is found, determine whether the link is a safe URL.
   * <p>
   * There may be multiple links in the message, so all links are checked individually
   * until a potential phishing link is found or no more links exist in the message to check.
   * </p>
   *
   * @param messageE object containing information about the message event
   */
  private void scanLinksInMessage(MessageReceivedEvent messageE) {
    String message = messageE.getMessage().getContentRaw().toLowerCase();

    boolean containsLinks = message.contains("https://") || message.contains("http://");
    boolean notLinkSchemeOnly = !(message.equals("https://") || message.equals("http://"));
    boolean continueToCheckLinks = containsLinks && notLinkSchemeOnly;

    if (continueToCheckLinks) {
      setMessageWithLinks(message);
    }

    // Any unsafe URL content signals message deletion and ends the loop immediately
    while (continueToCheckLinks) {
      if (getMessageWithLinks().contains("https://")) {
        continueToCheckLinks = isSafeURL(messageE, true);
      } else if (getMessageWithLinks().contains("http://")) {
        continueToCheckLinks = isSafeURL(messageE, false);
      } else {
        continueToCheckLinks = false;
      }
    }
  }

  /**
   * Separates the scheme from the URL and determines whether to delete the
   * message if it contains a potential phishing link. If the currently scanned
   * link tests negative,then continue to the next link in the message.
   * <p>
   * Each time this method is called, the message is truncated forward to the first
   * URL instance found. Repeated usage of this method allow for dynamically checking
   * all the links in the message, as it is reused as a parameter that is smaller each time.
   * </p>
   *
   * @param messageE object containing information about a message event
   * @param isHttps  whether the link is formatted https or http
   * @return the status of whether the URL is safe
   * @throws StringIndexOutOfBoundsException no space exists in the URL
   * @throws InsufficientPermissionException bot does not have the manage messages permission
   */
  private boolean isSafeURL(MessageReceivedEvent messageE, boolean isHttps) {
    // Separate the scheme from the URL
    if (isHttps) {
      setMessageWithLinks(getMessageWithLinks().substring(getMessageWithLinks().indexOf("https://") + 8));
    } else {
      setMessageWithLinks(getMessageWithLinks().substring(getMessageWithLinks().indexOf("https://") + 7));
    }

    // Delimit the URL by space
    String link;
    try {
      link = getMessageWithLinks().substring(0, getMessageWithLinks().indexOf(" "));
    } catch (StringIndexOutOfBoundsException e) {
      link = getMessageWithLinks();
    }

    if (isPotentialPhishingLink(link)) {
      try {
        messageE.getMessage().delete().queue();
        messageE.getChannel().sendMessage("**Potentially Dangerous Link!** "
            + messageE.getAuthor().getAsMention()).queue();
      } catch (InsufficientPermissionException e) {
      }
      return false;
    }

    return true; // Continue to the next link in the message if this URL is deemed safe
  }

  /**
   * Contains the definitions of potential phishing links.
   * <p>
   * To identify potentially risky links, a link's domain is first examined.
   * If it identifies as a Discord related site, then several official patterns
   * are excused. Otherwise, unrecognized patterns are marked for deletion.
   * </p>
   *
   * @param link URL link
   * @return whether the link is potentially risky and its message should be deleted
   * @throws IndexOutOfBoundsException link has no subdirectories
   */
  private boolean isPotentialPhishingLink(String link) {
    // Separate subdirectories from the domain
    String domain;
    try {
      domain = link.substring(0, link.indexOf("/"));
    } catch (IndexOutOfBoundsException e) {
      domain = link;
    }

    if (domain.contains("discord")) {
      int discordIndex = domain.indexOf("discord");

      boolean isDiscordAppDomain = domain.contains("discordapp");
      if (!isDiscordAppDomain) {
        domain = domain.substring(discordIndex + 7);
      } else {
        domain = domain.substring(discordIndex + 10);
      }

      // Validate top level domain
      switch (domain.length()) {
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

  private static String getTime() {
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM/dd(HH:mm)");
    LocalDateTime currentDateTime = LocalDateTime.now();
    return currentDateTime.format(dtf);
  }

  private static String getGuildName(MessageReceivedEvent messageE) {
    return "<" + messageE.getGuild().getName() + ">";
  }

  private static String getChannelName(MessageReceivedEvent messageE) {
    return "#" + messageE.getChannel().getName() + "";
  }

  private static String getAuthorTag(MessageReceivedEvent messageE) {
    return "[" + messageE.getAuthor().getAsTag() + "]:";
  }

  private static String getMessageContent(MessageReceivedEvent messageE) {
    return (!messageE.getMessage().getContentDisplay().isEmpty() ? messageE.getMessage().getContentDisplay() + " " : "");
  }

  private static String getMessageAttachment(MessageReceivedEvent messageE) {
    return "(" + messageE.getMessage().getAttachments().get(0).getUrl() + ")";
  }

  private String getMessageWithLinks() {
    return this.messageWithLinks;
  }

  private void setMessageWithLinks(String message) {
    this.messageWithLinks = message;
  }
}