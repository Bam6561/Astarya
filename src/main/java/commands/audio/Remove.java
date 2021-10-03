package commands.audio;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import commands.audio.managers.PlayerManager;
import commands.owner.Settings;

public class Remove extends Command {
  public Remove() {
    this.name = "remove";
    this.aliases = new String[]{"remove", "takeout", "nvm"};
    this.arguments = "[1]QueueNumber";
    this.help = "Removes an audio track from the queue.";
  }

  @Override
  protected void execute(CommandEvent ce) {
    Settings.deleteInvoke(ce);
    if (ce.getGuild().getSelfMember().getVoiceState().inVoiceChannel()) {
      if ((ce.getMember().getVoiceState().getChannel())
          .equals((ce.getGuild().getSelfMember().getVoiceState().getChannel()))) {
        String[] args = ce.getMessage().getContentRaw().split("\\s"); // Parse message for arguments
        int arguments = args.length;
        if (arguments == 2) {
          try { // Remove queue entry
            PlayerManager.getINSTANCE().getPlaybackManager(ce.getGuild()).
                getAudioScheduler().removeQueueEntry(ce, Integer.parseInt(args[1]));
          } catch (NumberFormatException e) { //
            ce.getChannel().sendMessage("Argument must be an integer.").queue();
          }
        } else {// Invalid argument
          ce.getChannel().sendMessage("Invalid number of arguments.").queue();
        }
      } else {
        ce.getChannel().sendMessage("I'm not in the same voice channel.").queue();
      }
    } else {
      ce.getChannel().sendMessage("I'm not in a voice channel yet.").queue();
    }
  }
}
