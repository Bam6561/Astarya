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
    if (ce.getGuild().getSelfMember().getVoiceState().inVoiceChannel()) {
      if ((ce.getMember().getVoiceState().getChannel())
          .equals((ce.getGuild().getSelfMember().getVoiceState().getChannel()))) {
        PlayerManager.getINSTANCE().getPlaybackManager(ce.getGuild()).getAudioScheduler().clearQueue(ce);
      } else
        ce.getChannel().sendMessage("I'm not in the same voice channel.").queue();
    } else {
      ce.getChannel().sendMessage("I'm not in a voice channel yet.").queue();
    }
  }
}