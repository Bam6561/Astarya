package commands.audio;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import commands.audio.managers.PlayerManager;
import commands.owner.Settings;

public class Pause extends Command {
  public Pause() {
    this.name = "pause";
    this.aliases = new String[]{"pause", "stop", "freeze"};
    this.arguments = "[0]pause";
    this.help = "Pauses the audio player.";
  }

  @Override
  protected void execute(CommandEvent ce) {
    Settings.deleteInvoke(ce);
    if (PlayerManager.getINSTANCE().getPlaybackManager(ce.getGuild()).getAudioScheduler().getAudioPlayer().isPaused()) {
      PlayerManager.getINSTANCE().getPlaybackManager(ce.getGuild()).getAudioScheduler().getAudioPlayer().setPaused(false);
      ce.getChannel().sendMessage("Audio player resumed.").queue();
    } else {
      PlayerManager.getINSTANCE().getPlaybackManager(ce.getGuild()).getAudioScheduler().getAudioPlayer().setPaused(true);
      ce.getChannel().sendMessage("Audio player paused.").queue();
    }
  }
}
