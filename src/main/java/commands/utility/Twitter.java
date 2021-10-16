package commands.utility;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import commands.owner.Settings;
import io.github.cdimascio.dotenv.Dotenv;
import io.github.redouane59.twitter.TwitterClient;
import io.github.redouane59.twitter.dto.user.User;
import io.github.redouane59.twitter.signature.TwitterCredentials;
import net.dv8tion.jda.api.EmbedBuilder;

public class Twitter extends Command {
  public Twitter() {
    this.name = "twitter";
    this.aliases = new String[]{"twitter", "tw"};
    this.arguments = "[1]TwitterHandle";
    this.help = "Provides information about a Twitter user.";
  }

  @Override
  protected void execute(CommandEvent ce) {
    Settings.deleteInvoke(ce);
    String[] args = ce.getMessage().getContentRaw().split("\\s"); // Parse message for arguments
    int arguments = args.length;
    if (arguments == 2) { // Search Twitter user
      try {
        searchTwitterUser(ce, args);
      } catch (NullPointerException error) { // Invalid Twitter handle
        ce.getChannel().sendMessage("Twitter handle not found.").queue();
      }
    } else { // Invalid number of arguments
      ce.getChannel().sendMessage("Invalid number of arguments.").queue();
    }
  }

  private void searchTwitterUser(CommandEvent ce, String[] args) {
    String twitterHandle = args[1];
    Dotenv dotenv = Dotenv.load();
    // Create a Twitter object for API access
    TwitterClient twitterClient = new TwitterClient(TwitterCredentials.builder()
        .accessToken(dotenv.get("TWITTER_ACCESS_TOKEN"))
        .accessTokenSecret(dotenv.get("TWITTER_ACCESS_TOKEN_SECRET"))
        .apiKey(dotenv.get("TWITTER_API_KEY"))
        .apiSecretKey(dotenv.get("TWITTER_API_SECRET_KEY"))
        .build());
    // Retrieve Twitter user from Twitter handle
    User user = twitterClient.getUserFromUserName(twitterHandle);
    // Create embed display for user
    EmbedBuilder display = new EmbedBuilder();
    // Display name, Twitter handle, & Twitter link
    display.setTitle(user.getDisplayedName() + " @" + user.getName(), "https://twitter.com/" + user.getName());
    display.setThumbnail(user.getProfileImageUrl()); // Profile picture
    StringBuilder description = new StringBuilder();
    // Verification & Publicity of Account
    if (user.isVerified()) {
      description.append("**[Verified]** ");
    } else {
      description.append("**[Unverified]** ");
    }
    if (user.isProtectedAccount()) {
      description.append("**[Private]**").append("\n");
    } else {
      description.append("**[Public]**").append("\n");
    }
    // Location
    if (!(user.getLocation() == null)) {
      description.append("**Location:** ").append(user.getLocation()).append(" \n");
    }
    // Following, Followers, & Number of Tweets
    description.append("**Following:** `").append(user.getFollowingCount()).append("` ");
    description.append("**Followers:** `").append(user.getFollowersCount()).append("` ");
    description.append("**Tweets:** `").append(user.getTweetCount()).append("` \n");
    // Website
    if (!(user.getUrl().equals(""))) {
      description.append("**Website:** ").append(user.getUrl());
    }
    // User bio
    display.addField("__**Bio:**__", user.getDescription(), false);
    // User pinned tweet
    if (!(user.getPinnedTweet() == null)) {
      display.addField("__**Pinned Tweet:**__", user.getPinnedTweet().getText(), false);
    }
    display.setDescription(description);
    Settings.sendEmbed(ce, display);
  }
}
