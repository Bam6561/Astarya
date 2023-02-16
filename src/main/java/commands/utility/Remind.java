package commands.utility;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import commands.owner.Settings;
import net.dv8tion.jda.api.EmbedBuilder;

public class Remind extends Command {
  public Remind() {
    this.name = "remind";
    this.aliases = new String[]{"remind", "alert", "timer"};
    this.arguments = "[1]TimeDuration&TimeType/Time [2]TimeDuration/TimeType/EventName [3++]EventName";
    this.help = "Sets a timer and alerts the user when the time expires.";
    this.ownerCommand = false;
  }

  // Sends an embed containing user input information after an elapsed period of time
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

  // Validate recognized time types
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
        case "hours", "hour", "hrs", "hr", "h", "minutes", "minute", "mins",
            "min", "m", "seconds", "second", "secs", "sec", "s" -> {
          return true;
        }
      }
      switch (lastChar) {
        case 's', 'm', 'h' -> {
          return true;
        }
      }
    }
    return false;
  }

  // Check if first argument ends in s, m, h
  private boolean checkTimeTypeInFirstArgument(String[] arguments) {
    char timeType = arguments[1].charAt(arguments[1].length() - 1);
    return timeType == 's' || timeType == 'm' || timeType == 'h';
  }

  /*
  Where the user provides the time type affects the data parsing, so
  this method handles the differences in one stream.
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
      if (validTimeDuration) { // Within the range of a day
        String timerName = "";
        timerName = setTimerName(arguments, numberOfArguments, timeInFirstArgument, timerName);

        setReminder(ce, timeDuration, timeType, timerName);
        setTimer(ce, timeDuration, timeType, timerName);
      } else { // Outside range of 1 day
        ce.getChannel().sendMessage("Can only set timer for the maximum length of a day.").queue();
      }
    } catch (NumberFormatException e) {
      ce.getChannel().sendMessage("Specify a valid numerical value, followed by an accepted time type.")
          .queue();
    }
  }

  // Convert all variances of how to write time to uniform timeType
  private char convertTimeStringToTimeType(String[] args) {
    return switch (args[2]) {
      case "hours", "hour", "hrs", "hr", "h" -> 'h';
      case "minutes", "minute", "mins", "min", "m" -> 'm';
      case "seconds", "second", "secs", "sec", "s" -> 's';
      default -> args[1].charAt(args[1].length() - 1);
    };
  }

  // Validate time range within 1 day
  private boolean checkTimeDuration(int timeDuration, char timeType) {
    if (timeType == 'h' && timeDuration >= 0 && timeDuration <= 24) {
      return true;
    } else if (timeType == 'm' && timeDuration >= 0 && timeDuration <= 1440) {
      return true;
    } else {
      return (timeType == 's' && timeDuration >= 0 && timeDuration <= 86400);
    }
  }

  /*
  Where the user provides the time type affects the timer name parsing, so
  this method handles the differences in one stream.
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

  // Sends an embed containing visual reminder creation confirmation
  private void setReminder(CommandEvent ce, int timeDuration, char timeType, String timerName) {
    EmbedBuilder display = new EmbedBuilder();
    display.setTitle("__Reminder__");
    display.setDescription(!timerName.equals("")
        ? "Time set for `" + timerName.substring(0, timerName.length() - 1) +
        "` in (" + timeDuration + ") " + getTimeTypeString(timeType) + "."
        : "Timer set to mention you in (" + timeDuration + ") " + getTimeTypeString(timeType) + ".");

    Settings.sendEmbed(ce, display);
  }

  // Character conversion to string
  private String getTimeTypeString(char timeType) {
    if (timeType == 's') {
      return "seconds";
    } else if (timeType == 'm') {
      return "minutes";
    } else if (timeType == 'h') {
      return "hours";
    }
    return null;
  }

  // Creates a timer
  public void setTimer(CommandEvent ce, int timeDuration, char timeType, String timerName) {
    new java.util.Timer().schedule(new java.util.TimerTask() {
      public void run() {
        ce.getChannel().sendMessage(!timerName.equals("") ? "Hey " + ce.getMember().getAsMention()
            + ", `" + timerName.substring(0, timerName.length() - 1) + "` is starting now!"
            : "Hey " + ce.getMember().getAsMention() + ", time's up!").queue();
      }
    }, timeDurationIntoMilliseconds(timeDuration, timeType));
  }

  // Millisecond conversion for application timer
  private int timeDurationIntoMilliseconds(int timeDuration, char timeType) {
    if (timeType == 's') {
      return timeDuration * 1000;
    } else if (timeType == 'm') {
      return timeDuration * 60000;
    } else if (timeType == 'h') {
      return timeDuration * 3600000;
    }
    return -1;
  }
}