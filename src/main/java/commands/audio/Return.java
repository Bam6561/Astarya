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

public class Return extends Command {
  public Return() {
    this.name = "return";
    this.aliases = new String[]{"return", "ret", "unskip"};
    this.arguments = "[0]skippedStack, [1]stackNumber";
    this.help = "Returns a recently skipped audio track to the queue.";
  }

  // Returns a recently skipped track to the queue
  @Override
  protected void execute(CommandEvent ce) {
    Settings.deleteInvoke(ce);

    GuildVoiceState userVoiceState = ce.getMember().getVoiceState();
    GuildVoiceState botVoiceState = ce.getGuild().getSelfMember().getVoiceState();

    boolean userInVoiceChannel = ce.getMember().getVoiceState().inVoiceChannel();
    boolean userInSameVoiceChannel = userVoiceState.getChannel().equals(botVoiceState.getChannel());

    if (userInVoiceChannel) {
      if (userInSameVoiceChannel) {
        parseReturnTrackRequest(ce);
      } else {
        ce.getChannel().sendMessage("User not in the same voice channel.").queue();
      }
    } else {
      ce.getChannel().sendMessage("User not in a voice channel.").queue();
    }
  }

  // Validates return track request before proceeding
  private void parseReturnTrackRequest(CommandEvent ce) {
    // Parse message for arguments
    String[] arguments = ce.getMessage().getContentRaw().split("\\s");
    int numberOfArguments = arguments.length - 1;

    switch (numberOfArguments) {
      case 0 -> displaySkippedStack(ce);

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

  // Sends an embed containing recently skipped tracks
  private void displaySkippedStack(CommandEvent ce) {
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

  // Returns a track from recently skipped stack
  private void returnTrackRequest(CommandEvent ce, int returnStackIndex) {
    try {
      AudioScheduler audioScheduler = PlayerManager.getINSTANCE().getPlaybackManager(ce.getGuild()).audioScheduler;
      ArrayList<AudioTrack> skippedStack = audioScheduler.getSkippedStack();

      // Displayed index to users are different from data index, so subtract 1
      AudioTrack skippedTrack = skippedStack.get(returnStackIndex - 1);

      // Sends embed confirmation
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

  // Converts long duration to conventional readable time
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
