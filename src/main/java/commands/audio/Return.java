package commands.audio;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import commands.audio.managers.AudioScheduler;
import commands.audio.managers.PlayerManager;
import commands.owner.Settings;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.GuildVoiceState;

import java.util.ArrayList;

/**
 * Return is a command invocation that either displays a stack of skipped tracks
 * and provides an option to return a recently skipped track to the queue.
 *
 * @author Danny Nguyen
 * @version 1.6.1
 * @since 1.5.2
 */
public class Return extends Command {
  public Return() {
    this.name = "return";
    this.aliases = new String[]{"return", "ret"};
    this.arguments = "[0]RecentlySkipped [1]SkippedStackNumber";
    this.help = "Returns a recently skipped track to the queue.";
  }

  /**
   * Determines whether the user is in the same voice channel as the bot to process a return command request.
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
        parseReturnTrackRequest(ce);
      } else {
        ce.getChannel().sendMessage("User not in the same voice channel.").queue();
      }
    } catch (NullPointerException e) {
      ce.getChannel().sendMessage("User not in a voice channel.").queue();
    }
  }

  /**
   * Either displays the stack of skipped tracks or returns a skipped track from the stack back to the queue.
   *
   * @param ce object containing information about the command event
   * @throws NumberFormatException user provided non-integer value
   */
  private void parseReturnTrackRequest(CommandEvent ce) {
    // Parse message for arguments
    String[] arguments = ce.getMessage().getContentRaw().split("\\s");
    int numberOfArguments = arguments.length - 1;

    switch (numberOfArguments) {
      case 0 -> displaySkippedTracksStack(ce);
      case 1 -> {
        try {
          int returnStackIndex = Integer.parseInt(arguments[1]);
          returnTrackRequest(ce, returnStackIndex);
        } catch (NumberFormatException e) {
          ce.getChannel().sendMessage("Specify what stack number to be returned with an integer.").queue();
        }
      }
      default -> ce.getChannel().sendMessage("Invalid number of arguments.").queue();
    }
  }

  /**
   * Sends an embed containing recently skipped tracks.
   * <p>
   * The skipped tracks stack can only contain up to 10 tracks. As another
   * skipped track is added to the stack, all previously existing tracks indices
   * are incremented by 1. After exceeding the amount, the least recent track is removed.
   * </p>
   *
   * @param ce object containing information about the command event
   */
  private void displaySkippedTracksStack(CommandEvent ce) {
    AudioScheduler audioScheduler = PlayerManager.getINSTANCE().getPlaybackManager(ce.getGuild()).audioScheduler;
    ArrayList<AudioTrack> skippedStack = audioScheduler.getSkippedStack();

    boolean skippedStackNotEmpty = !skippedStack.isEmpty();
    if (skippedStackNotEmpty) {
      // Build skipped stack page
      StringBuilder skippedStackPage = new StringBuilder();
      for (int i = 0; i < skippedStack.size(); i++) {
        String trackDuration = longTimeConversion(skippedStack.get(i).getDuration());
        skippedStackPage.append("**[").append(i + 1).append("]** `").
            append(skippedStack.get(i).getInfo().title)
            .append("` {*").append(trackDuration).append("*} ").append("\n");
      }

      EmbedBuilder display = new EmbedBuilder();
      display.setTitle("__**Recently Skipped**__");
      display.addField("**Tracks:**", String.valueOf(skippedStackPage), false);
      Settings.sendEmbed(ce, display);
    } else {
      ce.getChannel().sendMessage("There are no recently skipped tracks.").queue();
    }
  }

  /**
   * Returns a track from recently skipped tracks stack.
   *
   * @param ce                object containing information about the command event
   * @param skippedStackIndex track index in the skipped tracks stack to be returned
   * @throws IndexOutOfBoundsException user provided index out of range of skipped tracks stack
   */
  private void returnTrackRequest(CommandEvent ce, int skippedStackIndex) {
    try {
      AudioScheduler audioScheduler = PlayerManager.getINSTANCE().getPlaybackManager(ce.getGuild()).audioScheduler;
      ArrayList<AudioTrack> skippedStack = audioScheduler.getSkippedStack();

      // Displayed index to users are different from data index, so subtract 1
      AudioTrack skippedTrack = skippedStack.get(skippedStackIndex - 1);

      // Return confirmation
      StringBuilder returnTrackConfirmation = new StringBuilder();
      String trackDuration = longTimeConversion(skippedTrack.getDuration());
      returnTrackConfirmation.append("**Returned:** `")
          .append(skippedTrack.getInfo().title)
          .append("` {*").append(trackDuration).append("*} ")
          .append("[").append(ce.getAuthor().getAsTag()).append("]");
      ce.getChannel().sendMessage(returnTrackConfirmation).queue();

      audioScheduler.queue(skippedTrack);
      audioScheduler.getSkippedStack().remove(skippedTrack);
    } catch (IndexOutOfBoundsException e) {
      ce.getChannel().sendMessage("Queue number does not exist.").queue();
    }
  }

  /**
   * Converts long duration to conventional readable time.
   *
   * @param longTime duration of the track in long
   * @return readable time format
   */
  private String longTimeConversion(long longTime) {
    long days = longTime / 86400000 % 30;
    long hours = longTime / 3600000 % 24;
    long minutes = longTime / 60000 % 60;
    long seconds = longTime / 1000 % 60;
    return (days == 0 ? "" : days < 10 ? "0" + days + ":" : days + ":") +
        (hours == 0 ? "" : hours < 10 ? "0" + hours + ":" : hours + ":") +
        (minutes == 0 ? "00:" : minutes < 10 ? "0" + minutes + ":" : minutes + ":") +
        (seconds == 0 ? "00" : seconds < 10 ? "0" + seconds : seconds + "");
  }
}
