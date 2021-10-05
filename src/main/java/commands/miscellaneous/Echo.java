package commands.miscellaneous;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import commands.owner.Settings;
import net.dv8tion.jda.api.EmbedBuilder;

public class Echo extends Command {
  public Echo() {
    this.name = "echo";
    this.aliases = new String[]{"echo", "copycat"};
    this.arguments = "[0++]Text";
    this.help = "Repeats user's text.";
  }

  @Override
  protected void execute(CommandEvent ce) {
    Settings.deleteInvoke(ce);
    if (ce.getMessage().getAttachments().isEmpty()) { // No attachments
      EmbedBuilder display = new EmbedBuilder();
      display.setDescription(ce.getMember().getAsMention() + ":" + ce.getMessage().getContentDisplay());
      Settings.sendEmbed(ce, display);
    } else { // Has attachments
      ce.getChannel().sendMessage("I'm not repeating anything with attachments in them.").queue();
    }
  }
}
