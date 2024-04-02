package me.dannynguyen.astarya.commands.utility;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import me.dannynguyen.astarya.commands.owner.Settings;
import me.dannynguyen.astarya.enums.BotMessage;
import net.dv8tion.jda.api.EmbedBuilder;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Command invocation that sets a timer and alerts the user when the time expires.
 *
 * @author Danny Nguyen
 * @version 1.9.4
 * @since 1.0
 */
public class Remind extends Command {
  /**
   * Associates the command with its properties.
   */
  public Remind() {
    this.name = "remind";
    this.aliases = new String[]{"remind", "timer"};
    this.arguments = "[1]Time (dd:hh:mm) [2, ++] EventName";
    this.help = "Sets a timer and alerts the user when the time expires.";
  }

  /**
   * Checks if user provided parameters to read the command request.
   * <p>
   * Optionally, the user can provide a name for their reminder.
   *
   * @param ce command event
   */
  @Override
  protected void execute(CommandEvent ce) {
    Settings.deleteInvoke(ce);

    String[] parameters = ce.getMessage().getContentRaw().split("\\s");
    int numberOfParameters = parameters.length - 1;

    if (numberOfParameters > 0) {
      new RemindRequest(ce, parameters, numberOfParameters).readRequest();
    } else {
      ce.getChannel().sendMessage(BotMessage.INVALID_NUMBER_OF_PARAMETERS.getMessage()).queue();
    }
  }

  /**
   * Represents a set timer query.
   *
   * @author Danny Nguyen
   * @version 1.9.4
   * @since 1.9.4
   */
  private record RemindRequest(CommandEvent ce, String[] parameters, int numberOfParameters) {
    /**
     * Checks if the command request was formatted correctly
     * before setting a new timer with optional timer name.
     */
    private void readRequest() {
      long timerLengthToBeSet = convertTimeToLong();
      if (timerLengthToBeSet == -1) {
        ce.getChannel().sendMessage("Invalid time frame. Provide dd:hh:mm.").queue();
        return;
      }

      StringBuilder timerNameBuilder = new StringBuilder();
      if (numberOfParameters >= 2) {
        for (int i = 2; i < parameters.length; i++) {
          timerNameBuilder.append(parameters[i]).append(" ");
        }
      }
      String timerName = timerNameBuilder.toString().trim();

      EmbedBuilder embed = new EmbedBuilder();
      embed.setAuthor("Reminder");
      if (timerName.isEmpty()) {
        embed.setDescription("Timer set to mention you in in (" + parameters[1] + ").");
      } else {
        embed.setDescription("Time set for `" + timerName + "` in (" + parameters[1] + ").");
      }
      Settings.sendEmbed(ce, embed);

      new Timer().schedule(new TimerTask() {
        @Override
        public void run() {
          if (timerName.isEmpty()) {
            ce.getChannel().sendMessage("Hey " + ce.getMember().getAsMention() + ", time's up!").queue();
          } else {
            ce.getChannel().sendMessage("Hey " + ce.getMember().getAsMention() + ", `" + timerName + "` is starting now!").queue();
          }
        }
      }, timerLengthToBeSet);
    }

    /**
     * Converts user provided dd:hh:mm format to milliseconds.
     *
     * @return length of the timer to be set in milliseconds
     */
    private long convertTimeToLong() {
      try {
        String[] timerLengthTypes = parameters[1].split(":");
        long days = 0;
        long hours = 0;
        long minutes = 0;

        switch (timerLengthTypes.length) {
          case 1 -> minutes = Integer.parseInt(timerLengthTypes[0]);
          case 2 -> {
            hours = Integer.parseInt(timerLengthTypes[0]);
            minutes = Integer.parseInt(timerLengthTypes[1]);
          }
          case 3 -> {
            days = Integer.parseInt(timerLengthTypes[0]);
            hours = Integer.parseInt(timerLengthTypes[1]);
            minutes = Integer.parseInt(timerLengthTypes[2]);
          }
          default -> ce.getChannel().sendMessage(BotMessage.INVALID_NUMBER_OF_PARAMETERS.getMessage()).queue();
        }

        // Conversion to milliseconds
        days = days * 86400000;
        hours = hours * 3600000;
        minutes = minutes * 60000;
        return days + hours + minutes;
      } catch (NumberFormatException ex) {
        return -1;
      }
    }
  }
}