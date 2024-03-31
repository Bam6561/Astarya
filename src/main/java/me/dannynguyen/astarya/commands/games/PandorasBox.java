package me.dannynguyen.astarya.commands.games;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import me.dannynguyen.astarya.commands.owner.Settings;
import me.dannynguyen.astarya.enums.BotMessage;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;

import java.util.List;

/**
 * Command invocation that sends a random scenario prompt.
 * <p>
 * The prompt's subject is substituted if it has a field to support the user's given parameters.
 *
 * @author Danny Nguyen
 * @version 1.8.13
 * @since 1.6.11
 */
public class PandorasBox extends Command {
  /**
   * Pandoras box prompts.
   */
  private final List<String> pandorasBoxPrompts;

  /**
   * Associates the command with its properties.
   *
   * @param pandorasBoxPrompts pandoras box prompts
   */
  public PandorasBox(List<String> pandorasBoxPrompts) {
    this.name = "pandorasbox";
    this.aliases = new String[]{"pandorasbox", "pb"};
    this.arguments = "[0]Self [1]VC/DC/Name [2 ++]Phrase";
    this.help = "Sends a random scenario prompt.";
    this.pandorasBoxPrompts = pandorasBoxPrompts;
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
      case 0 -> sendPrompt(ce, ce.getMember().getNickname()); // Target: Self
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
    int randomPrompt = (int) (Math.random() * pandorasBoxPrompts.size());

    String prompt = pandorasBoxPrompts.get(randomPrompt);
    prompt = prompt.replace("<subject>", subject);

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
}
