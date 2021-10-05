package lucyfer;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MessageLog extends ListenerAdapter {
  public void onGuildMessageReceived(GuildMessageReceivedEvent ce) {
    // MM/dd(HH:mm)<Server>#channel[UserTag]:Text(MessageAttachment)
    if (isHuman(ce)) {
      sendMessageLog(ce);
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
}