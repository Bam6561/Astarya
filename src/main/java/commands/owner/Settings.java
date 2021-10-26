package commands.owner;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

public class Settings extends Command {
  private static String prefix;
  private static String alternativePrefix;
  private static boolean deleteInvoke = false;
  private static boolean embedDecay = false;
  private static int embedDecayTime = 30;
  private static boolean moderatePotentialPhishing = true;

  public Settings(String prefix, String alternativePrefix) {
    this.name = "settings";
    this.aliases = new String[]{"settings", "config"};
    this.arguments = "[1]Setting [2]True/False";
    this.help = "Provides information on the bot's settings.";
    this.prefix = prefix;
    this.alternativePrefix = alternativePrefix;
  }

  @Override
  protected void execute(CommandEvent ce) {
    Settings.deleteInvoke(ce);
    String[] args = ce.getMessage().getContentRaw().split("\\s"); // Parse message for arguments
    int arguments = args.length;
    switch (arguments) {
      case 1 -> { // Display main menu
        EmbedBuilder display = new EmbedBuilder();
        display.setTitle("__Settings__");
        display.setDescription("**Prefix:** `" + prefix + "` \n**AlternatePrefix:** `" + alternativePrefix +"`"
            + "\n**DeleteInvoke**: `" + deleteInvoke + "`" + "\n**EmbedDecay:** `" + embedDecay + "`"
            + "\n**EmbedDecayTime:** `" + embedDecayTime + "`s"
            + "\n**ModeratePotentialPhishing:** `" +moderatePotentialPhishing +"`");
        sendEmbed(ce, display);
      }
      case 3 -> { // Change settings
        if (ce.getMember().isOwner()) {
          String setting = args[1].toLowerCase();
          switch (setting) {
            case "deleteinvoke" -> setDeleteInvokeSetting(ce, args[2]);
            case "embeddecay" -> setEmbedDecaySetting(ce, args[2]);
            case "embeddecaytime" -> setEmbedDecayTimeSetting(ce, args[2]);
            case "moderatepotentialphishing" -> setModeratePotentialPhishingSetting(ce, args[2]);
            default -> ce.getChannel().sendMessage("Setting not found.").queue();
          }
        } else {
          ce.getChannel().sendMessage("You must be the server owner to change the bot's settings.").queue();
        }
      }
      // Invalid arguments
      default -> ce.getChannel().sendMessage("Invalid number of arguments.").queue();
    }
  }

  public static void deleteInvoke(CommandEvent ce) {
    if (deleteInvoke) {
      ce.getMessage().delete().queue();
    }
  }

  private void setDeleteInvokeSetting(CommandEvent ce, String change) {
    String proposedChange = change.toLowerCase();
    if (proposedChange.equals("true") || proposedChange.equals("false")) {
      deleteInvoke = Boolean.parseBoolean(proposedChange);
      ce.getChannel().sendMessage("DeleteInvoke has been set to `" + getDeleteInvoke() + "`.").queue();
    } else {
      ce.getChannel().sendMessage("You must specify true or false.").queue();
    }
  }

  private void setEmbedDecaySetting(CommandEvent ce, String change) {
    change = change.toLowerCase();
    if (change.equals("true") || change.equals("false")) {
      embedDecay = Boolean.parseBoolean(change);
      ce.getChannel().sendMessage("EmbedDecay has been set to `" + getEmbedDecay() + "`.").queue();
    } else {
      ce.getChannel().sendMessage("You must specify true or false.").queue();
    }
  }

  private void setEmbedDecayTimeSetting(CommandEvent ce, String change) {
    try { // Ensure argument is an integer
      int proposedChange = Integer.parseInt(change);
      if (proposedChange >= 15 && proposedChange <= 120) { // Between the range 15 - 120
        embedDecayTime = proposedChange;
        ce.getChannel().sendMessage("EmbedDecayTime has been set to `" + getEmbedDecayTime() + "`s.").queue();
      } else { // Outside of range 15 - 120
        ce.getChannel().sendMessage("You must provide a number between 15 - 120.").queue();
      }
    } catch (NumberFormatException error) { // Input mismatch
      ce.getChannel().sendMessage("You must provide a number between 15 - 120.").queue();
    }
  }

  private void setModeratePotentialPhishingSetting(CommandEvent ce, String change) {
    String proposedChange = change.toLowerCase();
    if (proposedChange.equals("true") || proposedChange.equals("false")) {
      moderatePotentialPhishing = Boolean.parseBoolean(proposedChange);
      ce.getChannel().sendMessage("ModeratePotentialPhishing has been set to `" + getModeratePotentialPhishing()
          + "`.").queue();
    } else {
      ce.getChannel().sendMessage("You must specify true or false.").queue();
    }
  }

  public static void sendEmbed(CommandEvent ce, EmbedBuilder display) {
    display.setColor(0x80000f);
    display.setFooter(ce.getMember().getUser().getAsTag());
    display.setTimestamp(Instant.now());
    ce.getChannel().sendTyping().queue();
    Settings.embedDecay(ce, display);
  }

  // Embeds auto delete after a period of time
  public static void embedDecay(CommandEvent ce, EmbedBuilder display) {
    if (embedDecay) {
      ce.getChannel().sendMessage(display.build()).complete().delete().queueAfter(embedDecayTime, TimeUnit.SECONDS);
    } else {
      ce.getChannel().sendMessage(display.build()).queue();
    }
  }

  public static String getPrefix() {
    return Settings.prefix;
  }

  private boolean getDeleteInvoke() {
    return Settings.deleteInvoke;
  }

  private boolean getEmbedDecay() {
    return Settings.embedDecay;
  }

  private int getEmbedDecayTime() {
    return Settings.embedDecayTime;
  }

  public static boolean getModeratePotentialPhishing() {
    return Settings.moderatePotentialPhishing;
  }
}
