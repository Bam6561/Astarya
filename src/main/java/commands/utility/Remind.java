package commands.utility;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import commands.owner.Settings;
import net.dv8tion.jda.api.EmbedBuilder;

/**
 * Remind is a command invocation that sets a timer and alerts the user when the time expires.
 *
 * @author Danny Nguyen
 * @version 1.6.5
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
   * Processes user provided arguments to determine whether the remind command request was formatted correctly.
   * <p>
   * Users can provide time types that either come in the form of a String or a Char,
   * so both variants are checked as to whether they exist and marked as to which
   * argument they are provided in. The location of where the time type is located
   * changes the parsing processing slightly, but the overall logic remains the same.
   * </p>
   * <p>
   * After the time type is provided, the user can optionally
   * provide a name for their reminder in additional arguments.
   * </p>
   *
   * @param ce object containing information about the command event
   */
  @Override
  protected void execute(CommandEvent ce) {
    Settings.deleteInvoke(ce);

    // Parse message for arguments
    String[] arguments = ce.getMessage().getContentRaw().split("\\s");
    int numberOfArguments = arguments.length - 1;

    if (numberOfArguments != 0) {
      boolean isValidTimeType = checkValidTimeTypeProvided(arguments);
      if (isValidTimeType) {
        boolean timeIsInFirstArgument = checkTimeTypeInFirstArgument(arguments);
        processTimeTypeBasedOnLocation(ce, arguments, numberOfArguments, timeIsInFirstArgument);
      } else { // No time type in first or second argument
        ce.getChannel().sendMessage("No time types provided.").queue();
      }
    } else {
      ce.getChannel().sendMessage("Invalid number of arguments.").queue();
    }
  }

  /**
   * Checks for recognized time types.
   *
   * @param arguments user provided arguments
   * @return whether the time type provided is valid
   */
  private boolean checkValidTimeTypeProvided(String[] arguments) {
    int numberOfArguments = arguments.length;
    int indexLimit = 3; // Assume time type is in the ending argument
    boolean timeTypeIsInTheFirstOrSecondArgument = numberOfArguments == 2;
    if (timeTypeIsInTheFirstOrSecondArgument) { // Only check first or second argument for time type
      indexLimit = 2;
    }

    // Search arguments for the time type symbol
    for (int i = 1; i < indexLimit; i++) {
      String argument = arguments[i]; // Time type string
      char lastChar = argument.charAt(arguments[i].length() - 1); // Time type char
      switch (argument) {
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
   * Checks if the first argument ends in d, h, m, or s.
   *
   * @param arguments user provided arguments
   * @return whether the time type exists in the first argument
   */
  private boolean checkTimeTypeInFirstArgument(String[] arguments) {
    char timeType = arguments[1].charAt(arguments[1].length() - 1);
    return timeType == 'd' || timeType == 'h' || timeType == 'm' || timeType == 's';
  }

  /**
   * Processes the remind command request based on the time type's location.
   * <p>
   * Where the user provides the time type affects the data parsing, so
   * this method handles the differences in one stream.
   * </p>
   *
   * @param ce                  object containing information about the command event
   * @param arguments           user provided arguments
   * @param numberOfArguments   number of user provided arguments
   * @param timeInFirstArgument whether the time type exists in the first argument
   * @throws NumberFormatException user provided non-integer values
   */
  private void processTimeTypeBasedOnLocation(CommandEvent ce, String[] arguments, int numberOfArguments, boolean timeInFirstArgument) {
    try {
      char timeType;
      int timeDuration;
      if (timeInFirstArgument) {
        timeType = arguments[1].charAt(arguments[1].length() - 1);
        timeDuration = Integer.parseInt(arguments[1].substring(0, arguments[1].length() - 1));
      } else { // Time in second argument
        timeType = convertTimeStringToTimeType(arguments);
        timeDuration = Integer.parseInt(arguments[1]);
      }

      boolean validTimeDuration = checkTimeDuration(timeDuration, timeType);
      if (validTimeDuration) { // Within the range of a week
        String timerName = "";
        timerName = setTimerName(arguments, numberOfArguments, timeInFirstArgument, timerName);
        setReminder(ce, timeDuration, timeType, timerName);
        setTimer(ce, timeDuration, timeType, timerName);
      } else {
        ce.getChannel().sendMessage("Can only set timer for the maximum length of a week.").queue();
      }
    } catch (NumberFormatException e) {
      ce.getChannel().sendMessage("Specify a valid numerical value, followed by an accepted time type.").queue();
    }
  }

  /**
   * Converts all variances of written time to uniform time types.
   *
   * @param arguments user provided arguments
   * @return character representing either seconds, minutes, or hours
   */
  private char convertTimeStringToTimeType(String[] arguments) {
    return switch (arguments[2]) {
      case "days", "day", "d" -> 'd';
      case "hours", "hour", "hrs", "hr", "h" -> 'h';
      case "minutes", "minute", "mins", "min", "m" -> 'm';
      case "seconds", "second", "secs", "sec", "s" -> 's';
      default -> arguments[1].charAt(arguments[1].length() - 1);
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
   * Sets name of the reminder using user provided arguments.
   * <p>
   * Where the user provides the time type affects the data parsing, so
   * this method handles the differences in one stream.
   * </p>
   *
   * @param arguments           user provided arguments
   * @param numberOfArguments   number of user provided arguments
   * @param timeInFirstArgument whether the time type exists in the first argument
   * @param timerName           name of the reminder
   * @return name of the reminder
   */
  private String setTimerName(String[] arguments, int numberOfArguments, boolean timeInFirstArgument, String timerName) {
    if (timeInFirstArgument) {
      boolean timerNameProvided = numberOfArguments > 1;

      if (timerNameProvided) {
        StringBuilder timerNameBuilder = new StringBuilder();
        for (int i = 2; i < arguments.length; i++) {
          timerNameBuilder.append(arguments[i]).append(" ");
        }
        timerName = timerNameBuilder.toString();
        return timerName;
      }
    } else {
      boolean timerNameProvided = numberOfArguments > 2;

      if (timerNameProvided) {
        StringBuilder timerNameBuilder = new StringBuilder(timerName);
        for (int i = 3; i < arguments.length; i++) {
          timerNameBuilder.append(arguments[i]).append(" ");
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
   * @param ce           object containing information about a command event
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
    switch (timeType){
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
   * @param ce           object containing information about the command event
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
}