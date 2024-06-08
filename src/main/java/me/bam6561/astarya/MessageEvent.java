package me.bam6561.astarya;

import me.bam6561.astarya.commands.owner.Settings;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Message received listener.
 *
 * @author Danny Nguyen
 * @version 1.9.0
 * @since 1.0.0
 */
public class MessageEvent extends ListenerAdapter {
  /**
   * Users' most recently reposted media link.
   */
  private final Map<Long, Long> recentRepostMessages = new HashMap<>();

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
  @Override
  public void onMessageReceived(@NotNull MessageReceivedEvent e) {
    Message message = Objects.requireNonNull(e, "Null message received event").getMessage();
    if (!message.isWebhookMessage() && !message.getAuthor().isBot()) {
      MessageLogger ml = new MessageLogger(e, message);
      ml.logMessage();

      if (message.getContentRaw().equalsIgnoreCase("unsend")) {
        ml.unsendRepostedMessage();
        return;
      }

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
   * @author Danny Nguyen
   * @version 1.9.0
   * @since 1.8.3
   */
  private class MessageLogger {
    /**
     * Message received event.
     */
    private final MessageReceivedEvent e;

    /**
     * Message.
     */
    private final Message message;

    /**
     * Associates the message logger with its event and message.
     *
     * @param e       message received event
     * @param message message
     */
    MessageLogger(MessageReceivedEvent e, Message message) {
      this.e = e;
      this.message = message;
    }

    /**
     * Logs human user sent messages into the application's
     * terminal window and is formatted as follows:
     * MM/dd(HH:mm)Server#channel[UserTag]:Text(MessageAttachment)
     */
    private void logMessage() {
      StringBuilder messageBuilder = new StringBuilder();
      messageBuilder.append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("MM/dd(HH:mm)")));
      messageBuilder.append("<").append(e.getGuild().getName()).append(">");
      messageBuilder.append("#").append(e.getChannel().getName());
      messageBuilder.append("[").append(e.getAuthor().getAsTag()).append("]:");
      messageBuilder.append(message.getContentDisplay());

      StringBuilder linksBuilder = new StringBuilder();
      for (Message.Attachment attachment : message.getAttachments()) {
        linksBuilder.append(attachment.getUrl()).append(" ");
      }
      if (!linksBuilder.isEmpty()) {
        messageBuilder.append("(").append(linksBuilder.toString().trim()).append(")");
      }

      System.out.println(messageBuilder);
    }

    /**
     * Unsends a reposted message from embedding media links.
     */
    private void unsendRepostedMessage() {
      long authorId = message.getAuthor().getIdLong();
      if (recentRepostMessages.containsKey(authorId)) {
        try {
          e.getMessage().delete().queue();
          e.getChannel().deleteMessageById(recentRepostMessages.get(authorId)).queue();
        } catch (ErrorResponseException ignored) { // Request not sent in original channel
        }
        recentRepostMessages.remove(authorId);
      }
    }

    /**
     * Checks if message contains a Twitter, Reddit, Instagram, or Pixiv media link.
     */
    private void checkForMediaLinks() {
      String checkedMessage = message.getContentRaw().toLowerCase();
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
     * @return message with replacements
     */
    private String replaceMediaLinks(boolean isTwitterMedia, boolean isRedditMedia, boolean isInstagramMedia, boolean isPixiv) {
      String newMessage = "[" + e.getAuthor().getAsTag() + "]\n" + message.getContentRaw();

      if (isTwitterMedia) {
        newMessage = newMessage.replace("/twitter.com/", "/vxtwitter.com/");
        newMessage = newMessage.replace("/x.com/", "/vxtwitter.com/");
      }
      if (isRedditMedia) {
        newMessage = newMessage.replace("www.reddit", "www.rxddit");
      }
      if (isInstagramMedia) {
        newMessage = newMessage.replace("www.instagram", "www.ddinstagram");
      }
      if (isPixiv) {
        newMessage = newMessage.replace("www.pixiv", "www.phixiv");
      }

      return newMessage;
    }

    /**
     * Attempts to delete the old message and repost it with its links replaced.
     *
     * @param messageToRepost message to repost
     */
    private void repostMessage(String messageToRepost) {
      try {
        message.delete().queue();
      } catch (InsufficientPermissionException ignored) {
      }

      Message reply = message.getReferencedMessage();
      if (reply == null) {
        e.getChannel().sendMessage(messageToRepost).queue((message) -> recentRepostMessages.put(e.getAuthor().getIdLong(), message.getIdLong()));
      } else {
        e.getChannel().sendMessage(messageToRepost).setMessageReference(reply).mentionRepliedUser(false).queue((message) -> recentRepostMessages.put(e.getAuthor().getIdLong(), message.getIdLong()));
      }
    }
  }
}