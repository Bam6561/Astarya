package commands.utility;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import commands.owner.Settings;
import net.dv8tion.jda.api.EmbedBuilder;

public class Emote extends Command {
  public Emote() {
    this.name = "emote";
    this.aliases = new String[]{"emote", "emoji"};
    this.arguments = "[1]Emote";
    this.help = "Provides mentioned emote as a file.";
  }

  @Override
  protected void execute(CommandEvent ce) {
    Settings.deleteInvoke(ce);
    if (!ce.getMessage().getEmotes().isEmpty()) { // Emotes
      EmbedBuilder display = new EmbedBuilder();
      display.setTitle(ce.getMessage().getEmotes().get(0).getName());
      display.setImage(ce.getMessage().getEmotes().get(0).getImageUrl());
      Settings.sendEmbed(ce, display);
    } else {
      ce.getChannel().sendMessage("You must include an emote as an argument.").queue();
    }
  }
}