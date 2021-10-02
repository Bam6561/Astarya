package commands.audio;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import commands.audio.managers.PlayerManager;
import commands.owner.Settings;

public class ClearQueue extends Command {
  public ClearQueue() {
    this.name = "clearqueue";
    this.aliases = new String[]{"clearqueue", "clear"};
    this.help = "Clears the track queue.";
  }

  @Override
  protected void execute(CommandEvent ce) {
    Settings.deleteInvoke(ce);
    PlayerManager.getINSTANCE().getPlaybackManager(ce.getGuild()).getAudioScheduler().clearQueue(ce);
  }
}
