package astarya;

import commands.owner.Settings;
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
 * @version 1.6.10
 * @since 1.0.0
 */
public class MessageLog extends ListenerAdapter {
  /**
   * Logs messages if they were sent by a human user and embeds Twitter, Reddit,
   * Instagram, and Pixiv media links if the setting for embedMediaLinks is true.
   *
   * @param messageE object containing information about the message event
   */
  public void onMessageReceived(MessageReceivedEvent messageE) {
    boolean isHuman = !messageE.getMessage().isWebhookMessage() && !messageE.getMessage().getAuthor().isBot();
    boolean embedMediaLinks = Settings.getEmbedMediaLinks();

    if (isHuman) {
      logMessage(messageE);
      if (embedMediaLinks) {
        scanForMediaLinks(messageE);
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
   * Checks if message contains a Twitter, Reddit, Instagram, or Pixiv media link.
   *
   * If a link is found, replace
   * - Twitter's domain with vxtwitter
   * - Reddit's domain with rxddit
   * - Instagram's domain with ddinstagram
   * - Pixiv's domain with phixiv
   * to embed its content.
   *
   * @param messageE object containing information about the message event
   */
  private void scanForMediaLinks(MessageReceivedEvent messageE) {
    String message = messageE.getMessage().getContentRaw();
    String checkMessage = message.toLowerCase();

    boolean isTwitterLink = (checkMessage.contains("https://twitter.com/") || checkMessage.contains("https://x.com/"));
    boolean isTwitterMedia = checkMessage.contains("/status/");
    boolean isRedditMedia = checkMessage.contains("https://www.reddit.com/");
    boolean isInstagramReel = checkMessage.contains("https://www.instagram.com/reel");
    boolean isPixiv = checkMessage.contains("https://www.pixiv.net/");

    // Replace domain and delete original message if permissions allow
    if ((isTwitterLink && isTwitterMedia) || isRedditMedia || isInstagramReel || isPixiv) {
      message = "[" + messageE.getAuthor().getAsTag() + "]\n" + message;

      if (isTwitterMedia){
        message = message.replace("/twitter.com/", "/vxtwitter.com/");
        message = message.replace("/x.com/", "/vxtwitter.com/");
      }
      if (isRedditMedia) {
        message = message.replace("www.reddit", "www.rxddit");
      }
      if (isInstagramReel) {
        message = message.replace("www.instagram", "www.ddinstagram");
      }
      if (isPixiv){
        message = message.replace("www.pixiv.net","www.phixiv.net");
      }

      try {
        messageE.getMessage().delete().queue();
      } catch (ErrorResponseException e) {
      }

      messageE.getChannel().sendMessage(message).queue();
    }
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
}