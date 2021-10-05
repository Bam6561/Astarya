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
    this.help = "Provides a list of audio tracks queued.";
  }

  @Override
  protected void execute(CommandEvent ce) {
    Settings.deleteInvoke(ce);
    String[] args = ce.getMessage().getContentRaw().split("\\s"); // Parse message for arguments
    int arguments = args.length;
    int queuePage = 0;
    switch (arguments) {
      case 1 -> { // First page
        PlayerManager.getINSTANCE().getPlaybackManager(ce.getGuild()).audioScheduler.getQueue(ce, queuePage);
      }
      case 2 -> {
        try { // Custom page
          PlayerManager.getINSTANCE().getPlaybackManager(ce.getGuild())
              .audioScheduler.getQueue(ce, Integer.parseInt(args[1]) - 1);
        } catch (NumberFormatException error) { // Invalid argument
          ce.getChannel().sendMessage("Page number must be a number.").queue();
        }
      } // Invalid arguments
      default -> ce.getChannel().sendMessage("Invalid number of arguments.").queue();
    }
  }
}