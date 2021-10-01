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

  private boolean isHuman(GuildMessageReceivedEvent e) {
    return (!e.getMessage().isWebhookMessage() && (!e.getMessage().getAuthor().isBot()));
  }

  private static String getTime() {
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM/dd(HH:mm)");
    LocalDateTime currentDateTime = LocalDateTime.now();
    return currentDateTime.format(dtf);
  }

  private static String getGuildName(GuildMessageReceivedEvent e) {
    return "<" + e.getGuild().getName() + ">";
  }

  private static String getChannelName(GuildMessageReceivedEvent e) {
    return "#" + e.getChannel().getName() + "";
  }

  private static String getAuthorTag(GuildMessageReceivedEvent e) {
    return "[" + e.getAuthor().getAsTag() + "]:";
  }

  private static String getMessageContent(GuildMessageReceivedEvent e) {
    return (!e.getMessage().getContentDisplay().isEmpty() ? e.getMessage().getContentDisplay() + " " : "");
  }

  private static String getMessageAttachment(GuildMessageReceivedEvent event) {
    return "(" + event.getMessage().getAttachments().get(0).getUrl() + ")";
  }
}