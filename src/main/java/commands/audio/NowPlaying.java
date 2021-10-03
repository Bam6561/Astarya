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
    this.help = "Shows user what's currently playing on the bot.";
  }

  @Override
  protected void execute(CommandEvent ce) {
    Settings.deleteInvoke(ce);
    PlayerManager.getINSTANCE().getPlaybackManager(ce.getGuild()).getAudioScheduler().getNowPlaying(ce);
  }
}
