package commands.owner;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

public class BuildEmbed extends Command {
  EventWaiter waiter;
  private int fieldAmount;
  private int fieldsAdded;

  public BuildEmbed(EventWaiter waiter) {
    this.name = "buildembed";
    this.aliases = new String[]{"buildembed", "embed"};
    this.arguments = "[1]9 Character Switch";
    this.help = "Builds embeds using the Discord message line.";
    this.ownerCommand = true;
    this.waiter = waiter;
  }

  @Override
  protected void execute(CommandEvent ce) {
    Settings.deleteInvoke(ce);
    String[] args = ce.getMessage().getContentRaw().split("\\s");
    int arguments = args.length;
    switch (arguments) {
      // Character switch reminder
      case 1 -> ce.getChannel().sendMessage("Refer to the help documentation of BuildEmbed for more details."
              + "\n1-Title | 2-Author | 3-Description | 4-Fields | 5-Thumbnail | 6-Image | 7-Color | 8-Footer | 9-Timestamp")
          .complete().delete().queueAfter(30, TimeUnit.SECONDS);
      case 2 -> { // Builds embed
        char[] characterSwitches = args[1].toLowerCase().toCharArray();
        if (checkValidCharacterSwitches(characterSwitches)) { // Valid characterswitch
          EmbedBuilder display = new EmbedBuilder();
          containsTitle(ce, waiter, display, characterSwitches);
        } else { // Invalid characterswitch
          ce.getChannel().sendMessage("Invalid character switch.").queue();
        }
      }
      default -> // Invalid argument
          ce.getChannel().sendMessage("Invalid number of arguments").queue();
    }
  }

  private boolean checkValidCharacterSwitches(char[] characterSwitch) {
    if (characterSwitch.length == 9) { // Correct characterswitch length
      for (int i = 0; i < 9; i++) { // All characters are 't' or 'f'
        if (!(characterSwitch[i] == 't' || characterSwitch[i] == 'f')) {
          return false;
        }
      }
      return true;
    }
    return false;
  }

  private void containsTitle(CommandEvent ce, EventWaiter waiter, EmbedBuilder display, char[] characterSwitches) {
    if (characterSwitches[0] == 't') {  // Contains title
      ce.getChannel().sendMessage("__**Title:**__ \n1 - *Title* [text] \n2 - *Title (Hyperlink)* [text>> url]")
          .complete().delete().queueAfter(30, TimeUnit.SECONDS);
      waiter.waitForEvent(GuildMessageReceivedEvent.class,
          w -> w.getAuthor().equals(ce.getAuthor()) && w.getChannel().equals(ce.getChannel()), w -> {
            w.getMessage().delete().queue();
            String[] args = w.getMessage().getContentRaw().split(">> "); // Parse message for arguments
            int arguments = args.length;
            switch (arguments) {
              case 1 -> { // Title [text]
                display.setTitle(args[0]);
                containsAuthor(ce, waiter, display, characterSwitches);
              }
              case 2 -> { // Title (Hyperlink) [text>> url]
                try {
                  display.setTitle(args[0], args[1]);
                  containsAuthor(ce, waiter, display, characterSwitches);
                } catch (IllegalArgumentException error) { // Invalid URL
                  ce.getChannel()
                      .sendMessage("Argument 2 must be a valid URL. "
                          + "Type \"yes\" to continue, anything else otherwise to cancel.")
                      .complete().delete().queueAfter(15, TimeUnit.SECONDS);
                  waiter.waitForEvent(GuildMessageReceivedEvent.class,
                      wInv -> wInv.getAuthor().equals(ce.getAuthor()) && wInv.getChannel().equals(ce.getChannel()),
                      wInv -> {
                        wInv.getMessage().delete().queue();
                        String again = wInv.getMessage().getContentRaw();
                        if (again.equalsIgnoreCase("yes")) {
                          containsTitle(ce, waiter, display, characterSwitches);
                        }
                      }, 15, TimeUnit.SECONDS, () -> {
                      });
                }
              }
              default -> { // Invalid Arguments
                ce.getChannel()
                    .sendMessage(
                        "Invalid number of arguments. Type \"yes\" to continue, anything else otherwise to cancel.")
                    .complete().delete().queueAfter(15, TimeUnit.SECONDS);
                waiter.waitForEvent(GuildMessageReceivedEvent.class,
                    wInv -> wInv.getAuthor().equals(ce.getAuthor()) && wInv.getChannel().equals(ce.getChannel()), wInv -> {
                      wInv.getMessage().delete().queue();
                      String again = wInv.getMessage().getContentRaw();
                      if (again.equalsIgnoreCase("yes")) {
                        containsTitle(ce, waiter, display, characterSwitches);
                      }
                    }, 15, TimeUnit.SECONDS, () -> {
                    });
              }
            }
          }, 1, TimeUnit.MINUTES, () -> {
          });
    } else { // No title
      containsAuthor(ce, waiter, display, characterSwitches);
    }
  }

  private void containsAuthor(CommandEvent ce, EventWaiter waiter, EmbedBuilder display, char[] characterSwitches) {
    if (characterSwitches[1] == 't') { // Contains author
      ce.getChannel().sendMessage(
              "__**Author:**__ \n1 - *Author* [text] \n2 - *Author(Hyperlink)* [text>> url] \n3 - *Author(Hyperlink) & Icon* [text>> url>> iconUrl]")
          .complete().delete().queueAfter(30, TimeUnit.SECONDS);
      waiter.waitForEvent(GuildMessageReceivedEvent.class,
          w -> w.getAuthor().equals(ce.getAuthor()) && w.getChannel().equals(ce.getChannel()), w -> {
            w.getMessage().delete().queue();
            String[] args = w.getMessage().getContentRaw().split(">> "); // Parse message for arguments
            int arguments = args.length;
            switch (arguments) {
              case 1 -> { // Author [text]
                display.setAuthor(args[0]);
                containsDescription(ce, waiter, display, characterSwitches);
              }
              case 2 -> { // Author(Hyperlink) [text>> url]
                try {
                  display.setAuthor(args[0], args[1]);
                  containsDescription(ce, waiter, display, characterSwitches);
                } catch (IllegalArgumentException error) { // Invalid URL
                  ce.getChannel()
                      .sendMessage(
                          "Argument 2 must be a valid URL. Type \"yes\" to continue, anything else otherwise to cancel.")
                      .complete().delete().queueAfter(15, TimeUnit.SECONDS);
                  waiter.waitForEvent(GuildMessageReceivedEvent.class,
                      wInv -> wInv.getAuthor().equals(ce.getAuthor()) && wInv.getChannel().equals(ce.getChannel()),
                      wInv -> {
                        wInv.getMessage().delete().queue();
                        String again = wInv.getMessage().getContentRaw();
                        if (again.equalsIgnoreCase("yes")) {
                          containsAuthor(ce, waiter, display, characterSwitches);
                        }
                      }, 15, TimeUnit.SECONDS, () -> {
                      });
                }
              }
              case 3 -> { // Author(Hyperlink) & Icon [text>> url>> iconUrl]
                try {
                  display.setAuthor(args[0], args[1], args[2]);
                  containsDescription(ce, waiter, display, characterSwitches);
                } catch (IllegalArgumentException error) { // Invalid URL
                  ce.getChannel().sendMessage(
                          "Arguments 2 & 3 must be a valid URL. Type \"yes\" to continue, anything else otherwise to cancel.")
                      .complete().delete().queueAfter(15, TimeUnit.SECONDS);
                  waiter.waitForEvent(GuildMessageReceivedEvent.class,
                      wInv -> wInv.getAuthor().equals(ce.getAuthor()) && wInv.getChannel().equals(ce.getChannel()),
                      wInv -> {
                        wInv.getMessage().delete().queue();
                        String again = wInv.getMessage().getContentRaw();
                        if (again.equalsIgnoreCase("yes")) {
                          containsAuthor(ce, waiter, display, characterSwitches);
                        }
                      }, 15, TimeUnit.SECONDS, () -> {
                      });
                }
              }
              default -> { // Invalid arguments
                ce.getChannel()
                    .sendMessage(
                        "Invalid number of arguments. Type \"yes\" to continue, anything else otherwise to cancel.")
                    .complete().delete().queueAfter(15, TimeUnit.SECONDS);
                waiter.waitForEvent(GuildMessageReceivedEvent.class,
                    wInv -> wInv.getAuthor().equals(ce.getAuthor()) && wInv.getChannel().equals(ce.getChannel()), wInv -> {
                      wInv.getMessage().delete().queue();
                      String again = wInv.getMessage().getContentRaw();
                      if (again.equalsIgnoreCase("yes")) {
                        containsAuthor(ce, waiter, display, characterSwitches);
                      }
                    }, 15, TimeUnit.SECONDS, () -> {
                    });
              }
            }
          }, 1, TimeUnit.MINUTES, () -> {
          });
    } else { // No author
      containsDescription(ce, waiter, display, characterSwitches);
    }
  }

  private void containsDescription(CommandEvent ce, EventWaiter waiter, EmbedBuilder display, char[] characterSwitches) {
    if (characterSwitches[2] == 't') { // Contains description
      ce.getChannel().sendMessage("__**Description:**__ \n[text]").complete().delete().queueAfter(30, TimeUnit.SECONDS);
      waiter.waitForEvent(GuildMessageReceivedEvent.class,
          w -> w.getAuthor().equals(ce.getAuthor()) && w.getChannel().equals(ce.getChannel()), w -> {
            w.getMessage().delete().queue();
            display.setDescription(w.getMessage().getContentRaw());
            containsField(ce, waiter, display, characterSwitches);
          }, 1, TimeUnit.MINUTES, () -> {
          });
    } else { // No description
      containsField(ce, waiter, display, characterSwitches);
    }
  }

  private void containsField(CommandEvent ce, EventWaiter waiter, EmbedBuilder display, char[] characterSwitches) {
    if (characterSwitches[3] == 't') { // Contains field
      ce.getChannel().sendMessage("__**Fields:**__ \n[0-8]").complete().delete().queueAfter(30, TimeUnit.SECONDS);
      waiter.waitForEvent(GuildMessageReceivedEvent.class,
          w -> w.getAuthor().equals(ce.getAuthor()) && w.getChannel().equals(ce.getChannel()), w -> {
            w.getMessage().delete().queue();
            try { // Fields (0-8)
              fieldAmount = Integer.parseInt(w.getMessage().getContentRaw());
              if (fieldAmount > 0 && fieldAmount <= 8) { // Valid range
                addField(ce, waiter, display, characterSwitches);
              } else if (fieldAmount == 0) { // No Fields
                containsThumbnail(ce, waiter, display, characterSwitches);
              } else { // Invalid range
                ce.getChannel()
                    .sendMessage(
                        "You must provide a valid number. Type \"yes\" to continue, anything else otherwise to cancel.")
                    .complete().delete().queueAfter(15, TimeUnit.SECONDS);
                waiter.waitForEvent(GuildMessageReceivedEvent.class,
                    wInv -> wInv.getAuthor().equals(ce.getAuthor()) && wInv.getChannel().equals(ce.getChannel()),
                    wInv -> {
                      wInv.getMessage().delete().queue();
                      String again = wInv.getMessage().getContentRaw();
                      if (again.equalsIgnoreCase("yes")) {
                        containsField(ce, waiter, display, characterSwitches);
                      }
                    }, 15, TimeUnit.SECONDS, () -> {
                    });
              }
            } catch (NumberFormatException error) { // Invalid number
              ce.getChannel().sendMessage("Invalid number. Type \"yes\" to continue, anything else otherwise to cancel.")
                  .complete().delete().queueAfter(15, TimeUnit.SECONDS);
              waiter.waitForEvent(GuildMessageReceivedEvent.class,
                  wInv -> wInv.getAuthor().equals(ce.getAuthor()) && wInv.getChannel().equals(ce.getChannel()), wInv -> {
                    wInv.getMessage().delete().queue();
                    String again = wInv.getMessage().getContentRaw();
                    if (again.equalsIgnoreCase("yes")) {
                      containsField(ce, waiter, display, characterSwitches);
                    }
                  }, 15, TimeUnit.SECONDS, () -> {
                  });
            }
          }, 1, TimeUnit.MINUTES, () -> {
          });
    } else { // No fields
      containsThumbnail(ce, waiter, display, characterSwitches);
    }
  }

  private void addField(CommandEvent ce, EventWaiter waiter, EmbedBuilder display, char[] characterSwitches) {
    if (fieldAmount > fieldsAdded) { // Loop field quantity
      ce.getChannel().sendMessage("__**Field (" + (fieldsAdded + 1) + "):**__ \n[text>> text>> inline]").complete()
          .delete().queueAfter(30, TimeUnit.SECONDS);
      waiter.waitForEvent(GuildMessageReceivedEvent.class,
          w -> w.getAuthor().equals(ce.getAuthor()) && w.getChannel().equals(ce.getChannel()), w -> {
            w.getMessage().delete().queue();
            String[] args = w.getMessage().getContentRaw().split(">> "); // Parse message for arguments
            int arguments = args.length;
            if (arguments == 3) { // [text>> text>> inline]
              if (args[2].equalsIgnoreCase("true") || args[2].equalsIgnoreCase("false")) {
                display.addField(args[0], args[1], Boolean.parseBoolean(args[2]));
                fieldsAdded++;
                addField(ce, waiter, display, characterSwitches);
              } else {
                ce.getChannel().sendMessage(
                        "Argument 3 must be \"true\" or \"false\". Type \"yes\" to continue, anything else otherwise to cancel.")
                    .complete().delete().queueAfter(15, TimeUnit.SECONDS);
                waiter.waitForEvent(GuildMessageReceivedEvent.class,
                    wInv -> wInv.getAuthor().equals(ce.getAuthor()) && wInv.getChannel().equals(ce.getChannel()),
                    wInv -> {
                      wInv.getMessage().delete().queue();
                      String again = wInv.getMessage().getContentRaw();
                      if (again.equalsIgnoreCase("yes")) {
                        addField(ce, waiter, display, characterSwitches);
                      }
                    }, 15, TimeUnit.SECONDS, () -> {
                    });
              }
            } else { // Invalid arguments
              ce.getChannel()
                  .sendMessage(
                      "Invalid number of arguments. Type \"yes\" to continue, anything else otherwise to cancel.")
                  .complete().delete().queueAfter(15, TimeUnit.SECONDS);
              waiter.waitForEvent(GuildMessageReceivedEvent.class,
                  wInv -> wInv.getAuthor().equals(ce.getAuthor()) && wInv.getChannel().equals(ce.getChannel()), wInv -> {
                    wInv.getMessage().delete().queue();
                    String again = wInv.getMessage().getContentRaw();
                    if (again.equalsIgnoreCase("yes")) {
                      addField(ce, waiter, display, characterSwitches);
                    }
                  }, 15, TimeUnit.SECONDS, () -> {
                  });
            }
          }, 1, TimeUnit.MINUTES, () -> {
          });
    } else { // No fields
      containsThumbnail(ce, waiter, display, characterSwitches);
    }
  }

  private void containsThumbnail(CommandEvent ce, EventWaiter waiter, EmbedBuilder display, char[] characterSwitches) {
    if (characterSwitches[4] == 't') { // Contains thumbnail
      ce.getChannel().sendMessage("__**Thumbnail:**__ \n[url]").complete().delete().queueAfter(30, TimeUnit.SECONDS);
      waiter.waitForEvent(GuildMessageReceivedEvent.class,
          w -> w.getAuthor().equals(ce.getAuthor()) && w.getChannel().equals(ce.getChannel()), w -> { //
            w.getMessage().delete().queue();
            try { // [url]
              display.setThumbnail(w.getMessage().getContentRaw());
              containsImage(ce, waiter, display, characterSwitches);
            } catch (IllegalArgumentException error) { // Invalid URL
              ce.getChannel()
                  .sendMessage("Must be a valid URL. Type \"yes\" to continue, anything else otherwise to cancel.")
                  .complete().delete().queueAfter(15, TimeUnit.SECONDS);
              waiter.waitForEvent(GuildMessageReceivedEvent.class,
                  wInv -> wInv.getAuthor().equals(ce.getAuthor()) && wInv.getChannel().equals(ce.getChannel()), wInv -> {
                    wInv.getMessage().delete().queue();
                    String again = wInv.getMessage().getContentRaw();
                    if (again.equalsIgnoreCase("yes")) {
                      containsThumbnail(ce, waiter, display, characterSwitches);
                    }
                  }, 15, TimeUnit.SECONDS, () -> {
                  });
            }
          }, 1, TimeUnit.MINUTES, () -> {
          });
    } else { // No thumbnail
      containsImage(ce, waiter, display, characterSwitches);
    }
  }

  private void containsImage(CommandEvent ce, EventWaiter waiter, EmbedBuilder display, char[] characterSwitches) {
    if (characterSwitches[5] == 't') { // Contains image
      ce.getChannel().sendMessage("__**Image:**__ \n[url]").complete().delete().queueAfter(30, TimeUnit.SECONDS);
      waiter.waitForEvent(GuildMessageReceivedEvent.class,
          w -> w.getAuthor().equals(ce.getAuthor()) && w.getChannel().equals(ce.getChannel()), w -> {
            w.getMessage().delete().queue();
            try { // [url]
              display.setImage(w.getMessage().getContentRaw());
              containsColor(ce, waiter, display, characterSwitches);
            } catch (IllegalArgumentException error) { // Invalid URL
              ce.getChannel()
                  .sendMessage("Must be a valid URL. Type \"yes\" to continue, anything else otherwise to cancel.")
                  .complete().delete().queueAfter(15, TimeUnit.SECONDS);
              waiter.waitForEvent(GuildMessageReceivedEvent.class,
                  wInv -> wInv.getAuthor().equals(ce.getAuthor()) && wInv.getChannel().equals(ce.getChannel()), wInv -> {
                    wInv.getMessage().delete().queue();
                    String again = wInv.getMessage().getContentRaw();
                    if (again.equalsIgnoreCase("yes")) {
                      containsImage(ce, waiter, display, characterSwitches);
                    }
                  }, 15, TimeUnit.SECONDS, () -> {
                  });
            }
          }, 1, TimeUnit.MINUTES, () -> {
          });
    } else { // No image
      containsColor(ce, waiter, display, characterSwitches);
    }
  }

  private void containsColor(CommandEvent ce, EventWaiter waiter, EmbedBuilder display, char[] characterSwitches) {
    if (characterSwitches[6] == 't') { // Contains color
      ce.getChannel().sendMessage("__**Color:**__ Automatically applied.").complete().delete().queueAfter(30,
          TimeUnit.SECONDS);
      display.setColor(0x80000f);
      containsFooter(ce, waiter, display, characterSwitches);
    } else if (characterSwitches[6] == 'F') { // No color
      containsFooter(ce, waiter, display, characterSwitches);
    }
  }

  private void containsFooter(CommandEvent ce, EventWaiter waiter, EmbedBuilder display, char[] characterSwitches) {
    if (characterSwitches[7] == 't') { // Contains footer
      ce.getChannel().sendMessage("__**Footer**__ \n1 - *Footer* [text] \n2 - *Footer (Hyperlink)* [text>> iconUrl]")
          .complete().delete().queueAfter(30, TimeUnit.SECONDS);
      waiter.waitForEvent(GuildMessageReceivedEvent.class,
          w -> w.getAuthor().equals(ce.getAuthor()) && w.getChannel().equals(ce.getChannel()), w -> {
            w.getMessage().delete().queue();
            String[] args = w.getMessage().getContentRaw().split(">> "); // Parse message for arguments
            int arguments = args.length;
            switch (arguments) {
              case 1 -> {
                display.setFooter(args[0]);
                containsTimestamp(ce, display, characterSwitches);
              }
              case 2 -> {
                try { // [text>> iconUrl]
                  display.setFooter(args[0], args[1]);
                  containsTimestamp(ce, display, characterSwitches);
                } catch (IllegalArgumentException error) { // Invalid URL
                  ce.getChannel()
                      .sendMessage(
                          "Argument 2 be a valid URL. Type \"yes\" to continue, anything else otherwise to cancel.")
                      .complete().delete().queueAfter(15, TimeUnit.SECONDS);
                  waiter.waitForEvent(GuildMessageReceivedEvent.class,
                      wInv -> wInv.getAuthor().equals(ce.getAuthor()) && wInv.getChannel().equals(ce.getChannel()),
                      wInv -> {
                        wInv.getMessage().delete().queue();
                        String again = wInv.getMessage().getContentRaw();
                        if (again.equalsIgnoreCase("yes")) {
                          containsFooter(ce, waiter, display, characterSwitches);
                        }
                      }, 15, TimeUnit.SECONDS, () -> {
                      });
                }
              }
              default -> { // Invalid Arguments
                ce.getChannel().sendMessage(
                    "Invalid number of arguments. Type \"yes\" to continue, anything else otherwise to cancel.").queue();
                waiter.waitForEvent(GuildMessageReceivedEvent.class,
                    wInv -> wInv.getAuthor().equals(ce.getAuthor()) && wInv.getChannel().equals(ce.getChannel()), wInv -> {
                      wInv.getMessage().delete().queue();
                      String again = wInv.getMessage().getContentRaw();
                      if (again.equalsIgnoreCase("yes")) {
                        containsFooter(ce, waiter, display, characterSwitches);
                      }
                    }, 15, TimeUnit.SECONDS, () -> {
                    });
              }
            }
          }, 1, TimeUnit.MINUTES, () -> {
          });
    } else { // No footer
      containsTimestamp(ce, display, characterSwitches);
    }
  }

  private void containsTimestamp(CommandEvent ce, EmbedBuilder display, char[] characterSwitches) {
    if (characterSwitches[8] == 't') { // Contains timestamp
      ce.getChannel().sendMessage("__**Timestamp:**__ Automatically applied.").queue();
      display.setTimestamp(Instant.now());
    }
    ce.getChannel().sendMessage(display.build()).queue();
  }
}