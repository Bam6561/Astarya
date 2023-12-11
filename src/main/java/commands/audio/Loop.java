package commands.audio;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import commands.audio.managers.AudioScheduler;
import commands.audio.managers.PlayerManager;
import commands.owner.Settings;
import net.dv8tion.jda.api.entities.GuildVoiceState;

/**
 * Loop is a command invocation that sets a boolean value of whether to loop the currently playing track.
 *
 * @author Danny Nguyen
 * @version 1.7.0
 * @since 1.2.6
 */
public class Loop extends Command {
  public Loop() {
    this.name = "loop";
    this.aliases = new String[]{"loop", "repeat"};
    this.help = "Loops the current track.";
  }

  /**
   * Determines whether the user is in the same voice channel as the bot to process a loop command request.
   *
   * @param ce object containing information about the command event
   */
  @Override
  protected void execute(CommandEvent ce) {
    Settings.deleteInvoke(ce);

    GuildVoiceState userVoiceState = ce.getMember().getVoiceState();
    GuildVoiceState botVoiceState = ce.getGuild().getSelfMember().getVoiceState();

    try {
      boolean userInSameVoiceChannel = userVoiceState.getChannel().equals(botVoiceState.getChannel());
      if (userInSameVoiceChannel) {
        setAudioPlayerLoop(ce);
      } else {
        ce.getChannel().sendMessage("User not in the same voice channel.").queue();
      }
    } catch (NullPointerException e) {
      ce.getChannel().sendMessage("User not in a voice channel.").queue();
    }
  }

  /**
   * Sets the loop boolean value of the audio player.
   *
   * @param ce object containing information about the command event
   */
  private void setAudioPlayerLoop(CommandEvent ce) {
    AudioScheduler audioScheduler = PlayerManager.getINSTANCE().getPlaybackManager(ce.getGuild()).audioScheduler;
    boolean audioPlayerNotLooped = !audioScheduler.getAudioPlayerLooped();

    StringBuilder loopConfirmation = new StringBuilder();
    if (audioPlayerNotLooped) {
      audioScheduler.setAudioPlayerLooped(true);
      loopConfirmation.append("**Loop:** `ON` [").append(ce.getAuthor().getAsTag()).append("]");
    } else {
      audioScheduler.setAudioPlayerLooped(false);
      loopConfirmation.append("**Loop:** `OFF` [").append(ce.getAuthor().getAsTag()).append("]");
    }
    ce.getChannel().sendMessage(loopConfirmation).queue();
  }
}
