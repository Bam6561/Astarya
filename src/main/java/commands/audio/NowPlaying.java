package commands.audio;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import commands.audio.managers.PlayerManager;
import commands.owner.Settings;

public class NowPlaying extends Command {
  public NowPlaying() {
    this.name = "nowplaying";
    this.aliases = new String[]{"nowplaying", "np", "now"};
    this.arguments = "[0]NowPlaying";
    this.help = "Shows the user what's currently playing in the player.";
  }

  @Override
  protected void execute(CommandEvent ce) {
    Settings.deleteInvoke(ce);
    PlayerManager.getINSTANCE().getPlaybackManager(ce.getGuild()).audioScheduler.getNowPlaying(ce);
  }
}
