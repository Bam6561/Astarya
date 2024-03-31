package me.dannynguyen.astarya.commands.games;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import me.dannynguyen.astarya.commands.owner.Settings;
import me.dannynguyen.astarya.enums.BotMessage;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Command invocation that sends a random scenario prompt.
 * <p>
 * The prompt's subject is substituted if it has a field to support the user's given parameters.
 *
 * @author Danny Nguyen
 * @version 1.9.0
 * @since 1.6.11
 */
public class PandorasBox extends Command {
  /**
   * Recently sent prompts' order.
   */
  private final Queue<Integer> recentOrder = new LinkedList<>();

  /**
   * Recently sent prompts.
   */
  private final Set<Integer> recentValues = new HashSet<>();

  /**
   * Pandora's Box prompts.
   */
  private final List<String> pandorasBoxPrompts;

  /**
   * Associates the command with its properties.
   *
   * @param pandorasBoxPrompts pandoras box prompts
   */
  public PandorasBox(@Nullable List<String> pandorasBoxPrompts) {
    this.pandorasBoxPrompts = pandorasBoxPrompts;
    this.name = "pandorasbox";
    this.aliases = new String[]{"pandorasbox", "pb"};
    this.arguments = "[0]Self [1]VC/DC/Name [2 ++]Phrase";
    this.help = "Sends a random scenario prompt.";
  }

  /**
   * Either substitutes the prompt's subject with:
   * <ul>
   *  <li> self-user
   *  <li> random vc member
   *  <li> random discord member
   *  <li> user's choice phrase
   * </ul>
   *
   * @param ce command event
   */
  @Override
  protected void execute(CommandEvent ce) {
    Settings.deleteInvoke(ce);

    String[] parameters = ce.getMessage().getContentRaw().split("\\s");
    int numberOfParameters = parameters.length - 1;

    switch (numberOfParameters) {
      case 0 -> {
        String subject = ce.getMember().getNickname();
        if (subject == null) {
          subject = ce.getMember().getEffectiveName();
        }
        sendPrompt(ce, subject); // Target: Self
      }
      case 1 -> { // Target: VC, DC, or  *
        String parameter = parameters[1].toLowerCase();
        if (parameter.equals("vc")) {
          randomVoiceChannelSubject(ce);
        } else if (parameter.equals("dc")) {
          randomDiscordSubject(ce);
        } else {
          sendPrompt(ce, parameters[1]);
        }
      }
      default -> { // Target: *
        StringBuilder subject = new StringBuilder();
        for (int i = 1; i < parameters.length; i++) {
          subject.append(parameters[i]).append(" ");
        }
        subject.deleteCharAt(subject.length() - 1);

        sendPrompt(ce, subject.toString());
      }
    }
  }

  /**
   * Substitutes the subject from a random Pandora's Box prompt and then is sent.
   *
   * @param ce      command event
   * @param subject the person the prompt is about
   */
  private void sendPrompt(CommandEvent ce, String subject) {
    int index = randomIndex();

    String prompt = pandorasBoxPrompts.get(index);
    if (prompt.contains("<subject>")) {
      prompt = prompt.replace("<subject>", subject);
    } else {
      prompt = "As " + subject + ": " + prompt;
    }

    ce.getChannel().sendMessage(prompt).queue();
  }

  /**
   * Chooses a random voice channel member to be substituted as a prompt subject.
   *
   * @param ce command event
   */
  private void randomVoiceChannelSubject(CommandEvent ce) {
    GuildVoiceState userVoiceState = ce.getMember().getVoiceState();

    if (userVoiceState.inAudioChannel()) {
      VoiceChannel vc = userVoiceState.getChannel().asVoiceChannel();
      List<Member> vcMembers = vc.getMembers();
      int randomVCMember = (int) (Math.random() * vcMembers.size());

      String subject = vcMembers.get(randomVCMember).getNickname();
      sendPrompt(ce, subject);
    } else {
      ce.getChannel().sendMessage(BotMessage.USER_NOT_IN_VC.getMessage()).queue();
    }
  }

  /**
   * Chooses a random Discord member to be substituted as a prompt subject.
   *
   * @param ce command event
   */
  private void randomDiscordSubject(CommandEvent ce) {
    List<Member> dcMembers = ce.getGuild().getMembers();
    int randomDCMember = (int) (Math.random() * dcMembers.size());

    String subject = dcMembers.get(randomDCMember).getEffectiveName();
    sendPrompt(ce, subject);
  }

  /**
   * Randomizes the prompt index until there are no recent duplicates.
   *
   * @return random prompt
   */
  private int randomIndex() {
    int index = (int) (Math.random() * pandorasBoxPrompts.size());
    if (!recentValues.contains(index)) {
      recentOrder.add(index);
      recentValues.add(index);
      if (recentOrder.size() == 50) {
        recentValues.remove(recentOrder.poll());
      }
      return index;
    }
    return randomIndex();
  }
}
