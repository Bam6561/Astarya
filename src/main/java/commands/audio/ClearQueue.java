package commands.audio;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import commands.audio.managers.AudioScheduler;
import commands.audio.managers.PlayerManager;
import commands.owner.Settings;
import net.dv8tion.jda.api.entities.GuildVoiceState;

import java.util.ArrayList;

public class ClearQueue extends Command {
  public ClearQueue() {
    this.name = "clearqueue";
    this.aliases = new String[]{"clearqueue", "clearq", "clear"};
    this.help = "Clears the track queue.";
  }

  // Clears the track queue
  @Override
  protected void execute(CommandEvent ce) {
    Settings.deleteInvoke(ce);

    GuildVoiceState userVoiceState = ce.getMember().getVoiceState();
    GuildVoiceState botVoiceState = ce.getGuild().getSelfMember().getVoiceState();

    boolean userInVoiceChannel = ce.getMember().getVoiceState().inVoiceChannel();
    boolean userInSameVoiceChannel = userVoiceState.getChannel().equals(botVoiceState.getChannel());

    if (userInVoiceChannel) {
      if (userInSameVoiceChannel) {
        clearTrackQueue(ce);
      } else {
        ce.getChannel().sendMessage("User not in the same voice channel.").queue();
      }
    } else {
      ce.getChannel().sendMessage("User not in a voice channel.").queue();
    }
  }

  // Clears the track queue
  private void clearTrackQueue(CommandEvent ce) {
    AudioScheduler audioScheduler = PlayerManager.getINSTANCE().getPlaybackManager(ce.getGuild()).audioScheduler;

    // Storage objects to access
    ArrayList<AudioTrack> trackQueue = audioScheduler.getTrackQueue();
    ArrayList<String> requesterList = audioScheduler.getRequesterList();

    trackQueue.clear();
    requesterList.clear();

    StringBuilder clearQueueConfirmation = new StringBuilder();
    clearQueueConfirmation.append("**Queue Clear:** [").append(ce.getAuthor().getAsTag()).append("]");
    ce.getChannel().sendMessage(clearQueueConfirmation).queue();
  }
}