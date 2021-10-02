package commands.audio;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import commands.audio.managers.PlayerManager;
import commands.owner.Settings;

public class Play extends Command {

  public Play() {
    this.name = "play";
    this.aliases = new String[]{"play", "p"};
    this.help = "Adds a song to the queue.";
  }

  @Override
  protected void execute(CommandEvent ce) {
    Settings.deleteInvoke(ce);
    String[] args = ce.getMessage().getContentRaw().split("\\s"); // Parse message for arguments
    int arguments = args.length;
    switch (arguments) {
      case 1 -> {
        ce.getChannel().sendMessage("Invalid number of arguments.").queue();
      }
      case 2 -> {
        PlayerManager.getINSTANCE().createAudioTrack(ce, args[1]);
      }
      default -> {
        StringBuilder searchQuery = new StringBuilder();
        for (int i = 1; i < arguments; i++) {
          searchQuery.append(args[i]);
        }
        String youtubeSearchQuery = "ytsearch:" + String.join(" ", searchQuery);
        PlayerManager.getINSTANCE().createAudioTrack(ce, youtubeSearchQuery);
      }
    }
  }
}
