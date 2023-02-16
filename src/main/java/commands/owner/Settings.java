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
    this.ownerCommand = true;
  }

  @Override
  protected void execute(CommandEvent ce) {
    Settings.deleteInvoke(ce);

    // Parse message for arguments
    String[] arguments = ce.getMessage().getContentRaw().split("\\s");
    int numberOfArguments = arguments.length - 1;

    switch (numberOfArguments) {
      case 0 -> displaySettingsMenu(ce); // Display main menu
      case 2 -> changeSettings(ce, arguments);// Change settings
      default -> ce.getChannel().sendMessage("Invalid number of arguments.").queue(); // Invalid arguments
    }
  }

  // Sends an embed containing bot settings
  private void displaySettingsMenu(CommandEvent ce) {
    EmbedBuilder display = new EmbedBuilder();
    display.setTitle("__Settings__");
    display.setDescription("**Prefix:** `" + prefix +
        "` \n**AlternatePrefix:** `" + alternativePrefix + "`"
        + "\n**DeleteInvoke**: `" + deleteInvoke +
        "`" + "\n**EmbedDecay:** `" + embedDecay + "`"
        + "\n**EmbedDecayTime:** `" + embedDecayTime + "`s"
        + "\n**ModeratePotentialPhishing:** `" + moderatePotentialPhishing + "`");

    sendEmbed(ce, display);
  }

  // Directs to setting type to be changed
  private void changeSettings(CommandEvent ce, String[] arguments) {
    String settingType = arguments[1].toLowerCase();

    switch (settingType) {
      case "deleteinvoke" -> setDeleteInvokeSetting(ce, arguments[2]);
      case "embeddecay" -> setEmbedDecaySetting(ce, arguments[2]);
      case "embeddecaytime" -> setEmbedDecayTimeSetting(ce, arguments[2]);
      case "moderatepotentialphishing" -> setModeratePotentialPhishingSetting(ce, arguments[2]);
      default -> ce.getChannel().sendMessage("Setting not found.").queue();
    }
  }

  // Changes delete invoke setting
  private void setDeleteInvokeSetting(CommandEvent ce, String settingChange) {
    settingChange = settingChange.toLowerCase();
    boolean settingChangeIsBoolean = (settingChange.equals("true")) || (settingChange.equals("false"));
    if (settingChangeIsBoolean) {
      setDeleteInvoke(Boolean.parseBoolean(settingChange));
      ce.getChannel().sendMessage("DeleteInvoke has been set to `" + getDeleteInvoke() + "`.").queue();
    } else { // Non-boolean value
      ce.getChannel().sendMessage("Specify true or false.").queue();
    }
  }

  // Changes embed decay setting
  private void setEmbedDecaySetting(CommandEvent ce, String settingChange) {
    settingChange = settingChange.toLowerCase();
    boolean settingChangeIsBoolean = (settingChange.equals("true")) || (settingChange.equals("false"));
    if (settingChangeIsBoolean) {
      setEmbedDecay(Boolean.parseBoolean(settingChange));
      ce.getChannel().sendMessage("EmbedDecay has been set to `" + getEmbedDecay() + "`.").queue();
    } else { // Non-boolean value
      ce.getChannel().sendMessage("Specify true or false.").queue();
    }
  }

  // Changes embed decay time setting
  private void setEmbedDecayTimeSetting(CommandEvent ce, String settingChange) {
    try { // Ensure argument is an integer
      int timeValue = Integer.parseInt(settingChange);
      boolean validTimeValue = (timeValue >= 15) && (timeValue <= 120);
      if (validTimeValue) {
        setEmbedDecayTime(timeValue);
        ce.getChannel().sendMessage("EmbedDecayTime has been set to `" + getEmbedDecayTime() + "`s.").queue();
      } else {
        ce.getChannel().sendMessage("Specify an integer between 15 - 120.").queue();
      }

    } catch (NumberFormatException error) { // Non-integer value
      ce.getChannel().sendMessage("Specify an integer between 15 - 120.").queue();
    }
  }

  // Changes moderate potential phishing setting
  private void setModeratePotentialPhishingSetting(CommandEvent ce, String settingChange) {
    settingChange = settingChange.toLowerCase();
    boolean settingChangeIsBoolean = (settingChange.equals("true")) || (settingChange.equals("false"));
    if (settingChangeIsBoolean) {
      setModeratePotentialPhishing(Boolean.parseBoolean(settingChange));
      ce.getChannel().sendMessage("ModeratePotentialPhishing has been set " +
          "to `" + getModeratePotentialPhishing() + "`.").queue();
    } else { // Non-boolean value
      ce.getChannel().sendMessage("Specify true or false.").queue();
    }
  }

  // Deletes command invocations
  public static void deleteInvoke(CommandEvent ce) {
    if (deleteInvoke) {
      ce.getMessage().delete().queue();
    }
  }

  // Sends a pre-set embed configuration into message channel
  public static void sendEmbed(CommandEvent ce, EmbedBuilder display) {
    ce.getChannel().sendTyping().queue();

    display.setColor(0x80000f);
    display.setFooter(ce.getMember().getUser().getAsTag());
    display.setTimestamp(Instant.now());

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

  // Get and set various variables
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

  private void setDeleteInvoke(boolean deleteInvoke) {
    this.deleteInvoke = deleteInvoke;
  }

  private void setEmbedDecay(boolean embedDecay) {
    this.embedDecay = embedDecay;
  }

  private void setEmbedDecayTime(int embedDecayTime) {
    this.embedDecayTime = embedDecayTime;
  }

  private void setModeratePotentialPhishing(boolean moderatePotentialPhishing) {
    this.moderatePotentialPhishing = moderatePotentialPhishing;
  }
}
