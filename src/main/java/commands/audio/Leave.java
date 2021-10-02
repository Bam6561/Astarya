package commands.audio;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import commands.owner.Settings;
import net.dv8tion.jda.api.managers.AudioManager;

public class Leave extends Command {
  public Leave() {
    this.name = "leave";
    this.aliases = new String[]{"leave", "disconnect", "dc", "goaway", "getout"};
    this.help = "Leaves the voice channel the bot is in.";
  }

  @Override
  protected void execute(CommandEvent ce) {
    Settings.deleteInvoke(ce);
    AudioManager audioManager = ce.getGuild().getAudioManager();
    audioManager.closeAudioConnection();
    ce.getChannel().sendMessage("Thanks for listening. See you later!").queue();
  }
}