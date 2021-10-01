package commands.utility;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import commands.owner.Settings;
import net.dv8tion.jda.api.EmbedBuilder;

public class Remind extends Command {
  public Remind() {
    this.name = "remind";
    this.aliases = new String[]{"remind", "reminder", "remindme", "notify", "mentionme", "alert"};
    this.arguments = "[1]TimeDuration&TimeType/Time [2]TimeDuration/TimeType/EventName [3++]EventName";
    this.help = "Sets a timer and alerts the user when the time expires.";
    this.ownerCommand = false;
  }

  @Override
  protected void execute(CommandEvent ce) {
    Settings.deleteInvoke(ce);
    String[] args = ce.getMessage().getContentRaw().split("\\s"); // Parse message for arguments
    int arguments = args.length;
    // Invalid arguments
    if (arguments == 1) {
      ce.getChannel().sendMessage("Invalid number of arguments.").queue();
    } else { // Time & timer name
      if (checkValidTimeType(args)) { // Accepted timeType
        String timerName = "";
        if (checkTimeTypeInFirstArgument(args)) { // Timer name
          timeTypeInFirstArgument(ce, args, arguments, timerName);
        } else { // Timer name (alternative handling)
          timeTypeInSecondArgument(ce, args, arguments, timerName);
        }
      } else { // No timeType exists in first or second argument
        ce.getChannel().sendMessage("Invalid argument.").queue();
      }
    }
  }

  // First or second argument must have a correct time type
  private boolean checkValidTimeType(String[] args) {
    int indexLimit;
    if (args.length == 2) {
      indexLimit = 2;
    } else {
      indexLimit = 3;
    }
    for (int i = 1; i < indexLimit; i++) {
      char lastChar = args[i].charAt(args[i].length() - 1);
      switch (lastChar) {
        case 's', 'm', 'h' -> {
          return true;
        }
      }
    }
    return false;
  }

  // Check if first argument ends in s, m, h
  private boolean checkTimeTypeInFirstArgument(String[] args) {
    char timeType = args[1].charAt(args[1].length() - 1);
    return timeType == 's' || timeType == 'm' || timeType == 'h';
  }

  private void timeTypeInFirstArgument(CommandEvent ce, String[] args, int arguments, String timerName) {
    try { // Ensure argument is an integer
      String timeString = args[1].substring(0, args[1].length() - 1);
      int timeDuration = Integer.parseInt(timeString);
      char timeType = args[1].charAt(args[1].length() - 1);
      if (checkTimeDurationLimit(timeDuration, timeType)) { // Range of 1 day
        if (arguments > 2) { // Timer name provided
          StringBuilder timerNameBuilder = new StringBuilder(timerName);
          for (int i = 2; i < args.length; i++) {
            timerNameBuilder.append(args[i]).append(" ");
          }
          timerName = timerNameBuilder.toString();
        }
        setReminder(ce, timeDuration, timeType, timerName);
        setTimer(ce, timeDuration, timeType, timerName);
      } else { // Outside range of 1 day
        ce.getChannel().sendMessage("You can only set the timer for the maximum length of a day.").queue();
      }
    } catch (NumberFormatException error) { // Input mismatch
      ce.getChannel().sendMessage("You must to specify a valid numerical value, followed by an accepted time type.")
          .queue();
    }
  }

  private void timeTypeInSecondArgument(CommandEvent ce, String[] args, int arguments, String timerName) {
    try { // Ensure argument is an integer
      int timeDuration = Integer.parseInt(args[1]);
      char timeType = timeTypeStringConversion(args);
      switch (timeType) {
        case 's', 'm', 'h' -> { // Check if second argument ends in s, m, h
          if (checkTimeDurationLimit(timeDuration, timeType)) { // Range of 1 day
            if (arguments > 3) { // Timer name provided
              StringBuilder timerNameBuilder = new StringBuilder(timerName);
              for (int i = 3; i < args.length; i++) {
                timerNameBuilder.append(args[i]).append(" ");
              }
              timerName = timerNameBuilder.toString();
            }
            setReminder(ce, timeDuration, timeType, timerName);
            setTimer(ce, timeDuration, timeType, timerName);
          } else { // Outside range of 1 day
            ce.getChannel().sendMessage("You can only set the timer for the maximum length of a day.").queue();
          }
        }
        default -> // Unaccepted time type
            ce.getChannel().sendMessage("You must to specify a valid numerical value, followed by an accepted time type.")
                .queue();
      }
    } catch (NumberFormatException error) { // Input mismatch
      ce.getChannel().sendMessage("You must to specify a valid numerical value, followed by an accepted time type.")
          .queue();
    }
  }

  // Convert time grammar to timeType
  private char timeTypeStringConversion(String[] args) {
    return switch (args[2]) {
      case "hours", "hour", "hrs", "hr", "h" -> 'h';
      case "minutes", "minute", "mins", "min", "m" -> 'm';
      case "seconds", "second", "secs", "sec", "s" -> 's';
      default -> args[1].charAt(args[1].length() - 1);
    };
  }

  // Validate time range of 1 day
  private boolean checkTimeDurationLimit(int timeDuration, char timeType) {
    if (timeType == 'h' && timeDuration >= 0 && timeDuration <= 24) {
      return true;
    } else if (timeType == 'm' && timeDuration >= 0 && timeDuration <= 1440) {
      return true;
    } else return timeType == 's' && timeDuration >= 0 && timeDuration <= 86400;
  }

  // Visual timer creation confirmation
  private void setReminder(CommandEvent e, int timeDuration, char timeType, String timerName) {
    EmbedBuilder display = new EmbedBuilder();
    display.setTitle("__Reminder__");
    display.setDescription(!timerName.equals("")
        ? "Time set for " + timerName + " in (" + timeDuration + ") " + getTimeTypeString(timeType) + "."
        : "Timer set to mention you in (" + timeDuration + ") " + getTimeTypeString(timeType) + ".");
    Settings.sendEmbed(e, display);
  }

  // Timer countdown
  public void setTimer(CommandEvent e, int timeDuration, char timeType, String timerName) {
    new java.util.Timer().schedule(new java.util.TimerTask() {
      public void run() {
        EmbedBuilder display = new EmbedBuilder();
        display.setTitle("__Reminder__");
        display.setDescription(
            !timerName.equals("") ? "Hey " + e.getMember().getAsMention() + ", " + timerName + " is starting now!"
                : "Hey " + e.getMember().getAsMention() + ", time's up!");
        Settings.sendEmbed(e, display);
      }
    }, timeDurationIntoMilliseconds(timeDuration, timeType));
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

  // Millisecond conversion
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