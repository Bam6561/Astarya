package me.dannynguyen.astarya.commands.owner;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import me.dannynguyen.astarya.enums.BotMessage;
import net.dv8tion.jda.api.EmbedBuilder;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

/**
 * Settings is a command invocation that provides information on
 * the bot's settings and provides the option to change them.
 *
 * @author Danny Nguyen
 * @version 1.8.1
 * @since 1.0
 */
public class Settings extends Command {
  private static String prefix;
  private static String alternativePrefix;
  private static boolean deleteInvoke = false;
  private static boolean embedDecay = false;
  private static int embedDecayTime = 30;
  private static boolean embedMediaLinks = true;

  public Settings(String prefix, String alternativePrefix) {
    this.name = "settings";
    this.aliases = new String[]{"settings", "config"};
    this.arguments = "[0]MainMenu [1]Setting [2]True/False";
    this.help = "Provides information on the bot's settings.";
    Settings.prefix = prefix;
    Settings.alternativePrefix = alternativePrefix;
    this.ownerCommand = true;
  }

  /**
   * Either sends an embed containing all settings and their boolean values or
   * allows the user to change the settings based on the number of parameters provided.
   *
   * @param ce command event
   */
  @Override
  protected void execute(CommandEvent ce) {
    Settings.deleteInvoke(ce);

    String[] parameters = ce.getMessage().getContentRaw().split("\\s");
    int numberOfParameters = parameters.length - 1;

    switch (numberOfParameters) {
      case 0 -> sendSettingsMenu(ce);
      case 2 -> interpretSettingsMenuChange(ce, parameters);
      default -> ce.getChannel().sendMessage(BotMessage.Failure.INVALID_NUMBER_OF_PARAMETERS.text).queue();
    }
  }

  /**
   * Sends an embed containing the bot's settings' boolean values.
   *
   * @param ce command event
   */
  private void sendSettingsMenu(CommandEvent ce) {
    EmbedBuilder display = new EmbedBuilder();
    display.setAuthor("Settings");
    display.setDescription("**Prefix:** `" + prefix +
        "` \n**AlternatePrefix:** `" + alternativePrefix + "`"
        + "\n**DeleteInvoke**: `" + deleteInvoke +
        "`" + "\n**EmbedDecay:** `" + embedDecay + "`"
        + "\n**EmbedDecayTime:** `" + embedDecayTime + "`s"
        + "\n**EmbedMediaLinks:** `" + embedMediaLinks + "`");
    sendEmbed(ce, display);
  }

  /**
   * Either changes the delete invoke, embed decay, embed decay time, or embed media links setting.
   *
   * @param ce         command event
   * @param parameters user provided parameters
   */
  private void interpretSettingsMenuChange(CommandEvent ce, String[] parameters) {
    String settingType = parameters[1].toLowerCase();

    switch (settingType) {
      case "deleteinvoke" -> setDeleteInvokeSetting(ce, parameters[2].toLowerCase());
      case "embeddecay" -> setEmbedDecaySetting(ce, parameters[2].toLowerCase());
      case "embeddecaytime" -> setEmbedDecayTimeSetting(ce, parameters[2]);
      case "embedmedialinks" -> setEmbedMediaLinksSetting(ce, parameters[2].toLowerCase());
      default -> ce.getChannel().sendMessage(Failure.SETTING_NOT_FOUND.text).queue();
    }
  }

  /**
   * Changes the deleteinvoke's setting to true or false.
   *
   * @param ce            command event
   * @param settingChange the boolean value to be changed to
   */
  private void setDeleteInvokeSetting(CommandEvent ce, String settingChange) {
    boolean settingChangeIsBoolean = (settingChange.equals("true")) || (settingChange.equals("false"));
    if (settingChangeIsBoolean) {
      setDeleteInvoke(Boolean.parseBoolean(settingChange));
      ce.getChannel().sendMessage("DeleteInvoke has been set to `" + getDeleteInvoke() + "`.").queue();
    } else {
      ce.getChannel().sendMessage(Failure.SPECIFY_TRUE_FALSE.text).queue();
    }
  }

  /**
   * Changes the embeddecay's setting to true or false.
   *
   * @param ce            command event
   * @param settingChange the boolean value to be changed to
   */
  private void setEmbedDecaySetting(CommandEvent ce, String settingChange) {
    boolean settingChangeIsBoolean = (settingChange.equals("true")) || (settingChange.equals("false"));
    if (settingChangeIsBoolean) {
      setEmbedDecay(Boolean.parseBoolean(settingChange));
      ce.getChannel().sendMessage("EmbedDecay has been set to `" + getEmbedDecay() + "`.").queue();
    } else {
      ce.getChannel().sendMessage(Failure.SPECIFY_TRUE_FALSE.text).queue();
    }
  }

  /**
   * Changes the embeddecaytime's setting to an integer value.
   *
   * @param ce            command event
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
        ce.getChannel().sendMessage(Failure.SETTINGS_EMBED_DECAY_RANGE.text).queue();
      }
    } catch (NumberFormatException e) {
      ce.getChannel().sendMessage(Failure.SETTINGS_EMBED_DECAY_RANGE.text).queue();
    }
  }

  /**
   * Changes the embedmedia links setting to true or false.
   *
   * @param ce            command event
   * @param settingChange the boolean value to be changed to
   */
  private void setEmbedMediaLinksSetting(CommandEvent ce, String settingChange) {
    settingChange = settingChange.toLowerCase();
    boolean settingChangeIsBoolean = (settingChange.equals("true")) || (settingChange.equals("false"));
    if (settingChangeIsBoolean) {
      setEmbedMediaLinks(Boolean.parseBoolean(settingChange));
      ce.getChannel().sendMessage("EmbedMediaLinks has been set to `" + getEmbedMediaLinks() + "`.").queue();
    } else {
      ce.getChannel().sendMessage(Failure.SPECIFY_TRUE_FALSE.text).queue();
    }
  }

  /**
   * Deletes users' command invocations.
   *
   * @param ce command event
   */
  public static void deleteInvoke(CommandEvent ce) {
    if (deleteInvoke) {
      ce.getMessage().delete().queue();
    }
  }

  /**
   * Sends a pre-set embed configuration into the text channel.
   *
   * @param ce      command event
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
   * @param ce      command event
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

  public static boolean getEmbedMediaLinks() {
    return Settings.embedMediaLinks;
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

  private void setDeleteInvoke(boolean deleteInvoke) {
    Settings.deleteInvoke = deleteInvoke;
  }

  private void setEmbedDecay(boolean embedDecay) {
    Settings.embedDecay = embedDecay;
  }

  private void setEmbedDecayTime(int embedDecayTime) {
    Settings.embedDecayTime = embedDecayTime;
  }

  private void setEmbedMediaLinks(boolean embedMediaLinks) {
    Settings.embedMediaLinks = embedMediaLinks;
  }

  private enum Failure {
    SETTING_NOT_FOUND("Setting not found."),
    SPECIFY_TRUE_FALSE("Provide true or false."),
    SETTINGS_EMBED_DECAY_RANGE("Provide between 15 - 120 seconds.");

    public final String text;

    Failure(String text) {
      this.text = text;
    }
  }
}
