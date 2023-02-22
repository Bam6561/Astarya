package commands.audio;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import commands.audio.managers.AudioScheduler;
import commands.audio.managers.PlayerManager;
import commands.owner.Settings;
import net.dv8tion.jda.api.entities.GuildVoiceState;

public class Loop extends Command {
  public Loop() {
    this.name = "loop";
    this.aliases = new String[]{"loop", "repeat"};
    this.help = "Loops the current audio track.";
  }

  // Loops the currently playing track
  @Override
  protected void execute(CommandEvent ce) {
    Settings.deleteInvoke(ce);

    GuildVoiceState userVoiceState = ce.getMember().getVoiceState();
    GuildVoiceState botVoiceState = ce.getGuild().getSelfMember().getVoiceState();

    boolean userInVoiceChannel = ce.getMember().getVoiceState().inVoiceChannel();
    boolean userInSameVoiceChannel = userVoiceState.getChannel().equals(botVoiceState.getChannel());

    if (userInVoiceChannel) {
      if (userInSameVoiceChannel) {
        setAudioPlayerLoop(ce);
      } else {
        ce.getChannel().sendMessage("User not in the same voice channel.").queue();
      }
    } else {
      ce.getChannel().sendMessage("User not in a voice channel.").queue();
    }
  }

  // Sets the loop state of the audio player
  private void setAudioPlayerLoop(CommandEvent ce) {
    AudioScheduler audioScheduler = PlayerManager.getINSTANCE().getPlaybackManager(ce.getGuild()).audioScheduler;
    boolean audioPlayerNotLooped = !audioScheduler.getAudioPlayerLoopState();

    StringBuilder loopConfirmation = new StringBuilder();
    if (audioPlayerNotLooped) {
      audioScheduler.setAudioPlayerLoopState(true);
      loopConfirmation.append("**Loop:** `ON` [").append(ce.getAuthor().getAsTag()).append("]");
    } else {
      audioScheduler.setAudioPlayerLoopState(false);
      loopConfirmation.append("**Loop:** `OFF` [").append(ce.getAuthor().getAsTag()).append("]");
    }
    ce.getChannel().sendMessage(loopConfirmation).queue();
  }
}
