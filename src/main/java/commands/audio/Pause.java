package commands.audio;

import astarya.Astarya;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import commands.audio.managers.AudioScheduler;
import commands.audio.managers.PlayerManager;
import commands.owner.Settings;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.managers.Presence;

/**
 * Pause is a command invocation that pauses the audio player.
 *
 * @author Danny Nguyen
 * @version 1.7.2
 * @since 1.2.5
 */
public class Pause extends Command {
  public Pause() {
    this.name = "pause";
    this.aliases = new String[]{"pause", "stop"};
    this.help = "Pauses the audio player.";
  }

  /**
   * Checks if the user is in the same voice channel as the bot to read a pause command request.
   *
   * @param ce object containing information about the command event
   * @throws NullPointerException user not in same voice channel
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
   * to if it's paused, playing music, or not playing anything.
   *
   * @param ce object containing information about the command event
   */
  private void setAudioPlayerPause(CommandEvent ce) {
    AudioScheduler audioScheduler = PlayerManager.getINSTANCE().getPlaybackManager(ce.getGuild()).audioScheduler;
    AudioPlayer audioPlayer = audioScheduler.getAudioPlayer();

    Astarya Astarya = new Astarya();
    Presence presence = Astarya.getApi().getPresence();

    boolean audioPlayerNotPaused = !audioPlayer.isPaused();
    if (audioPlayerNotPaused) { // Paused - Idle Yellow
      setActivityToPaused(ce, audioPlayer, presence);
    } else { // Playing Music - Online Green || Not playing audio - Do Not Disturb Red
      audioPlayer.setPaused(false);
      try {
        setActivityToPlayingMusic(audioPlayer, presence);
      } catch (NullPointerException e) { // No track currently playing
        setActivityToNothing(presence);
      }
      ce.getChannel().sendMessage("Audio player resumed.").queue();
    }
  }

  /**
   * Sets the bot's activity to paused and status to Idle.
   *
   * @param ce          object containing information about the command event
   * @param audioPlayer bot's audio player
   * @param presence    bot's presence
   */
  private void setActivityToPaused(CommandEvent ce, AudioPlayer audioPlayer, Presence presence) {
    audioPlayer.setPaused(true);
    presence.setActivity(Activity.listening("Paused"));
    presence.setStatus(OnlineStatus.IDLE);
    ce.getChannel().sendMessage("Audio player paused.").queue();
  }

  /**
   * Sets the bot's activity to listening <track name> and status to Online.
   *
   * @param audioPlayer bot's audio player
   * @param presence    bot's presence
   */
  private void setActivityToPlayingMusic(AudioPlayer audioPlayer, Presence presence) {
    presence.setActivity(Activity.listening(audioPlayer.getPlayingTrack().getInfo().title));
    presence.setStatus(OnlineStatus.ONLINE);
  }

  /**
   * Sets the bot's activity to listening to nothing and status to Do Not Disturb.
   *
   * @param presence bot's presence
   */
  private void setActivityToNothing(Presence presence) {
    presence.setActivity(Activity.listening("Nothing"));
    presence.setStatus(OnlineStatus.DO_NOT_DISTURB);
  }
}
