package commands.audio;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import commands.audio.managers.PlayerManager;
import commands.owner.Settings;

public class Queue extends Command {
  public Queue() {
    this.name = "queue";
    this.aliases = new String[]{"queue", "q"};
    this.arguments = "[0]queue";
    this.help = "Provides a list of tracks queued.";
  }

  @Override
  protected void execute(CommandEvent ce) {
    Settings.deleteInvoke(ce);
    PlayerManager.getINSTANCE().getPlaybackManager(ce.getGuild()).getAudioScheduler().getQueue(ce);
  }
}