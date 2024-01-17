package commands.owner;

import astarya.Text;
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
 * @version 1.7.9
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
    this.prefix = prefix;
    this.alternativePrefix = alternativePrefix;
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
      default -> ce.getChannel().sendMessage(Text.INVALID_NUMBER_OF_PARAMS.value()).queue();
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
   * Either changes the deleteinvoke, embeddecay, embeddecaytime, or embedmedialinks setting.
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
      default -> ce.getChannel().sendMessage("Setting not found.").queue();
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
      ce.getChannel().sendMessage("Specify true or false.").queue();
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
      ce.getChannel().sendMessage("Specify true or false.").queue();
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
        ce.getChannel().sendMessage("Specify an integer between 15 - 120.").queue();
      }
    } catch (NumberFormatException e) {
      ce.getChannel().sendMessage("Specify an integer between 15 - 120.").queue();
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
      ce.getChannel().sendMessage("EmbedMediaLinks has been set " +
          "to `" + getEmbedMediaLinks() + "`.").queue();
    } else {
      ce.getChannel().sendMessage("Specify true or false.").queue();
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
    this.deleteInvoke = deleteInvoke;
  }

  private void setEmbedDecay(boolean embedDecay) {
    this.embedDecay = embedDecay;
  }

  private void setEmbedDecayTime(int embedDecayTime) {
    this.embedDecayTime = embedDecayTime;
  }

  private void setEmbedMediaLinks(boolean embedMediaLinks) {
    this.embedMediaLinks = embedMediaLinks;
  }
}
