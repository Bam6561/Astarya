package commands.audio;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import commands.audio.managers.PlayerManager;
import commands.owner.Settings;

public class Loop extends Command {
  public Loop() {
    this.name = "loop";
    this.aliases = new String[]{"loop", "again", "infinite", "repeat"};
    this.arguments = "[0]loop";
    this.help = "Loops the next track";
    this.ownerCommand = false;
  }

  @Override
  protected void execute(CommandEvent ce) {
    Settings.deleteInvoke(ce);
    if (ce.getGuild().getSelfMember().getVoiceState().inVoiceChannel()) {
      if ((ce.getMember().getVoiceState().getChannel())
          .equals((ce.getGuild().getSelfMember().getVoiceState().getChannel()))) {
        PlayerManager.getINSTANCE().getPlaybackManager(ce.getGuild()).getAudioScheduler().setLooped(ce);
      } else
        ce.getChannel().sendMessage("I'm not in the same voice channel.").queue();
    } else {
      ce.getChannel().sendMessage("I'm not in a voice channel yet.").queue();
    }
  }
}
