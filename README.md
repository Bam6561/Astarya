# LucyferBot

<img src="https://i.ibb.co/QNmmDqW/lucyfer-Bot.png" alt="lucyfer-Bot" width="128" height="128">

## About The Project

Java-based Discord bot written with JDA API.

#### Commands:

- About: (Credits, Help, Info)
- Audio: (ClearQueue, Join, Leave, Loop, NowPlaying, Pause, Play, PlayNext, Queue, Remove, SearchTrack, SetPosition,
  Shuffle, Skip, Swap)
- Games (Choice, Flip, HighOrLow, Roll)
- HoloLive (HololiveTags)
- Miscellaneous: (Echo, Ping, Random)
- Owner: (BuildEmbed, Delete, Settings, Shutdown, Volume)
- Promotion: (DungeonArchives)
- Utility (Avatar, Emote, Remind, ServerInfo, Twitter, WhoIs)

[Version History](https://ndanny09.github.io/lucyferbot.html)

### Built With

* [Java](https://www.java.com/en/)
* [JDA](https://github.com/DV8FromTheWorld/JDA)
* [JDA Utilities](https://github.com/JDA-Applications/JDA-Utilities)
* [LavaPlayer](https://github.com/sedmelluq/lavaplayer)
* [Spotify Web API Java](https://github.com/spotify-web-api-java/spotify-web-api-java)
* [Twittered](https://github.com/redouane59/twittered)

## Setup

#### 1.2.15.3 & below:

<ol>
  <li> Clone the repo. </li>
  <li> Create a .env file with a BOT_TOKEN variable in your directory. </li>
  <li> Create an application on the Discord Developer Portal (https://discord.com/developers/docs/intro). </li>
  <li> Under the Bot tab, retrieve a authentication token string to insert into BOT_TOKEN that allow the bots to login to Discord. </li>
  <li> Under the OAuth2 tab, generate a link that allows you to invite the bot to any discord servers you manage. </li>
  <li> Give the bot MANAGE_MESSAGES permission in order for the CLEAR command to work. Other than that, it is ready to go. </li>
</ol>

#### 1.3.0 - 1.3.3.4:

<ol>
  <li> Follow 1.2.15.3 and below's instructions.</li>
  <li> Inside your .env file, include a SPOTIFY_CLIENT_ID & SPOTIFY_CLIENT_SECRET variable.</li>
  <li> Create an application on the Spotify Developer Portal (https://developer.spotify.com/dashboard/). </li>
  <li> After the application is created, you will be provided a Client ID and Client Secret to use in the .env file. </li>
</ol>

#### 1.3.4+:

<ol>
  <li> Follow 1.3.3.4 and below's instructions.</li>
  <li> Inside your .env file, include a TWITTER_ACCESS_TOKEN, TWITTER_ACCESS_TOKEN_SECRET, TWITTER_API_KEY, & TWITTER_API_SECRET_KEY variable.</li>
  <li> Create an application on the Twitter Developer Portal (https://developer.twitter.com/en). </li>
  <li> After the application is created, you will be provided a Twitter Access Token, Twitter Access Token Secret, Twitter API key, & Twitter API Secret Key to use in the .env file. </li>
</ol>

## Contributing

<ol>
  <li> Fork the Project </li>
  <li> Create your Feature Branch  </li>
  <li> Commit your Changes  </li>
  <li> Push to the Branch  </li>
  <li> Open a Pull Request </li>
</ol>

## License

Distributed under the MIT License. See `LICENSE` for more information.

## Contact

Danny Nguyen - [LinkedIn](https://www.linkedin.com/in/ndanny09/) - ndanny09@gmail.com <br>
Project Link: https://github.com/ndanny09/LucyferBot

## Acknowledgements

* [Kody Simpson](https://www.youtube.com/c/KodySimpson)
* [MenuDocs](https://www.youtube.com/c/MenuDocs)
* [TechToolBox](https://www.youtube.com/c/TechToolboxOfficial)
* [README Template](https://github.com/othneildrew/Best-README-Template#prerequisites)
