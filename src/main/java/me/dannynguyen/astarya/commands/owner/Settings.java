package me.dannynguyen.astarya.commands.owner;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import me.dannynguyen.astarya.enums.BotMessage;
import net.dv8tion.jda.api.EmbedBuilder;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Command invocation that provides information on
 * the bot's settings and provides the option to change them.
 *
 * @author Danny Nguyen
 * @version 1.8.14
 * @since 1.0
 */
public class Settings extends Command {
  /**
   * Command prefix.
   */
  private static String prefix;

  /**
   * Command alternate prefix.
   */
  private static String alternativePrefix;

  /**
   * If to delete command invocations.
   */
  private static boolean deleteInvoke = false;

  /**
   * If to delete embeds after a duration.
   */
  private static boolean embedDecay = false;

  /**
   * The duration to delete embeds after (in seconds).
   */
  private static int embedDecayTime = 30;

  /**
   * If to embed media links.
   */
  private static boolean embedMediaLinks = true;

  /**
   * Associates the command with its properties.
   *
   * @param prefix    prefix
   * @param altPrefix alternate prefix
   */
  public Settings(@NotNull String prefix, @NotNull String altPrefix) {
    Settings.prefix = Objects.requireNonNull(prefix, "Null prefix");
    Settings.alternativePrefix = Objects.requireNonNull(altPrefix, "Null alternate prefix");
    this.name = "settings";
    this.aliases = new String[]{"settings", "config"};
    this.arguments = "[0]MainMenu [1]Setting [2]True/False";
    this.help = "Provides information on the bot's settings.";
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
      case 2 -> new SettingsChangeRequest(ce).interpretRequest(parameters);
      default -> ce.getChannel().sendMessage(BotMessage.INVALID_NUMBER_OF_PARAMETERS.getMessage()).queue();
    }
  }

  /**
   * Sends an embed containing the bot's settings' boolean values.
   *
   * @param ce command event
   */
  private void sendSettingsMenu(CommandEvent ce) {
    EmbedBuilder embed = new EmbedBuilder();
    embed.setAuthor("Settings");
    embed.setDescription("**Prefix:** `" + prefix
        + "` \n**AlternatePrefix:** `" + alternativePrefix + "`"
        + "\n**DeleteInvoke**: `" + deleteInvoke
        + "`" + "\n**EmbedDecay:** `" + embedDecay + "`"
        + "\n**EmbedDecayTime:** `" + embedDecayTime + "`s"
        + "\n**EmbedMediaLinks:** `" + embedMediaLinks + "`");
    sendEmbed(ce, embed);
  }

  /**
   * Deletes users' command invocations.
   *
   * @param ce command event
   */
  public static void deleteInvoke(@NotNull CommandEvent ce) {
    if (deleteInvoke) {
      Objects.requireNonNull(ce, "Null command event").getMessage().delete().queue();
    }
  }

  /**
   * Sends a pre-set embed configuration into the text channel.
   *
   * @param ce    command event
   * @param embed embed
   */
  public static void sendEmbed(@NotNull CommandEvent ce, @NotNull EmbedBuilder embed) {
    Objects.requireNonNull(ce, "Null command event").getChannel().sendTyping().queue();
    Objects.requireNonNull(embed, "Null embed").setColor(0x006fb1);
    embed.setFooter(ce.getMember().getUser().getAsTag());
    embed.setTimestamp(Instant.now());
    Settings.embedDecay(ce, embed);
  }

  /**
   * Automatically deletes embeds after an elapsed period of time.
   *
   * @param ce    command event
   * @param embed embed
   */
  public static void embedDecay(@NotNull CommandEvent ce, @NotNull EmbedBuilder embed) {
    Objects.requireNonNull(ce, "Null command event");
    Objects.requireNonNull(embed, "Null embed");
    if (embedDecay) {
      ce.getChannel().sendMessageEmbeds(embed.build()).complete().delete().queueAfter(embedDecayTime, TimeUnit.SECONDS);
    } else {
      ce.getChannel().sendMessageEmbeds(embed.build()).queue();
    }
  }

  /**
   * Gets the commands prefix.
   *
   * @return commands prefix
   */
  @NotNull
  public static String getPrefix() {
    return Settings.prefix;
  }

  /**
   * If to embed media links.
   *
   * @return if to embed media links
   */
  public static boolean getEmbedMediaLinks() {
    return Settings.embedMediaLinks;
  }

  /**
   * Represents a settings change query.
   *
   * @param ce command event
   * @author Danny Nguyen
   * @version 1.8.13
   * @since 1.8.13
   */
  private record SettingsChangeRequest(CommandEvent ce) {
    /**
     * Either changes the setting:
     * <ul>
     *  <li> delete invoke
     *  <li> embed decay
     *  <li> embed decay time
     *  <li> embed media links
     * </ul>
     *
     * @param parameters user provided parameters
     */
    private void interpretRequest(String[] parameters) {
      try {
        Setting setting = Setting.valueOf(parameters[1].toUpperCase());
        switch (setting) {
          case DELETEINVOKE -> setDeleteInvokeSetting(parameters[2].toLowerCase());
          case EMBEDDECAY -> setEmbedDecaySetting(parameters[2].toLowerCase());
          case EMBEDDECAYTIME -> setEmbedDecayTimeSetting(parameters[2]);
          case EMBEDMEDIALINKS -> setEmbedMediaLinksSetting(parameters[2].toLowerCase());
        }
      } catch (IllegalArgumentException ex) {
        ce.getChannel().sendMessage("Setting not found.").queue();
      }
    }

    /**
     * Changes the delete invoke's setting to true or false.
     *
     * @param value the boolean value to be changed to
     */
    private void setDeleteInvokeSetting(String value) {
      if (value.equals("true") || value.equals("false")) {
        Settings.deleteInvoke = Boolean.parseBoolean(value);
        ce.getChannel().sendMessage("DeleteInvoke has been set to `" + Settings.deleteInvoke + "`.").queue();
      } else {
        ce.getChannel().sendMessage(Error.SPECIFY_TRUE_FALSE.message).queue();
      }
    }

    /**
     * Changes the embed decay's setting to true or false.
     *
     * @param value the boolean value to be changed to
     */
    private void setEmbedDecaySetting(String value) {
      if (value.equals("true") || value.equals("false")) {
        Settings.embedDecay = Boolean.parseBoolean(value);
        ce.getChannel().sendMessage("EmbedDecay has been set to `" + Settings.embedDecay + "`.").queue();
      } else {
        ce.getChannel().sendMessage(Error.SPECIFY_TRUE_FALSE.message).queue();
      }
    }

    /**
     * Changes the embed decay time's setting to an integer value.
     *
     * @param value the boolean value to be changed to
     */
    private void setEmbedDecayTimeSetting(String value) {
      try {
        int timeValue = Integer.parseInt(value);
        if (timeValue >= 15 && timeValue <= 120) {
          Settings.embedDecayTime = timeValue;
          ce.getChannel().sendMessage("EmbedDecayTime has been set to `" + Settings.embedDecayTime + "`s.").queue();
        } else {
          ce.getChannel().sendMessage(Error.SETTINGS_EMBED_DECAY_RANGE.message).queue();
        }
      } catch (NumberFormatException e) {
        ce.getChannel().sendMessage(Error.SETTINGS_EMBED_DECAY_RANGE.message).queue();
      }
    }

    /**
     * Changes the embed media links setting to true or false.
     *
     * @param value the boolean value to be changed to
     */
    private void setEmbedMediaLinksSetting(String value) {
      if (value.equals("true") || value.equals("false")) {
        Settings.embedMediaLinks = Boolean.parseBoolean(value);
        ce.getChannel().sendMessage("EmbedMediaLinks has been set to `" + Settings.embedMediaLinks + "`.").queue();
      } else {
        ce.getChannel().sendMessage(Error.SPECIFY_TRUE_FALSE.message).queue();
      }
    }

    /**
     * Types of settings.
     */
    private enum Setting {
      /**
       * If to delete command invocations.
       */
      DELETEINVOKE,

      /**
       * If to delete embeds after a delay.
       */
      EMBEDDECAY,

      /**
       * How long to wait before deleting embeds.
       */
      EMBEDDECAYTIME,

      /**
       * If to embed media links.
       */
      EMBEDMEDIALINKS;
    }

    /**
     * Types of errors.
     */
    private enum Error {
      /**
       * Non-boolean value.
       */
      SPECIFY_TRUE_FALSE("Provide true or false."),

      /**
       * Out of range or not in seconds.
       */
      SETTINGS_EMBED_DECAY_RANGE("Provide between 15 - 120 seconds.");

      /**
       * Message.
       */
      public final String message;

      /**
       * Associates an error with its message.
       *
       * @param message message
       */
      Error(String message) {
        this.message = message;
      }
    }
  }
}
