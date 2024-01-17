package commands.audio;

import astarya.Text;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import commands.audio.managers.AudioScheduler;
import commands.audio.managers.PlayerManager;
import commands.audio.objects.TrackQueueIndex;
import commands.audio.utility.TimeConversion;
import commands.owner.Settings;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.GuildVoiceState;

import java.util.ArrayList;

/**
 * Return is a command invocation that either displays a stack of skipped tracks
 * and provides an option to return a recently skipped track to the track queue.
 *
 * @author Danny Nguyen
 * @version 1.7.8
 * @since 1.5.2
 */
public class Return extends Command {
  public Return() {
    this.name = "return";
    this.aliases = new String[]{"return", "ret"};
    this.arguments = "[0]RecentlySkipped [1]SkippedStackNumber";
    this.help = "Returns a recently skipped track to the track queue.";
  }

  /**
   * Checks if the user is in the same voice channel as the bot to read a return command request.
   *
   * @param ce command event
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
        interpretReturnTrackRequest(ce);
      } else {
        ce.getChannel().sendMessage(Text.NOT_IN_SAME_VC.value()).queue();
      }
    } catch (NullPointerException e) {
      ce.getChannel().sendMessage(Text.NOT_IN_VC.value()).queue();
    }
  }

  /**
   * Either displays the stack of skipped tracks or returns a skipped track from the stack back to the track queue.
   *
   * @param ce command event
   * @throws NumberFormatException user provided non-integer value
   */
  private void interpretReturnTrackRequest(CommandEvent ce) {
    String[] parameters = ce.getMessage().getContentRaw().split("\\s");
    int numberOfParameters = parameters.length - 1;

    switch (numberOfParameters) {
      case 0 -> sendSkippedTracksStack(ce);
      case 1 -> {
        try {
          int returnStackIndex = Integer.parseInt(parameters[1]);
          processReturnTrackRequest(ce, returnStackIndex);
        } catch (NumberFormatException e) {
          ce.getChannel().sendMessage("Specify what stack number to be returned with an integer.").queue();
        }
      }
      default -> ce.getChannel().sendMessage("Invalid number of parameters.").queue();
    }
  }

  /**
   * Sends an embed containing recently skipped tracks.
   *
   * @param ce command event
   */
  private void sendSkippedTracksStack(CommandEvent ce) {
    ArrayList<TrackQueueIndex> skippedTracks = PlayerManager.getINSTANCE().getPlaybackManager(ce.getGuild()).audioScheduler.getSkippedTracks();

    boolean skippedTracksStackNotEmpty = !skippedTracks.isEmpty();
    if (skippedTracksStackNotEmpty) {
      EmbedBuilder display = new EmbedBuilder();
      display.setAuthor("Recently Skipped");
      display.addField("**Tracks:**", (buildSkippedTracksStackPage(skippedTracks)), false);
      Settings.sendEmbed(ce, display);
    } else {
      ce.getChannel().sendMessage("There are no recently skipped tracks.").queue();
    }
  }

  /**
   * Checks if user provided integer within range of skipped tracks
   * stack before returning a skipped track to the track queue.
   *
   * @param ce                      command event
   * @param skippedTracksStackIndex track index in the skipped tracks stack to be returned
   * @throws IndexOutOfBoundsException user provided index out of range of skipped tracks stack
   */
  private void processReturnTrackRequest(CommandEvent ce, int skippedTracksStackIndex) {
    try {
      AudioScheduler audioScheduler = PlayerManager.getINSTANCE().getPlaybackManager(ce.getGuild()).audioScheduler;
      ArrayList<TrackQueueIndex> skippedTracksStack = audioScheduler.getSkippedTracks();

      // Displayed index to users are different from data index, so subtract 1
      AudioTrack skippedTrack = skippedTracksStack.get(skippedTracksStackIndex - 1).getAudioTrack();

      returnSkippedTrack(ce, skippedTracksStackIndex, audioScheduler, skippedTracksStack, skippedTrack);
      sendReturnConfirmation(ce, skippedTrack);
    } catch (IndexOutOfBoundsException e) {
      ce.getChannel().sendMessage(Text.INVALID_QUEUE_NUMBER.value()).queue();
    }
  }

  /**
   * Creates the display text to represent the skipped tracks stack.
   * <p>
   * The skipped tracks stack can only contain up to 10 tracks. As another
   * skipped track is added to the stack, all previously existing tracks indices
   * are incremented by 1. After exceeding the amount, the least recent track is removed.
   * </p>
   *
   * @param skippedTracks stack of skipped tracks
   * @return string representing the stack of skipped tracks
   */
  private String buildSkippedTracksStackPage(ArrayList<TrackQueueIndex> skippedTracks) {
    StringBuilder skippedTracksStackPage = new StringBuilder();
    for (int i = 0; i < skippedTracks.size(); i++) {
      String trackDuration = TimeConversion.convert(skippedTracks.get(i).getAudioTrack().getDuration());
      skippedTracksStackPage.append("**[").append(i + 1).append("]** `").
          append(skippedTracks.get(i).getAudioTrack().getInfo().title)
          .append("` {*").append(trackDuration).append("*} ").append("\n");
    }
    return skippedTracksStackPage.toString();
  }

  /**
   * Returns a track from recently skipped tracks stack.
   *
   * @param ce                      command event
   * @param skippedTracksStackIndex index of the skipped tracks stack to be returned
   * @param audioScheduler          bot's audio scheduler
   * @param skippedTracksStack      stack of skipped tracks
   * @param skippedTrack            chosen skipped track to be returned
   */
  private void returnSkippedTrack(CommandEvent ce, int skippedTracksStackIndex, AudioScheduler audioScheduler,
                                  ArrayList<TrackQueueIndex> skippedTracksStack, AudioTrack skippedTrack) {
    String requester = "[" + ce.getAuthor().getAsTag() + "]";
    audioScheduler.queue(skippedTrack, requester);
    skippedTracksStack.remove(skippedTracksStackIndex - 1);
  }

  /**
   * Sends confirmation the skipped track was returned to the track queue.
   *
   * @param ce           command event
   * @param skippedTrack chosen skipped track to be returned to the track queue
   */
  private void sendReturnConfirmation(CommandEvent ce, AudioTrack skippedTrack) {
    StringBuilder returnTrackConfirmation = new StringBuilder();
    String trackDuration = TimeConversion.convert(skippedTrack.getDuration());
    returnTrackConfirmation.append("**Returned:** `")
        .append(skippedTrack.getInfo().title)
        .append("` {*").append(trackDuration).append("*} ")
        .append("[").append(ce.getAuthor().getAsTag()).append("]");
    ce.getChannel().sendMessage(returnTrackConfirmation).queue();
  }
}
