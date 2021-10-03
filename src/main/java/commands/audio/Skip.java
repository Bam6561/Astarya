package commands.audio;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import commands.audio.managers.AudioScheduler;
import commands.audio.managers.PlayerManager;
import commands.owner.Settings;

public class Skip extends Command {
  public Skip() {
    this.name = "skip";
    this.aliases = new String[]{"skip", "s", "next", "ff"};
    this.arguments = "[0]skip";
    this.help = "Skips the current audio track in the player.";
  }

  @Override
  protected void execute(CommandEvent ce) {
    Settings.deleteInvoke(ce);
    if (ce.getGuild().getSelfMember().getVoiceState().inVoiceChannel()) {
      if ((ce.getMember().getVoiceState().getChannel())
          .equals((ce.getGuild().getSelfMember().getVoiceState().getChannel()))) {
        AudioScheduler reference = PlayerManager.getINSTANCE().getPlaybackManager(ce.getGuild()).getAudioScheduler();
        StringBuilder skipString = new StringBuilder();
        skipString.append("**Skipped:** `").append(reference.getQueueListTitle()).append("` ")
            .append(reference.getRequesterListName());
        reference.forceSkip();
        ce.getChannel().sendMessage(skipString).queue();
      } else {
        ce.getChannel().sendMessage("I'm not in the same voice channel.").queue();
      }
    } else {
      ce.getChannel().sendMessage("I'm not in a voice channel yet.").queue();
    }
  }
}
