package commands.owner;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

/**
 * Settings is a command invocation that provides information on
 * the bot's settings and provides the option to change them.
 *
 * @author Danny Nguyen
 * @version 1.6.6
 * @since 1.0
 */
public class Settings extends Command {
  private static String prefix;
  private static String alternativePrefix;
  private static boolean deleteInvoke = false;
  private static boolean embedDecay = false;
  private static int embedDecayTime = 30;
  private static boolean moderatePotentialPhishing = true;
  private static boolean embedTwitterLinks = true;

  public Settings(String prefix, String alternativePrefix) {
    this.name = "settings";
    this.aliases = new String[]{"settings", "config"};
    this.arguments = "[0]MainMenu [1]Setting [2]True/False";
    this.help = "Provides information on the bot's settings.";
    this.prefix = prefix;
    this.alternativePrefix = alternativePrefix;
    this.ownerCommand = true;
  }

  /**
   * Either sends an embed containing all settings and their boolean values or
   * allows the user to change the settings based on the number of parameters provided.
   *
   * @param ce object containing information about the command event
   */
  @Override
  protected void execute(CommandEvent ce) {
    Settings.deleteInvoke(ce);

    // Parse message for parameters
    String[] parameters = ce.getMessage().getContentRaw().split("\\s");
    int numberOfParameters = parameters.length - 1;

    switch (numberOfParameters) {
      case 0 -> sendSettingsMenu(ce);
      case 2 -> changeSettings(ce, parameters);
      default -> ce.getChannel().sendMessage("Invalid number of parameters.").queue();
    }
  }

  /**
   * Sends an embed containing the bot's settings' boolean values.
   *
   * @param ce object containing information about the command event
   */
  private void sendSettingsMenu(CommandEvent ce) {
    EmbedBuilder display = new EmbedBuilder();
    display.setAuthor("Settings");
    display.setDescription("**Prefix:** `" + prefix +
        "` \n**AlternatePrefix:** `" + alternativePrefix + "`"
        + "\n**DeleteInvoke**: `" + deleteInvoke +
        "`" + "\n**EmbedDecay:** `" + embedDecay + "`"
        + "\n**EmbedDecayTime:** `" + embedDecayTime + "`s"
        + "\n**ModeratePotentialPhishing:** `" + moderatePotentialPhishing + "`"
        + "\n**EmbedTwitterLinks:** `" +embedTwitterLinks + "`");
    sendEmbed(ce, display);
  }

  /**
   * Checks which setting type the user is attempting to change.
   *
   * @param ce        object containing information about the command event
   * @param parameters user provided parameters
   */
  private void changeSettings(CommandEvent ce, String[] parameters) {
    String settingType = parameters[1].toLowerCase();

    switch (settingType) {
      case "deleteinvoke" -> setDeleteInvokeSetting(ce, parameters[2]);
      case "embeddecay" -> setEmbedDecaySetting(ce, parameters[2]);
      case "embeddecaytime" -> setEmbedDecayTimeSetting(ce, parameters[2]);
      case "moderatepotentialphishing" -> setModeratePotentialPhishingSetting(ce, parameters[2]);
      case "embedtwitterlinks" -> setEmbedTwitterLinksSetting(ce,parameters[2]);
      default -> ce.getChannel().sendMessage("Setting not found.").queue();
    }
  }

  /**
   * Changes the delete invoke's setting to true or false.
   *
   * @param ce            object containing information about the command event
   * @param settingChange the boolean value to be changed to
   */
  private void setDeleteInvokeSetting(CommandEvent ce, String settingChange) {
    settingChange = settingChange.toLowerCase();
    boolean settingChangeIsBoolean = (settingChange.equals("true")) || (settingChange.equals("false"));
    if (settingChangeIsBoolean) {
      setDeleteInvoke(Boolean.parseBoolean(settingChange));
      ce.getChannel().sendMessage("DeleteInvoke has been set to `" + getDeleteInvoke() + "`.").queue();
    } else {
      ce.getChannel().sendMessage("Specify true or false.").queue();
    }
  }

  /**
   * Changes the embed decay's setting to true or false.
   *
   * @param ce            object containing information about the command event
   * @param settingChange the boolean value to be changed to
   */
  private void setEmbedDecaySetting(CommandEvent ce, String settingChange) {
    settingChange = settingChange.toLowerCase();
    boolean settingChangeIsBoolean = (settingChange.equals("true")) || (settingChange.equals("false"));
    if (settingChangeIsBoolean) {
      setEmbedDecay(Boolean.parseBoolean(settingChange));
      ce.getChannel().sendMessage("EmbedDecay has been set to `" + getEmbedDecay() + "`.").queue();
    } else {
      ce.getChannel().sendMessage("Specify true or false.").queue();
    }
  }

  /**
   * Changes the embed decay time's setting to true or false.
   *
   * @param ce            object containing information about the command event
   * @param settingChange the boolean value to be changed to
   * @throws NumberFormatException user provided non-integer value
   */
  private void setEmbedDecayTimeSetting(CommandEvent ce, String settingChange) {
    try {
      int timeValue = Integer.parseInt(settingChange);
      boolean validTimeValue = (timeValue >= 15) && (timeValue <= 120);
      if (validTimeValue) {
        setEmbedDecayTime(timeValue);
        ce.getChannel().sendMessage("EmbedDecayTime has been set to `" + getEmbedDecayTime() + "`s.").queue();
      } else {
        ce.getChannel().sendMessage("Specify an integer between 15 - 120.").queue();
      }
    } catch (NumberFormatException e) {
      ce.getChannel().sendMessage("Specify an integer between 15 - 120.").queue();
    }
  }

  /**
   * Changes the moderate potential phishing's setting to true or false.
   *
   * @param ce            object containing information about the command event
   * @param settingChange the boolean value to be changed to
   */
  private void setModeratePotentialPhishingSetting(CommandEvent ce, String settingChange) {
    settingChange = settingChange.toLowerCase();
    boolean settingChangeIsBoolean = (settingChange.equals("true")) || (settingChange.equals("false"));
    if (settingChangeIsBoolean) {
      setModeratePotentialPhishing(Boolean.parseBoolean(settingChange));
      ce.getChannel().sendMessage("ModeratePotentialPhishing has been set " +
          "to `" + getModeratePotentialPhishing() + "`.").queue();
    } else {
      ce.getChannel().sendMessage("Specify true or false.").queue();
    }
  }

  /**
   * Changes the embed Twitter links setting to true or false.
   *
   * @param ce            object containing information about the command event
   * @param settingChange the boolean value to be changed to
   */
  private void setEmbedTwitterLinksSetting(CommandEvent ce, String settingChange) {
    settingChange = settingChange.toLowerCase();
    boolean settingChangeIsBoolean = (settingChange.equals("true")) || (settingChange.equals("false"));
    if (settingChangeIsBoolean) {
      setEmbedTwitterLinks(Boolean.parseBoolean(settingChange));
      ce.getChannel().sendMessage("EmbedTwitterLinks has been set " +
          "to `" + getEmbedTwitterLinks() + "`.").queue();
    } else {
      ce.getChannel().sendMessage("Specify true or false.").queue();
    }
  }

  /**
   * Deletes users' command invocations.
   *
   * @param ce object containing information about the command event
   */
  public static void deleteInvoke(CommandEvent ce) {
    if (deleteInvoke) {
      ce.getMessage().delete().queue();
    }
  }

  /**
   * Sends a pre-set embed configuration into the text channel.
   *
   * @param ce      object containing information about the command event
   * @param display object representing the embed
   */
  public static void sendEmbed(CommandEvent ce, EmbedBuilder display) {
    ce.getChannel().sendTyping().queue();
    display.setColor(0x006fb1);
    display.setFooter(ce.getMember().getUser().getAsTag());
    display.setTimestamp(Instant.now());
    Settings.embedDecay(ce, display);
  }

  /**
   * Automatically deletes embeds after an elapsed period of time.
   *
   * @param ce      object containing information about the command event
   * @param display object representing the embed
   */
  public static void embedDecay(CommandEvent ce, EmbedBuilder display) {
    if (embedDecay) {
      ce.getChannel().sendMessageEmbeds(display.build()).complete().
          delete().queueAfter(embedDecayTime, TimeUnit.SECONDS);
    } else {
      ce.getChannel().sendMessageEmbeds(display.build()).queue();
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

  public static boolean getEmbedTwitterLinks() { return Settings.embedTwitterLinks; }

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

  private void setEmbedTwitterLinks(boolean embedTwitterLinks) {
    this.embedTwitterLinks = embedTwitterLinks;
  }
}
