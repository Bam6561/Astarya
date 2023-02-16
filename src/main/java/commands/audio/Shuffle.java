package commands.audio;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import commands.audio.managers.AudioScheduler;
import commands.audio.managers.PlayerManager;
import commands.owner.Settings;
import net.dv8tion.jda.api.entities.GuildVoiceState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class Shuffle extends Command {
  public Shuffle() {
    this.name = "shuffle";
    this.aliases = new String[]{"shuffle", "mix", "sh"};
    this.help = "Shuffles the queue.";
  }

  // Shuffles the track queue
  @Override
  protected void execute(CommandEvent ce) {
    Settings.deleteInvoke(ce);

    GuildVoiceState userVoiceState = ce.getMember().getVoiceState();
    GuildVoiceState botVoiceState = ce.getGuild().getSelfMember().getVoiceState();

    boolean userInVoiceChannel = ce.getMember().getVoiceState().inVoiceChannel();
    boolean userInSameVoiceChannel = userVoiceState.getChannel().equals(botVoiceState.getChannel());

    if (userInVoiceChannel) {
      if (userInSameVoiceChannel) {
        shuffleQueue(ce);
      } else {
        ce.getChannel().sendMessage("User not in the same voice channel.").queue();
      }
    } else {
      ce.getChannel().sendMessage("User not in a voice channel.").queue();
    }
  }

  // Shuffles the track queue
  public void shuffleQueue(CommandEvent ce) {
    AudioScheduler audioScheduler = PlayerManager.getINSTANCE().getPlaybackManager(ce.getGuild()).audioScheduler;

    // Storage objects to access
    ArrayList<AudioTrack> trackQueue = audioScheduler.getTrackQueue();
    ArrayList<String> requesterList = audioScheduler.getRequesterList();

    Random rand = new Random();
    for (int i = 0; i < trackQueue.size(); i++) {
      int indexSwitch = rand.nextInt(trackQueue.size());
      Collections.swap(trackQueue, i, indexSwitch);
      Collections.swap(requesterList, i, indexSwitch);
    }

    StringBuilder shuffleConfirmation = new StringBuilder();
    shuffleConfirmation.append("**Shuffle:** [").append(ce.getAuthor().getAsTag()).append("]");
    ce.getChannel().sendMessage(shuffleConfirmation).queue();
  }
}
