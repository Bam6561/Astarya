package astarya;

import commands.owner.Settings;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
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
 * @version 1.7.10
 * @since 1.0.0
 */
public class MessageLog extends ListenerAdapter {
  /**
   * Logs messages if they were sent by a human user and embeds Twitter, Reddit,
   * Instagram, and Pixiv media links if the setting for embedMediaLinks is true.
   *
   * @param e message received event
   */
  public void onMessageReceived(MessageReceivedEvent e) {
    boolean isHuman = !e.getMessage().isWebhookMessage() && !e.getMessage().getAuthor().isBot();
    if (isHuman) {
      logMessage(e);
      if (Settings.getEmbedMediaLinks()) {
        checkForMediaLinks(e);
      }
    }
  }

  /**
   * Logs human user sent messages into the application's
   * terminal window and is formatted as follows:
   * MM/dd(HH:mm)<Server>#channel[UserTag]:Text(MessageAttachment)
   *
   * @param e message received event
   */
  private void logMessage(MessageReceivedEvent e) {
    System.out.println(getTime() + getGuildName(e)
        + getChannelName(e) + getAuthorTag(e) + getMessageContent(e)
        + (!e.getMessage().getAttachments().isEmpty() ? getMessageAttachments(e) : ""));
  }

  /**
   * Checks if message contains a Twitter, Reddit, Instagram, or Pixiv media link.
   *
   * @param e message received event
   */
  private void checkForMediaLinks(MessageReceivedEvent e) {
    String originalMessage = e.getMessage().getContentRaw();
    String scannedMessage = originalMessage.toLowerCase();

    boolean isTwitterLink = (scannedMessage.contains("https://twitter.com/") ||
        scannedMessage.contains("https://x.com/"));
    boolean isTwitterMedia = scannedMessage.contains("/status/");
    boolean isRedditMedia = scannedMessage.contains("https://www.reddit.com/");
    boolean isInstagramMedia = scannedMessage.contains("https://www.instagram.com/");
    boolean isPixiv = scannedMessage.contains("https://www.pixiv.net/");

    // Replace domain and delete original message if permissions allow
    if ((isTwitterLink && isTwitterMedia) || isRedditMedia || isInstagramMedia || isPixiv) {
      originalMessage = "[" + e.getAuthor().getAsTag() + "]\n" + originalMessage;
      replaceMediaLinks(e, originalMessage, isTwitterMedia, isRedditMedia, isInstagramMedia, isPixiv);
    }
  }

  /**
   * Replace
   * - Twitter's domain with vxtwitter
   * - Reddit's domain with rxddit
   * - Instagram's domain with ddinstagram
   * - Pixiv's domain with phixiv
   * to embed its content.
   *
   * @param e                message received event
   * @param originalMessage  original message
   * @param isTwitterMedia   is Twitter media
   * @param isRedditMedia    is Reddit media
   * @param isInstagramMedia is Instagram media
   * @param isPixiv          is Pixiv
   */
  private void replaceMediaLinks(MessageReceivedEvent e, String originalMessage,
                                 boolean isTwitterMedia, boolean isRedditMedia,
                                 boolean isInstagramMedia, boolean isPixiv) {
    if (isTwitterMedia) {
      originalMessage = originalMessage.replace("/twitter.com/", "/vxtwitter.com/");
      originalMessage = originalMessage.replace("/x.com/", "/vxtwitter.com/");
    }
    if (isRedditMedia) {
      originalMessage = originalMessage.replace("www.reddit", "www.rxddit");
    }
    if (isInstagramMedia) {
      originalMessage = originalMessage.replace("www.instagram", "www.ddinstagram");
    }
    if (isPixiv) {
      originalMessage = originalMessage.replace("www.pixiv.net", "www.phixiv.net");
    }

    try {
      e.getMessage().delete().queue();
    } catch (ErrorResponseException ignored) {
    }

    Message repliedTo = e.getMessage().getReferencedMessage();
    if (repliedTo == null) {
      e.getChannel().sendMessage(originalMessage).queue();
    } else {
      e.getChannel().sendMessage(originalMessage).
          setMessageReference(repliedTo).
          mentionRepliedUser(false).queue();
    }
  }

  private String getTime() {
    return LocalDateTime.now().format(DateTimeFormatter.ofPattern("MM/dd(HH:mm)"));
  }

  private String getGuildName(MessageReceivedEvent e) {
    return "<" + e.getGuild().getName() + ">";
  }

  private String getChannelName(MessageReceivedEvent e) {
    return "#" + e.getChannel().getName();
  }

  private String getAuthorTag(MessageReceivedEvent e) {
    return "[" + e.getAuthor().getAsTag() + "]:";
  }

  private String getMessageContent(MessageReceivedEvent e) {
    return (!e.getMessage().getContentDisplay().isEmpty() ? e.getMessage().getContentDisplay() + " " : "");
  }

  private String getMessageAttachments(MessageReceivedEvent e) {
    StringBuilder links = new StringBuilder();
    for (Message.Attachment attachment : e.getMessage().getAttachments()) {
      links.append(attachment.getUrl() + " ");
    }
    return "(" + links.toString().trim() + ")";
  }
}