package commands.audio;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import commands.audio.managers.AudioScheduler;
import commands.audio.managers.PlayerManager;
import commands.owner.Settings;
import lucyfer.LucyferBot;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.GuildVoiceState;

/**
 * Pause is a command invocation that pauses the audio player.
 *
 * @author Danny Nguyen
 * @version 1.5.4
 * @since 1.2.5
 */
public class Pause extends Command {
  public Pause() {
    this.name = "pause";
    this.aliases = new String[]{"pause", "stop"};
    this.help = "Pauses the audio player.";
  }

  /**
   * Determines whether the user is in the same voice channel as the bot to process a pause command request.
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
        setAudioPlayerPause(ce);
      } else {
        ce.getChannel().sendMessage("User not in the same voice channel.").queue();
      }
    } catch (NullPointerException e) {
      ce.getChannel().sendMessage("User not in a voice channel.").queue();
    }
  }

  /**
   * Pauses the audio player, and sets the bot's presence according
   * to whether it's paused, playing music, or not playing anything.
   *
   * @param ce object containing information about the command event
   */
  private void setAudioPlayerPause(CommandEvent ce) {
    AudioScheduler audioScheduler = PlayerManager.getINSTANCE().getPlaybackManager(ce.getGuild()).audioScheduler;
    AudioPlayer audioPlayer = audioScheduler.getAudioPlayer();
    LucyferBot lucyferBot = new LucyferBot();

    boolean audioPlayerNotPaused = !audioPlayer.isPaused();
    if (audioPlayerNotPaused) { // Update presence when Paused - Idle Yellow
      audioPlayer.setPaused(true);
      lucyferBot.getApi().getPresence().setActivity(Activity.listening("Paused"));
      lucyferBot.getApi().getPresence().setStatus(OnlineStatus.IDLE);
      ce.getChannel().sendMessage("Audio player paused.").queue();
    } else { // Update presence when Playing Music - Online Green || Not playing audio - Do Not Disturb Red
      audioPlayer.setPaused(false);
      try {
        lucyferBot.getApi().getPresence().setActivity(Activity.listening(audioPlayer.getPlayingTrack().getInfo().title));
        lucyferBot.getApi().getPresence().setStatus(OnlineStatus.ONLINE);
      } catch (NullPointerException e) { // No track currently playing
        lucyferBot.getApi().getPresence().setActivity(Activity.listening("Nothing"));
        lucyferBot.getApi().getPresence().setStatus(OnlineStatus.DO_NOT_DISTURB);
      }
      ce.getChannel().sendMessage("Audio player resumed.").queue();
    }
  }
}
