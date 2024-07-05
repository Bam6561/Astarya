<a name="top"></a>

<!-- LOGO -->
<div align="center">
  <h3>Astarya</h3>
  <img src="https://i.ibb.co/9sFBrX8/astarya-bot128x128.png" alt="astarya-bot128x128">

  [![Stargazers][stars-shield]][stars-url] [![License][license-shield]][license-url]

  <a href="https://github.com/Bam6561/Astarya/wiki">Wiki</a>
</div>

<!-- TABLE OF CONTENTS -->
<details open>
  <summary><b> Table of Contents </b></summary>
  <ol>
    <li><a href="#about-the-project"> About The Project </a></li>
      <ul>
        <li><a href="#built-with"> Built With </a></li>
      </ul>
    <li><a href="#setup"> Setup </a></li>
      <ul>
        <li><a href="#core-function"> Core Function </a></li>
        <li><a href="#spotify-integration"> Spotify Integration </a></li>
      </ul>
    <li><a href="#contributing"> Contributing </a></li>
    <li><a href="#license"> License </a></li>
    <li><a href="#contact"> Contact </a></li>
    <li><a href="#acknowledgements"> Acknowledgements </a></li>
  </ol>
</details>

<!-- ABOUT THE PROJECT -->
## About The Project
Discord bot written in Java using the JDA wrapper. 

Optional Spotify API integration for more audio playback functionality. 

See the [Wiki](https://github.com/Bam6561/Astarya/wiki) for documentation on its commands.

Formerly known as LucyferBot.

<p align="right"><a href="#top">Back to Top</a></p>

### Built With
* [Dotenv](https://github.com/cdimascio/dotenv-java)
* [JSON](https://github.com/stleary/JSON-java)
* [JDA](https://github.com/DV8FromTheWorld/JDA)
* [JDA Chewtils](https://github.com/Chew/JDA-Chewtils)
* [LavaPlayer](https://github.com/sedmelluq/lavaplayer)
* [Spotify](https://developer.spotify.com/dashboard/)
* [Spotify Web API](https://github.com/spotify-web-api-java/spotify-web-api-java)

<p align="right"><a href="#top">Back to Top</a></p>

<!-- SETUP -->
## Setup

### Core Function
1. Clone the repo.
2. Create a [Discord Application](https://discord.com/developers/docs/intro).
3. Add the Bot functionality in the Application's settings.
4. Copy your bot's login token.
5. Create an .env file.
6. In your .env file, write a line containing `BOT_TOKEN =` followed by your bot's login token enclosed by double
   quotes. It should look like this: `BOT_TOKEN = "12345"`.
7. Navigate to OAUTH2, then go to the URL Generator section in the dropdown.
8. Under scopes, checkmark `Bot`, and optionally add the following permissions:
- `Manage Messages` to delete messages
- `Manage Roles` to manage color roles
9. Finally, invite the bot to your Discord server by copying the generated URL and pasting it into your browser.

<p align="right"><a href="#top">Back to Top</a></p>

### Spotify Integration
1. Optionally, if you want the bot to be able to interact with Spotify links containing songs, playlists, and albums,
    then create two other variables in your .env file called `SPOTIFY_CLIENT_ID` and `SPOTIFY_CLIENT_SECRET`.
2. Go to [Spotify For Developers](https://developer.spotify.com/) and create an account.
3. Go to your Dashboard and create a Spotify Application.
4. Repeating the process in step #6 for Core Function setup, add in the .env file the tokens for your Client ID and Client Secret.

<p align="right"><a href="#top">Back to Top</a></p>

<!-- CONTRIBUTING -->
## Contributing
1. Fork the Project
2. Create your Feature Branch
3. Commit your Changes
4. Push to the Branch
5. Open a Pull Request

<p align="right"><a href="#top">Back to Top</a></p>

<!-- LICENSE -->
## License
Distributed under the MIT License. See `LICENSE` for more information.

<p align="right"><a href="#top">Back to Top</a></p>

<!-- CONTACT -->
## Contact
Danny Nguyen - [LinkedIn](https://www.linkedin.com/in/ndanny09/) - ndanny09@gmail.com

Project Link: https://github.com/Bam6561/Astarya

<p align="right"><a href="#top">Back to Top</a></p>

<!-- ACKNOWLEDGEMENTS -->
## Acknowledgements
* [Kody Simpson](https://www.youtube.com/c/KodySimpson)
* [MenuDocs](https://www.youtube.com/c/MenuDocs)
* [TechToolBox](https://www.youtube.com/c/TechToolboxOfficial)
* [README Template](https://github.com/othneildrew/Best-README-Template#prerequisites)

<p align="right"><a href="#top">Back to Top</a></p>

<!-- SHIELDS -->
[stars-shield]: https://img.shields.io/github/stars/Bam6561/Astarya
[stars-url]: https://github.com/Bam6561/Astarya/stargazers
[license-shield]: https://img.shields.io/github/license/Bam6561/Astarya
[license-url]: https://github.com/Bam6561/Astarya/blob/main/LICENSE
