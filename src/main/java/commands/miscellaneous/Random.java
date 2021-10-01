package commands.miscellaneous;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import commands.owner.Settings;
import net.dv8tion.jda.api.EmbedBuilder;

public class Random extends Command {
  public Random() {
    this.name = "random";
    this.aliases = new String[]{"random"};
    this.help = "Provides an out of context screenshot.";
  }

  @Override
  protected void execute(CommandEvent ce) {
    Settings.deleteInvoke(ce);
    EmbedBuilder display = new EmbedBuilder();
    display.setDescription("Random images directory is currently empty and being worked on.");
    display.setImage(randomLink(links)); // Retrieve random link from image archive
    Settings.sendEmbed(ce, display);
  }

  public String randomLink(String[] links) {
    java.util.Random rand = new java.util.Random();
    int num = rand.nextInt(links.length);
    return links[num];
  }

  private final String[] links = {"https://www.google.com/url?sa=i&url=https%3A%2F%2Fwww.istockphoto.com%2Fphotos%2Fplaceholder-image&psig=AOvVaw11STsGjvdke5sfpuoKYcpF&ust=1633135401647000&source=images&cd=vfe&ved=0CAsQjRxqFwoTCMiwjc79p_MCFQAAAAAdAAAAABAJ"};
}
