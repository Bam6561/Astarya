package commands.promotion;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import commands.owner.Settings;
import net.dv8tion.jda.api.EmbedBuilder;

public class DungeonArchives extends Command {
  public DungeonArchives() {
    this.name = "dungeonarchives";
    this.aliases = new String[]{"dungeonarchives, dainvite"};
    this.help = "Discord advertisement for Dungeon Archives";
  }

  @Override
  protected void execute(CommandEvent ce) {
    Settings.deleteInvoke(ce);
    EmbedBuilder display = new EmbedBuilder();
    display.setTitle("__📜Dungeon Archives📜__");
    display.setDescription("""
        📣Ark Mobile Documentation & Reference Server\s
        ```"Knowledge is a treasure, but practice is the key to it."```\s
        ⚔Dungeon\s
        🥚Breeding\s
        🛠Breeding\s
        🦖Taming\s
        ⚖Kibble Weight\s
        📈Player/Creature XP\s
        🎮How To Set Up An Unofficial Server\s
        📱Emote Servers\s
        ```Additional contributors welcome.```""");
    display.addField("Discord:", "https://discord.gg/zw9jNCQ", false);
    display.setImage("https://cdn.discordapp.com/attachments/721939884235030558/758767568977788998/"
        + "BamMadeThisGifForDungeonArchivesIfYouSeeThisAnywhereElseItsStolen.gif");
    Settings.sendEmbed(ce, display);
  }
}
