package commands.audio;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import commands.audio.managers.AudioScheduler;
import commands.audio.managers.PlayerManager;
import commands.owner.Settings;
import net.dv8tion.jda.api.entities.GuildVoiceState;

public class Skip extends Command {
  public Skip() {
    this.name = "skip";
    this.aliases = new String[]{"skip", "s", "next"};
    this.help = "Skips the currently playing audio track.";
  }

  // Skips the currently playing track
  @Override
  protected void execute(CommandEvent ce) {
    Settings.deleteInvoke(ce);

    GuildVoiceState userVoiceState = ce.getMember().getVoiceState();
    GuildVoiceState botVoiceState = ce.getGuild().getSelfMember().getVoiceState();

    boolean userInVoiceChannel = ce.getMember().getVoiceState().inVoiceChannel();
    boolean userInSameVoiceChannel = userVoiceState.getChannel().equals(botVoiceState.getChannel());

    if (userInVoiceChannel) {
      if (userInSameVoiceChannel) {
        skipTrack(ce);
      } else {
        ce.getChannel().sendMessage("User not in the same voice channel.").queue();
      }
    } else {
      ce.getChannel().sendMessage("User not in a voice channel.").queue();
    }
  }

  // Sends skipped track confirmation
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
