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
    if (ce.getGuild().getSelfMember().getVoiceState().inVoiceChannel()) {
      try {
        PlayerManager.getINSTANCE().getPlaybackManager(ce.getGuild()).getAudioScheduler().getNowPlaying(ce);
      } catch (IndexOutOfBoundsException error) {
        ce.getChannel().sendMessage("Error retrieving current audio track. There's either nothing playing, " +
            "or someone recently cleared the queue.").queue();
      }
    } else {
      ce.getChannel().sendMessage("I'm not in a voice channel yet.").queue();
    }
  }
}
