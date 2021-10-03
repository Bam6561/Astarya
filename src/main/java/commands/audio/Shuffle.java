package commands.audio;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import commands.audio.managers.PlayerManager;
import commands.owner.Settings;

public class Shuffle extends Command {
  public Shuffle() {
    this.name = "shuffle";
    this.aliases = new String[]{"shuffle", "mix"};
    this.arguments = "[0]shuffle";
    this.help = "Shuffles audio tracks in the player.";
  }

  @Override
  protected void execute(CommandEvent ce) {
    Settings.deleteInvoke(ce);
    if (ce.getGuild().getSelfMember().getVoiceState().inVoiceChannel()) {
      if ((ce.getMember().getVoiceState().getChannel())
          .equals((ce.getGuild().getSelfMember().getVoiceState().getChannel()))) {
        PlayerManager.getINSTANCE().getPlaybackManager(ce.getGuild()).getAudioScheduler().Shuffle();
        StringBuilder shuffleConfirmation = new StringBuilder();
        shuffleConfirmation.append("**SHUFFLE:** Tracks shuffled. [").append(ce.getAuthor().getAsTag()).append("]");
        ce.getChannel().sendMessage(shuffleConfirmation).queue();
      } else
        ce.getChannel().sendMessage("I'm not in the same voice channel.").queue();
    } else {
      ce.getChannel().sendMessage("I'm not in a voice channel yet.").queue();
    }
  }
}
