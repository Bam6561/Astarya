package commands.audio;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import commands.audio.managers.PlayerManager;
import commands.owner.Settings;

public class Queue extends Command {
  public Queue() {
    this.name = "queue";
    this.aliases = new String[]{"queue", "q"};
    this.arguments = "[0]queue, [1]pageNumber";
    this.help = "Provides a list of tracks queued.";
  }

  @Override
  protected void execute(CommandEvent ce) {
    Settings.deleteInvoke(ce);
    String[] args = ce.getMessage().getContentRaw().split("\\s"); // Parse message for arguments
    int arguments = args.length;
    int queuePage = 0;
    switch (arguments) {
      case 1 -> {
        PlayerManager.getINSTANCE().getPlaybackManager(ce.getGuild()).getAudioScheduler().getQueue(ce, queuePage);
      }
      case 2 -> {
        try {
          queuePage = Integer.parseInt(args[1])-1;
          PlayerManager.getINSTANCE().getPlaybackManager(ce.getGuild()).getAudioScheduler().getQueue(ce, queuePage);
        } catch (NumberFormatException error) {
          ce.getChannel().sendMessage("Page number must be a number.");
        }
      }
      default -> {
        ce.getChannel().sendMessage("Invalid number of arguments.").queue();
      }
    }

  }
}