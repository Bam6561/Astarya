package me.dannynguyen.astarya.commands.utility;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import me.dannynguyen.astarya.commands.owner.Settings;
import me.dannynguyen.astarya.enums.BotMessage;
import net.dv8tion.jda.api.EmbedBuilder;

/**
 * Remind is a command invocation that sets a timer and alerts the user when the time expires.
 *
 * @author Danny Nguyen
 * @version 1.8.1
 * @since 1.0
 */
public class Remind extends Command {
  public Remind() {
    this.name = "remind";
    this.aliases = new String[]{"remind", "timer"};
    this.arguments = "[1]TimeDuration&TimeType/Time [2]TimeDuration/TimeType/EventName [3++]EventName";
    this.help = "Sets a timer and alerts the user when the time expires.";
    this.ownerCommand = false;
  }

  /**
   * Checks if user provided parameters to read the remind command request.
   * <p>
   * Users can provide time types that either come in the form of a String or a Char,
   * so both variants are checked as to if they exist and marked as to which
   * parameter they are provided in. The location of where the time type is located
   * changes the parsing processing slightly, but the overall logic remains the same.
   * <p>
   * After the time type is provided, the user can optionally
   * provide a name for their reminder in additional parameters.
   *
   * @param ce command event
   */
  @Override
  protected void execute(CommandEvent ce) {
    Settings.deleteInvoke(ce);

    String[] parameters = ce.getMessage().getContentRaw().split("\\s");
    int numberOfParameters = parameters.length - 1;

    if (numberOfParameters != 0) {
      readRemindRequest(ce, parameters, numberOfParameters);
    } else {
      ce.getChannel().sendMessage(BotMessage.INVALID_NUMBER_OF_PARAMETERS.getMessage()).queue();
    }
  }

  /**
   * Checks if user provided parameters contain a valid time type before processing the time type's location.
   *
   * @param ce                 command event
   * @param parameters         user provided parameters
   * @param numberOfParameters number of user provided parameters
   */
  private void readRemindRequest(CommandEvent ce, String[] parameters, int numberOfParameters) {
    boolean isValidTimeType = checkValidTimeTypeProvided(parameters);
    if (isValidTimeType) {
      boolean timeIsInFirstParameter = checkTimeTypeInFirstParameter(parameters);
      processTimeTypeBasedOnLocation(ce, parameters, numberOfParameters, timeIsInFirstParameter);
    } else { // No time type in first or second parameter
      ce.getChannel().sendMessage(Failure.NO_TIME_TYPES.text).queue();
    }
  }

  /**
   * Checks for recognized time types.
   *
   * @param parameters user provided parameters
   * @return whether the time type provided is valid
   */
  private boolean checkValidTimeTypeProvided(String[] parameters) {
    int numberOfParameters = parameters.length;
    int indexLimit = 3; // Assume time type is in the ending parameter
    boolean timeTypeIsInTheFirstOrSecondParameter = numberOfParameters == 2;
    if (timeTypeIsInTheFirstOrSecondParameter) { // Only check first or second parameter for time type
      indexLimit = 2;
    }

    // Search parameters for the time type symbol
    for (int i = 1; i < indexLimit; i++) {
      String parameter = parameters[i]; // Time type string
      char lastChar = parameter.charAt(parameters[i].length() - 1); // Time type char
      switch (parameter) {
        case "days", "day", "d", "hours", "hour", "hrs", "hr", "h", "minutes", "minute", "mins",
            "min", "m", "seconds", "second", "secs", "sec", "s" -> {
          return true;
        }
      }
      switch (lastChar) {
        case 'd', 'h', 'm', 's' -> {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Checks if the first parameter ends in d, h, m, or s.
   *
   * @param parameters user provided parameters
   * @return whether the time type exists in the first parameter
   */
  private boolean checkTimeTypeInFirstParameter(String[] parameters) {
    char timeType = parameters[1].charAt(parameters[1].length() - 1);
    return timeType == 'd' || timeType == 'h' || timeType == 'm' || timeType == 's';
  }

  /**
   * Processes the remind command request based on the time type's location.
   * <p>
   * Where the user provides the time type affects the data parsing, so
   * this method handles the differences in one stream.
   *
   * @param ce                   command event
   * @param parameters           user provided parameters
   * @param numberOfParameters   number of user provided parameters
   * @param timeInFirstParameter whether the time type exists in the first parameter
   * @throws NumberFormatException user provided non-integer values
   */
  private void processTimeTypeBasedOnLocation(CommandEvent ce, String[] parameters,
                                              int numberOfParameters, boolean timeInFirstParameter) {
    try {
      char timeType;
      int timeDuration;
      if (timeInFirstParameter) {
        timeType = parameters[1].charAt(parameters[1].length() - 1);
        timeDuration = Integer.parseInt(parameters[1].substring(0, parameters[1].length() - 1));
      } else { // Time in second parameter
        timeType = convertTimeStringToTimeType(parameters);
        timeDuration = Integer.parseInt(parameters[1]);
      }

      boolean validTimeDuration = checkTimeDuration(timeDuration, timeType);
      if (validTimeDuration) { // Within the range of a week
        String timerName = "";
        timerName = setTimerName(parameters, numberOfParameters, timeInFirstParameter, timerName);
        setReminder(ce, timeDuration, timeType, timerName);
        setTimer(ce, timeDuration, timeType, timerName);
      } else {
        ce.getChannel().sendMessage(Failure.EXCEED_RANGE.text).queue();
      }
    } catch (NumberFormatException e) {
      ce.getChannel().sendMessage(Failure.INVALID_INPUT.text).queue();
    }
  }

  /**
   * Converts all variances of written time to uniform time types.
   *
   * @param parameters user provided parameters
   * @return character representing either seconds, minutes, or hours
   */
  private char convertTimeStringToTimeType(String[] parameters) {
    return switch (parameters[2]) {
      case "days", "day", "d" -> 'd';
      case "hours", "hour", "hrs", "hr", "h" -> 'h';
      case "minutes", "minute", "mins", "min", "m" -> 'm';
      case "seconds", "second", "secs", "sec", "s" -> 's';
      default -> parameters[1].charAt(parameters[1].length() - 1);
    };
  }

  /**
   * Checks the time range is within 1 week.
   *
   * @param timeDuration duration of time
   * @param timeType     type of time
   * @return whether the time duration is within 1 week.
   */
  private boolean checkTimeDuration(int timeDuration, char timeType) {
    if (timeType == 'd' && timeDuration >= 0 && timeDuration <= 7) {
      return true;
    } else if (timeType == 'h' && timeDuration >= 0 && timeDuration <= 168) {
      return true;
    } else if (timeType == 'm' && timeDuration >= 0 && timeDuration <= 10080) {
      return true;
    } else {
      return (timeType == 's' && timeDuration >= 0 && timeDuration <= 604800);
    }
  }

  /**
   * Sets name of the reminder using user provided parameters.
   * <p>
   * Where the user provides the time type affects the data parsing, so
   * this method handles the differences in one stream.
   *
   * @param parameters           user provided parameters
   * @param numberOfParameters   number of user provided parameters
   * @param timeInFirstParameter whether the time type exists in the first parameter
   * @param timerName            name of the reminder
   * @return name of the reminder
   */
  private String setTimerName(String[] parameters, int numberOfParameters, boolean timeInFirstParameter, String timerName) {
    if (timeInFirstParameter) {
      boolean timerNameProvided = numberOfParameters > 1;

      if (timerNameProvided) {
        StringBuilder timerNameBuilder = new StringBuilder();
        for (int i = 2; i < parameters.length; i++) {
          timerNameBuilder.append(parameters[i]).append(" ");
        }
        timerName = timerNameBuilder.toString();
        return timerName;
      }
    } else {
      boolean timerNameProvided = numberOfParameters > 2;

      if (timerNameProvided) {
        StringBuilder timerNameBuilder = new StringBuilder(timerName);
        for (int i = 3; i < parameters.length; i++) {
          timerNameBuilder.append(parameters[i]).append(" ");
        }
        timerName = timerNameBuilder.toString();
        return timerName;
      }
    }
    return "";
  }

  /**
   * Sends an embed containing confirmation for the creation of a reminder.
   *
   * @param ce           command event
   * @param timeDuration duration of time
   * @param timeType     type of time
   * @param timerName    name of the reminder
   */
  private void setReminder(CommandEvent ce, int timeDuration, char timeType, String timerName) {
    EmbedBuilder display = new EmbedBuilder();
    display.setAuthor("Reminder");
    display.setDescription(!timerName.equals("")
        ? "Time set for `" + timerName.substring(0, timerName.length() - 1) +
        "` in (" + timeDuration + ") " + getTimeTypeString(timeType) + "."
        : "Timer set to mention you in (" + timeDuration + ") " + getTimeTypeString(timeType) + ".");
    Settings.sendEmbed(ce, display);
  }

  /**
   * Converts the uniform time type characters to a readable form.
   *
   * @param timeType type of time
   * @return the time type in a written form
   */
  private String getTimeTypeString(char timeType) {
    switch (timeType) {
      case 'd' -> {
        return "days";
      }
      case 'h' -> {
        return "hours";
      }
      case 'm' -> {
        return "minutes";
      }
      case 's' -> {
        return "seconds";
      }
    }
    return null;
  }

  /**
   * Creates a timer for the user provided time duration.
   *
   * @param ce           command event
   * @param timeDuration duration of time
   * @param timeType     type of time
   * @param timerName    name of the reminder
   */
  private void setTimer(CommandEvent ce, int timeDuration, char timeType, String timerName) {
    new java.util.Timer().schedule(new java.util.TimerTask() {
      public void run() {
        ce.getChannel().sendMessage(!timerName.equals("") ? "Hey " + ce.getMember().getAsMention()
            + ", `" + timerName.substring(0, timerName.length() - 1) + "` is starting now!"
            : "Hey " + ce.getMember().getAsMention() + ", time's up!").queue();
      }
    }, timeDurationIntoMilliseconds(timeDuration, timeType));
  }

  /**
   * Converts seconds, minutes, or hours into milliseconds.
   *
   * @param timeDuration duration of time
   * @param timeType     type of time
   * @return equivalent amount of time in milliseconds
   */
  private int timeDurationIntoMilliseconds(int timeDuration, char timeType) {
    switch (timeType) {
      case 'd' -> {
        return timeDuration * 86400000;
      }
      case 'h' -> {
        return timeDuration * 3600000;
      }
      case 'm' -> {
        return timeDuration * 60000;
      }
      case 's' -> {
        return timeDuration * 1000;
      }
    }
    return -1;
  }

  private enum Failure {
    NO_TIME_TYPES("No time types provided."),
    INVALID_INPUT("Provide a valid numerical value followed by an accepted time type."),
    EXCEED_RANGE("Can only set timer for maximum length of a week.");

    public final String text;

    Failure(String text) {
      this.text = text;
    }
  }
}