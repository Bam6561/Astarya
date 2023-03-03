<div align="center">
  <h3>LucyferBot</h3>
  <img src="https://i.ibb.co/q7vdKJJ/lucyfer-Bot128x128.png" alt="lucyfer-Bot128x128">

[![Stargazers][stars-shield]][stars-url] [![License][license-shield]][license-url]

</div>

## About The Project

Discord bot written in Java using the JDA wrapper. Optional Spotify API integration for more audio playback
functionality. 

See the [Wiki](https://github.com/Bam6561/LucyferBot/wiki) for documentation on its commands.

### Built With

- [Dotenv](https://github.com/cdimascio/dotenv-java)
- [JSON](https://github.com/stleary/JSON-java)
- [JDA](https://github.com/DV8FromTheWorld/JDA)
- [JDA Chewtils](https://github.com/Chew/JDA-Chewtils)
- [LavaPlayer](https://github.com/sedmelluq/lavaplayer)
- [Spotify](https://developer.spotify.com/dashboard/)
- [Spotify Web API](https://github.com/spotify-web-api-java/spotify-web-api-java)

## Setup

1. Clone the repo.
2. Create a [Discord Application](https://discord.com/developers/docs/intro).
3. Add the Bot functionality in the Application's settings.
4. Copy your bot's login token.
5. Create an .env file.
6. In your .env file, write a line containing `BOT_TOKEN =` followed by your bot's login token enclosed by double
   quotes. It should look like this: `BOT_TOKEN = "12345"`.
7. Navigate to OAUTH2, then go to the URL Generator section in the dropdown.
8. Under scopes, checkmark `Bot`, and optionally add the `Manage Messages` permission if you plan to use the bot to
   delete messages for you.
9. Finally, invite the bot to your Discord server by copying the generated URL and pasting it into your browser.
10. Optionally, if you want the bot to be able to interact with Spotify links containing songs, playlists, and albums,
    then create two other variables in your .env file called `SPOTIFY_CLIENT_ID` and `SPOTIFY_CLIENT_SECRET`.
11. Go to [Spotify For Developers](https://developer.spotify.com/) and create an account.
12. Go to your Dashboard and create a Spotify Application.
13. Repeating the process in step #6, add in the .env file the tokens for your Client ID and Client Secret.

## Contributing

1. Fork the Project
2. Create your Feature Branch
3. Commit your Changes
4. Push to the Branch
5. Open a Pull Request

## License

Distributed under the MIT License. See `LICENSE` for more information.

## Contact

Danny Nguyen - [LinkedIn](https://www.linkedin.com/in/ndanny09/) - ndanny09@gmail.com

Project Link: https://github.com/Bam6561/LucyferBot

## Acknowledgements

- [Kody Simpson](https://www.youtube.com/c/KodySimpson)
- [MenuDocs](https://www.youtube.com/c/MenuDocs)
- [TechToolBox](https://www.youtube.com/c/TechToolboxOfficial)
- [README Template](https://github.com/othneildrew/Best-README-Template#prerequisites)

[stars-shield]: https://img.shields.io/github/stars/Bam6561/LucyferBot

[stars-url]: https://github.com/Bam6561/LucyferBot/stargazers

[license-shield]: https://img.shields.io/github/license/Bam6561/LucyferBot

[license-url]: https://github.com/Bam6561/LucyferBot/blob/main/LICENSE

