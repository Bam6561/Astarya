package me.dannynguyen.astarya;

import me.dannynguyen.astarya.commands.owner.Settings;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Message received listener.
 *
 * @author Danny Nguyen
 * @version 1.8.3
 * @since 1.0.0
 */
public class MessageEvent extends ListenerAdapter {
  /**
   * No parameter constructor.
   */
  public MessageEvent() {
  }

  /**
   * Routes interactions for messages sent.
   *
   * @param e message received event
   */
  public void onMessageReceived(MessageReceivedEvent e) {
    boolean isHuman = !e.getMessage().isWebhookMessage() && !e.getMessage().getAuthor().isBot();
    if (isHuman) {
      MessageLogger ml = new MessageLogger(e);
      ml.logMessage();
      if (Settings.getEmbedMediaLinks()) {
        ml.checkForMediaLinks();
      }
    }
  }

  /**
   * Prints messages sent by users into the command terminal and
   * embeds Twitter, Reddit, Instagram, and Pixiv media links.
   * <p>
   * Strictly used for debugging purposes only.
   * <p>
   * The logger does not write any data into external files, and its
   * content is lost upon closing the application's terminal window.
   *
   * @param e message received event
   * @author Danny Nguyen
   * @version 1.8.3
   * @since 1.8.3
   */
  private record MessageLogger(MessageReceivedEvent e) {
    /**
     * Logs human user sent messages into the application's
     * terminal window and is formatted as follows:
     * MM/dd(HH:mm)<Server>#channel[UserTag]:Text(MessageAttachment)
     */
    private void logMessage() {
      StringBuilder message = new StringBuilder();
      message.append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("MM/dd(HH:mm)")));
      message.append("<").append(e.getGuild().getName()).append(">");
      message.append("#").append(e.getChannel().getName());
      message.append("[").append(e.getAuthor().getAsTag()).append("]:");
      message.append(e.getMessage().getContentDisplay());

      StringBuilder links = new StringBuilder();
      for (Message.Attachment attachment : e.getMessage().getAttachments()) {
        links.append(attachment.getUrl()).append(" ");
      }
      if (!links.isEmpty()) {
        message.append("(").append(links.toString().trim()).append(")");
      }

      System.out.println(message);
    }

    /**
     * Checks if message contains a Twitter, Reddit, Instagram, or Pixiv media link.
     */
    private void checkForMediaLinks() {
      String checkedMessage = e.getMessage().getContentRaw().toLowerCase();
      if (!checkedMessage.contains("https://")) {
        return;
      }

      boolean isTwitterMedia = (checkedMessage.contains("twitter.com/") || checkedMessage.contains("x.com/")) && checkedMessage.contains("/status/");
      boolean isRedditMedia = checkedMessage.contains("www.reddit.com/");
      boolean isInstagramMedia = checkedMessage.contains("www.instagram.com/");
      boolean isPixiv = checkedMessage.contains("www.pixiv.net/");

      if (isTwitterMedia || isRedditMedia || isInstagramMedia || isPixiv) {
        repostMessage(replaceMediaLinks(isTwitterMedia, isRedditMedia, isInstagramMedia, isPixiv));
      }
    }

    /**
     * Replaces
     * <ul>
     *  <li> Twitter's domain with vxtwitter
     *  <li> Reddit's domain with rxddit
     *  <li> Instagram's domain with ddinstagram
     *  <li> Pixiv's domain with phixiv
     * </ul>
     * to embed its content.
     *
     * @param isTwitterMedia   is Twitter media
     * @param isRedditMedia    is Reddit media
     * @param isInstagramMedia is Instagram media
     * @param isPixiv          is Pixiv
     */
    private String replaceMediaLinks(boolean isTwitterMedia, boolean isRedditMedia, boolean isInstagramMedia, boolean isPixiv) {
      String message = "[" + e.getAuthor().getAsTag() + "]\n" + e.getMessage().getContentRaw();

      if (isTwitterMedia) {
        message = message.replace("/twitter.com/", "/vxtwitter.com/");
        message = message.replace("/x.com/", "/vxtwitter.com/");
      }
      if (isRedditMedia) {
        message = message.replace("www.reddit", "www.rxddit");
      }
      if (isInstagramMedia) {
        message = message.replace("www.instagram", "www.ddinstagram");
      }
      if (isPixiv) {
        message = message.replace("www.pixiv", "www.phixiv");
      }

      return message;
    }

    /**
     * Attempts to delete the old message and repost it with its links replaced.
     *
     * @param message message
     */
    private void repostMessage(String message) {
      try {
        e.getMessage().delete().queue();
      } catch (ErrorResponseException ignored) {
        // Insufficient permissions
      }

      Message reply = e.getMessage().getReferencedMessage();
      if (reply == null) {
        e.getChannel().sendMessage(message).queue();
      } else {
        e.getChannel().sendMessage(message).setMessageReference(reply).mentionRepliedUser(false).queue();
      }
    }
  }
}