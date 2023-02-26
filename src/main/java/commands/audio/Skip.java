package commands.audio;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import commands.audio.managers.AudioScheduler;
import commands.audio.managers.PlayerManager;
import commands.owner.Settings;
import net.dv8tion.jda.api.entities.GuildVoiceState;

/**
 * Skip is a command invocation that skips the currently playing track in the audio player.
 *
 * @author Danny Nguyen
 * @version 1.5.4
 * @since 1.2.4
 */
public class Skip extends Command {
  public Skip() {
    this.name = "skip";
    this.aliases = new String[]{"skip", "s", "next"};
    this.help = "Skips the currently playing track.";
  }

  /**
   * Determines whether the user is in the same voice channel as the bot to process a skip command request.
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
        skipTrack(ce);
      } else {
        ce.getChannel().sendMessage("User not in the same voice channel.").queue();
      }
    } catch (NullPointerException e) {
      ce.getChannel().sendMessage("User not in a voice channel.").queue();
    }
  }

  /**
   * Skips the currently playing track in the queue and adds it to the skipped song stack.
   *
   * @param ce object containing information about the command event
   */
  private void skipTrack(CommandEvent ce) {
    AudioScheduler audioScheduler = PlayerManager.getINSTANCE().getPlaybackManager(ce.getGuild()).audioScheduler;
    AudioPlayer audioPlayer = audioScheduler.getAudioPlayer();

    boolean currentlyPlayingTrack = !(audioPlayer.getPlayingTrack() == null);
    if (currentlyPlayingTrack) {
      audioScheduler.addToSkippedStack(audioPlayer.getPlayingTrack().makeClone());
      audioScheduler.nextTrack();
      StringBuilder skipTrackConfirmation = new StringBuilder();
      skipTrackConfirmation.append("**Skip:** [").append(ce.getAuthor().getAsTag()).append("]");
      ce.getChannel().sendMessage(skipTrackConfirmation).queue();
    } else {
      ce.getChannel().sendMessage("Nothing to skip.").queue();
    }
  }
}
