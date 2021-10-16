package commands.hololive;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import commands.owner.Settings;
import net.dv8tion.jda.api.EmbedBuilder;

public class HololiveTags extends Command {
  public HololiveTags() {
    this.name = "hololiveTags";
    this.aliases = new String[]{"hololiveTags", "holotags", "tags"};
    this.arguments = "[1]FirstOrLastName";
    this.help = "Provides the requested HoloLive member's Twitter tags.";
  }

  @Override
  protected void execute(CommandEvent ce) {
    Settings.deleteInvoke(ce);
    String[] args = ce.getMessage().getContentRaw().split("\\s"); // Parse message for arguments
    int arguments = args.length;
    if (arguments == 2) {
      String name = args[1].toLowerCase();
      matchHololiveName(ce, name);
    } else {
      ce.getChannel().sendMessage("Invalid number of arguments.").queue();
    }
  }

  private void matchHololiveName(CommandEvent ce, String name) {
    EmbedBuilder display = new EmbedBuilder();
    switch (name) {
      // Gen 0
      case "sora", "tokino" -> sendEmbed(ce, display, "Tokino Sora @tokino_sora",
          "https://twitter.com/tokino_sora",
          "https://static.wikia.nocookie.net/virtualyoutuber/images/5/52/" +
              "Tokino_Sora_-_Icon.png/revision/latest/scale-to-width-down/100?cb=20210901132939",
          """
              **General:** #ときのそら
              **Stream Talk:** #ときのそら生放送
              **Fan Art:** #soraArt
              **Hololy:** #ときのそら撮ったよ""");
      case "roboco" -> sendEmbed(ce, display, "Roboco-san @robocosan",
          "https://twitter.com/robocosan",
          "https://static.wikia.nocookie.net/virtualyoutuber/images/7/70/" +
              "Roboco_San_-_Icon.png/revision/latest/scale-to-width-down/100?cb=20210901132755",
          """
              **General:** #robo_co
              **Stream Talk:** #ロボ子生放送
              **Fans:** #ろぼさー\s
              **Fan Art:** #ロボ子Art\s
              **Schedule:** #ロボジュール\s
              **Commentary On Released Voice Dramas:** #聴いたよロボ子さん\s
              **Hololy:** #ロボ子さんと一緒\s
              **Video Clips:** #ロボ子レクション\s""");
      case "miko", "sakura" -> sendEmbed(ce, display, "Sakura Miko @sakuramiko35",
          "https://twitter.com/sakuramiko35",
          "https://static.wikia.nocookie.net/virtualyoutuber/images/e/e3/" +
              "Sakura_Miko_-_Icon.png/revision/latest/scale-to-width-down/100?cb=20210901132810",
          """
              **General:** #さくらみこ\s
              **Stream Talk:** #みこなま\s
              **Fan Art:** #miko_Art\s
              **Video Clips:** #ミコミコ動画\s""");
      case "suisei", "hoshimachi" -> sendEmbed(ce, display, "Hoshimachi Suisei @suisei_hosimati",
          "https://twitter.com/suisei_hosimati",
          "https://static.wikia.nocookie.net/virtualyoutuber/images/4/43/" +
              "Hoshimachi_Suisei_-_Icon.png/revision/latest/scale-to-width-down/100?cb=20210901132220",
          """
              **General:** #星街すいせい\s
              **Stream Talk:** #ほしまちすたじお\s
              **Fans:** #星詠み\s
              **Fan Art:** #ほしまちぎゃらりー\s
              **Music Space:** #すいせいみゅーじっく\s""");
      case "azki" -> sendEmbed(ce, display, "AZKi @AZKi_VDiVA",
          "https://twitter.com/AZKi_VDiVA",
          "https://static.wikia.nocookie.net/virtualyoutuber/images/9/9f/" +
              "AZKi_-_Icon.png/revision/latest/scale-to-width-down/100?cb=20210901132054",
          """
              **Stream Talk:** #AZKi生放送\s
              **Stream Talk For Home Streams:** #あずきんち\s
              **Fan Art:** #AZKiART\s
              **Music:** #AZKiレビュー\s
              **Commentary On Released Voice Dramas:** #AZ声\s
              **Hololy:** #どこAZ\s
              **Memes:** #コラAZ\s""");
      // Gen 1
      case "mel", "nozora" -> sendEmbed(ce, display, "Nozora Mel @yozoramel",
          "https://twitter.com/yozoramel",
          "https://static.wikia.nocookie.net/virtualyoutuber/images/7/7e/" +
              "Yozora_Mel_-_Icon.png/revision/latest/scale-to-width-down/80?cb=20210901133121",
          """
              **General:** #夜空メル\s
              **Stream:** #メル生放送\s
              **Fan Art:** #メルArt\s
              **Video Clips:** #かぷかぷ動画\s""");
      case "fubuki", "shirakami" -> sendEmbed(ce, display, "Shirakami Fubuki @shirakamifubuki",
          "https://twitter.com/shirakamifubuki",
          "https://static.wikia.nocookie.net/virtualyoutuber/images/5/5f/" +
              "Shirakami_Fubuki_-_Icon.png/revision/latest/scale-to-width-down/80?cb=20210901132823",
          """
              **General:** 白上フブキ\s
              **Stream Talk:** #フブキch\s
              **Schedule:** #白上式手抜きスケジュール\s
              **Fans:** #すこん部\s
              **Fan Art:** #絵フブキ\s
              **Video Clips:** #フブ切り\s
              **Hololy:** #フブキ散歩\s""");
      case "matsuri", "natsuiro" -> sendEmbed(ce, display, "Natsuiro Matsuri @natsuiromatsuri",
          "https://twitter.com/natsuiromatsuri",
          "https://static.wikia.nocookie.net/virtualyoutuber/images/9/90/" +
              "Natsuiro_Matsuri_-_Icon.png/revision/latest/scale-to-width-down/80?cb=20210901132606",
          """
              **General:** #夏色まつり\s
              **Stream Talk:** #夏まつch\s
              **Fan Art:** #祭絵 \s
              **Fan art (NSFW):** #まつりは絵っち\s
              **Video Clips:** #抜いたぞまつり\s
              **Commentary On Released Voice Dramas:** #きいたよまつり\s""");
      case "aki", "rosenthal" -> sendEmbed(ce, display, "Aki Rosenthal @akirosenthal",
          "https://twitter.com/akirosenthal",
          "https://static.wikia.nocookie.net/virtualyoutuber/images/7/70/" +
              "Aki_Rosenthal_-_Icon.png/revision/latest/scale-to-width-down/80?cb=20210901131926",
          """
              **General:** #アキ・ローゼンタール\s
              **Stream Talk:** #アキびゅーわーるど\s
              **Fans:** #ロゼ隊\s
              **Fan Art:** #アロ絵\s
              **Fan Art (Worldwide):** #Akimage\s
              **Fan Art (NSFW):** #スケベなアロ絵\s
              **Video Clips:** #切り抜きロゼ\s
              **MMD:** #アキロゼMMD\s""");
      case "haato", "haachama" -> sendEmbed(ce, display, "Akai Haato @akaihaato",
          "https://twitter.com/akaihaato",
          "https://static.wikia.nocookie.net/virtualyoutuber/images/b/b7/" +
              "Akai_Haato_-_Icon.png/revision/latest/scale-to-width-down/80?cb=20210901131912",
          """
              **General:** #赤井はあと\s
              **Fans:** #はあとん\s
              **Fan Art:** #はあとart\s
              **Stream Talk:** #はあちゃまなう or #はあちゃま病\s
              **Stream Talk For English Streams:** #赤井イングリッシュ\s
              **Video Clips:** #ここ好きはあと様 or #はあちゃま切り抜き\s""");
      // Gen 2
      case "aqua", "minato" -> sendEmbed(ce, display, "Minato Aqua @minatoaqua",
          "https://twitter.com/minatoaqua",
          "https://static.wikia.nocookie.net/virtualyoutuber/images/f/f8/" +
              "Minato_Aqua_-_Icon.png/revision/latest/scale-to-width-down/100?cb=20210901132414",
          """
              **General:** 湊あくあ\s
              **Stream Talk:** #湊あくあ生放送\s
              **Fans:** #あくあクルー\s
              **Fan Art:** #あくあーと\s
              **Video Clips:** #切り抜きあくたん\s
              **MMD:** #湊あくあMMD\s""");
      case "shion", "murasaki" -> sendEmbed(ce, display, "Murasaki Shion @murasakishionch",
          "https://twitter.com/murasakishionch",
          "https://static.wikia.nocookie.net/virtualyoutuber/images/1/12/" +
              "Murasaki_Shion_-_Icon.png/revision/latest/scale-to-width-down/100?cb=20210901132516",
          """
              **General/Stream Talk:** #紫咲シオン\s
              **Fans:** #塩っ子\s
              **Fan Art:** #シオンの書物\s
              **MMD:** #紫咲シオンMMD
              **Clips:** #コレクシオン\s""");
      case "ayame", "nakiri" -> sendEmbed(ce, display, "Nakiri Ayame @nakiriayame",
          "https://twitter.com/nakiriayame",
          "https://static.wikia.nocookie.net/virtualyoutuber/images/d/d1/" +
              "Nakiri_Ayame_-_Icon.png/revision/latest/scale-to-width-down/100?cb=20210901132530",
          """
              **Live Stream:** #百鬼あやめch\s
              **Fan Art:** #百鬼絵巻\s
              **Related Tweets:** #あやめ夜行\s""");
      case "choco", "yuzuki" -> sendEmbed(ce, display, "Yuzuki Choco @yuzukichococh",
          "https://twitter.com/yuzukichococh",
          "https://static.wikia.nocookie.net/virtualyoutuber/images/2/20/" +
              "Yuzuki_Choco_-_Icon.png/revision/latest/scale-to-width-down/100?cb=20210901133147",
          """
              **Stream Talk:** #癒月診療所\s
              **Fan Art:** #しょこらーと\s
              **Fans:** #ちょこカルテ\s
              **Video Clips:** #ちょこ先生をみろ\s""");
      case "subaru", "oozora" -> sendEmbed(ce, display, "Oozora Subaru @oozorasubaru",
          "https://twitter.com/oozorasubaru",
          "https://static.wikia.nocookie.net/virtualyoutuber/images/4/46/" +
              "Oozora_Subaru_-_Icon.png/revision/latest/scale-to-width-down/100?cb=20210901132728",
          """
              **General:** #大空スバル\s
              **Stream Talk:** #生スバル\s
              **Fans:** #スバ友\s
              **Fan Art:** #プロテインザスバル\s
              **Video Clips:** #きりぬきスバル\s""");
      // Hololive Gamers
      case "mio", "ookami" -> sendEmbed(ce, display, "Ookami Mio @ookamimio",
          "https://twitter.com/ookamimio",
          "https://static.wikia.nocookie.net/virtualyoutuber/images/2/25/" +
              "Ookami_Mio_-_Icon.png/revision/latest/scale-to-width-down/100?cb=20210901132711",
          """
              **General:** #大神ミオ\s
              **Stream Talk:** #ミオかわいい\s
              **Schedule:** #ミオじゅ〜る\s
              **Fan Art:** #みおーん絵\s
              **Commentary On Released Voice Dramas:** #ボイスのミオかわいい\s
              **With Mio:** #ミオといっしょ\s""");
      case "okayu", "nekomata" -> sendEmbed(ce, display, "Nekomata Okayu @nekomataokayu",
          "https://twitter.com/nekomataokayu",
          "https://static.wikia.nocookie.net/virtualyoutuber/images/5/5e/" +
              "Nekomata_Okayu_-_Icon.png/revision/latest/scale-to-width-down/100?cb=20210901132624",
          """
              **General:** #猫又おかゆ\s
              **Stream Talk:** #生おかゆ\s
              **Fans:** #おにぎりゃー\s
              **Fan Art:** #絵かゆ\s
              **Fan Art (NSFW):** #エロおにぎり\s
              **Video Clips:** #おに切り\s
              **MMD:** #みんなのおかゆ\s""");
      case "korone", "inugami" -> sendEmbed(ce, display, "Inugami Korone @inugamikorone",
          "https://twitter.com/inugamikorone",
          "https://static.wikia.nocookie.net/virtualyoutuber/images/3/3c/" +
              "Inugami_Korone_-_Icon.png/revision/latest/scale-to-width-down/100?cb=20210901132256",
          """
              **General:** #戌神ころね\s
              **Stream Talk:** #生神もんざえもん\s
              **Fans:** #ころねすきー or #ごまキング\s
              **Fan Art:** #できたてころね\s
              **Video Clips:** #ちょこっところね\s
              **MMD:** #戌神ころねMMD\s""");
      // Gen 3
      case "pekora", "usada" -> sendEmbed(ce, display, "Usada Pekora @usadapekora",
          "https://twitter.com/usadapekora",
          "https://static.wikia.nocookie.net/virtualyoutuber/images/3/3f/" +
              "Usada_Pekora_-_Icon.png/revision/latest/scale-to-width-down/100?cb=20210901133050",
          """
              **General:** #兎田ぺこら\s
              **Stream Talk:** #ぺこらいぶ\s
              **Fans:** #野うさぎ同盟\s
              **Fan Art:** #ぺこらーと\s
              **Video Clips:** #ひとくちぺこら\s""");
      case "rushia", "uruha" -> sendEmbed(ce, display, "Uruha Rushia @uruharushia",
          "https://twitter.com/uruharushia",
          "https://static.wikia.nocookie.net/virtualyoutuber/images/1/14/" +
              "Uruha_Rushia_-_Icon.png/revision/latest/scale-to-width-down/100?cb=20210901133032",
          """
              **General:** 潤羽るしあ \s
              **Stream Talk:** #るしあらいぶ\s
              **Fans:** #ふぁんでっど\s
              **Fan Art:** #絵クロマンサー\s
              **Video Clips:** #きるしあ\s""");
      case "flare", "shiranui" -> sendEmbed(ce, display, "Shiranui Flare @shiranuiflare",
          "https://twitter.com/shiranuiflare",
          "https://static.wikia.nocookie.net/virtualyoutuber/images/4/47/" +
              "Shiranui_Flare_-_Icon.png/revision/latest/scale-to-width-down/100?cb=20210901132842",
          """
              **General:** #不知火フレア\s
              **Stream Talk:** #フレアストリーム\s
              **Fans:** #エルフレンド\s
              **Fan Art:** #しらぬえ\s
              **Commentary On Released Voice Dramas:** #きいたよフレア\s
              **Flare's Recommendations:** #聞いてよフレア\s
              **Video Clips:** #切りぬい\s""");
      case "noel", "shirogane" -> sendEmbed(ce, display, "Shirogane Noel @shiroganenoel",
          "https://twitter.com/shiroganenoel",
          "https://static.wikia.nocookie.net/virtualyoutuber/images/3/3b/" +
              "Shirogane_Noel_-_Icon.png/revision/latest/scale-to-width-down/100?cb=20210901132858",
          """
              **General:** #白銀ノエル\s
              **Stream Talk:** #ノエルーム\s
              **Fans:** #白銀聖騎士団\s
              **Fan Art:** #ノエラート\s
              **Fan Art (NSFW):** #オークアート\s
              **Video Clips:** #クリ抜き太郎\s""");
      case "marine", "houshou" -> sendEmbed(ce, display, "Houshou Marine @houshoumarine",
          "https://twitter.com/houshoumarine",
          "https://static.wikia.nocookie.net/virtualyoutuber/images/f/f7/" +
              "Houshou_Marine_-_Icon.png/revision/latest/scale-to-width-down/100?cb=20210901132233",
          """
              **General:** 宝鐘マリン\s
              **Stream Talk:** #マリン航海記\s
              **Fans:** #宝鐘の一味\s
              **Fan Art:** #マリンのお宝\s
              **Fan Art (NSFW):** #沈没後悔日記\s
              **Commentary On Released Voice Dramas:** #聞いたよマリン船長\s
              **Video Clips:** #わかるマリン\s
              **Memes:** #宝鐘海賊団クソコラ部\s""");
      // Gen 4
      case "kanata", "amane" -> sendEmbed(ce, display, "Amane Kanata @amanekanatach",
          "https://twitter.com/amanekanatach",
          "https://static.wikia.nocookie.net/virtualyoutuber/images/0/05/" +
              "Amane_Kanata_-_Icon.png/revision/latest/scale-to-width-down/100?cb=20210901131938",
          """
              **General:** #天音かなた\s
              **Stream Talk:** #天界学園放送部\s
              **Fans:** #へい民\s
              **Fan Art:** #かなたーと\s
              **Commentary On Released Voice Dramas:** #天音かなたボイス\s
              **Singing:** #天音かなた歌ってみた\s
              **Video Clips:** #PPカット\s""");
      case "watame", "tsunomaki" -> sendEmbed(ce, display, "Tsunomaki Watame @tsunomakiwatame",
          "https://twitter.com/tsunomakiwatame",
          "https://static.wikia.nocookie.net/virtualyoutuber/images/c/c8/" +
              "Tsunomaki_Watame_-_Icon.png/revision/latest/scale-to-width-down/100?cb=20210901133018",
          """
              **General:** 角巻わため\s
              **Stream Talk:** #ドドドライブ\s
              **Fans:** #わためいと\s
              **Fan Art:** #つのまきあーと\s
              **Video Clips:** #わたわた動画\s
              **Watame No Uta:** #わためのうた\s""");
      case "towa", "tokoyami" -> sendEmbed(ce, display, "Tokoyami Towa @tokoyamitowa",
          "https://twitter.com/tokoyamitowa",
          "https://static.wikia.nocookie.net/virtualyoutuber/images/a/a1/" +
              "Tokoyami_Towa_-_Icon.png/revision/latest/scale-to-width-down/100?cb=20210901132952",
          """
              **General:** #常闇トワ\s
              **Stream Talk:** #トワイライヴ\s
              **Fans:** #常闇眷属\s
              **Fan Art:** #TOWART\s
              **Video Clips:** #CUTOWA\s""");
      case "luna", "himemori" -> sendEmbed(ce, display, "Himemori Luna @himemoriluna",
          "https://twitter.com/himemoriluna",
          "https://static.wikia.nocookie.net/virtualyoutuber/images/e/ec/" +
              "Himemori_Luna_-_Icon.png/revision/latest/scale-to-width-down/100?cb=20210901132153",
          """
              **General:** #姫森ルーナ\s
              **Stream talk:** #なのらいぶ\s
              **Fans:** #ルーナイト\s
              **Fan Art:** #ルーナート\s
              **Fan Art (NSFW):** #セクシールーナート\s
              **Commentary On Released Voice Dramas:** #ルーナ聴いたよ\s
              **Miscellaneous:** #ルーナわからないのら\s""");
      case "coco", "kiryu" -> sendEmbed(ce, display, "Kiryu Coco @kiryucoco",
          "https://twitter.com/kiryucoco",
          "https://static.wikia.nocookie.net/virtualyoutuber/images/6/6a/" +
              "Kiryu_Coco_-_Icon.png/revision/latest/scale-to-width-down/100?cb=20210901132331",
          """
              **General:** #桐生ココ\s
              **Stream Talk:** #ココここ\s
              **Fans:** #たつのこ\s
              **Fan Art:** #みかじ絵\s
              **AsaCoco Talk:** #あさココLIVE\s
              **AsaCoco Leaks:** #あさココリーク\s
              **Reddit Shitpost Review talk:** #redditshitreview\s""");
      // Gen 5
      case "lamy", "yukihana" -> sendEmbed(ce, display, "Yukihana Lamy @yukihanalamy",
          "https://twitter.com/yukihanalamy",
          "https://static.wikia.nocookie.net/virtualyoutuber/images/7/78/" +
              "Yukihana_Lamy_-_Icon.png/revision/latest/scale-to-width-down/100?cb=20210901133134",
          """
              **Stream Talk:** #らみらいぶ\s
              **Fans:** #雪民\s
              **Fan Art:** #LamyArt\s
              **Video Clips:** #ラミィネート\s
              **Memes:** #Lamyme\s""");
      case "nene", "momosuzu" -> sendEmbed(ce, display, "Momosuzu Nene @momosuzunene",
          "https://twitter.com/momosuzunene",
          "https://static.wikia.nocookie.net/virtualyoutuber/images/e/ec/" +
              "Momosuzu_Nene_-_Icon.png/revision/latest/scale-to-width-down/100?cb=20210901132429",
          """
              **Stream Talk:** #ねねいろらいぶ\s
              **Fans:** #ねっ子\s
              **Fan Art:** #ねねアルバム\s""");
      case "botan", "shishiro" -> sendEmbed(ce, display, "Shishiro Botan @shishirobotan",
          "https://twitter.com/shishirobotan",
          "https://static.wikia.nocookie.net/virtualyoutuber/images/1/1f/" +
              "Shishiro_Botan_-_Icon.png/revision/latest/scale-to-width-down/100?cb=20210901132912",
          """
              **Stream Talk:** #ぐうたらいぶ\s
              **Fans:** #SSRB\s
              **Fan Art:** #ししらーと\s""");
      case "polka", "omaru" -> sendEmbed(ce, display, "Omaru Polka @omarupolka",
          "https://twitter.com/omarupolka",
          "https://static.wikia.nocookie.net/virtualyoutuber/images/6/6e/" +
              "Omaru_Polka_-_Icon.png/revision/latest/scale-to-width-down/100?cb=20210901132655",
          """
              **General:** #尾丸ポルカ\s
              **Stream:** #ポルカ公演中\s
              **Fans:** #おまる座\s
              **Fan Art:** #絵まる\s
              **Video Clips:** #ポルカット\s
              **Other:** #ポルカおるか\s""");
      case "aloe", "mano" -> sendEmbed(ce, display, "Mano Aloe @manoaloe",
          "https://twitter.com/manoaloe",
          "https://static.wikia.nocookie.net/virtualyoutuber/images/5/5c/" +
              "Mano_Aloe_-_Icon.png/revision/latest/scale-to-width-down/100?cb=20210901132400",
          "**Fan Art:** #まのあろ絵 ");
      // ID Gen 1
      case "risu", "ayuna" -> sendEmbed(ce, display, "Ayunda Risu @ayunda_risu",
          "https://twitter.com/ayunda_risu",
          "https://static.wikia.nocookie.net/virtualyoutuber/images/c/c5/" +
              "Ayunda_Risu_-_Icon.png/revision/latest/scale-to-width-down/130?cb=20210901132013",
          """
              **General:** #Ayunda_Risu\s
              **Stream Talk:** #Risu_Live\s
              **Fans:** #Risuners\s
              **Fan Art:** #GambaRisu\s
              **Memes:** #Risu_meme\s
              **Celebrations:** #CongRISUlation\s""");
      case "moona", "hoshinova" -> sendEmbed(ce, display, "Moona Hoshinova @moonahoshinova",
          "https://twitter.com/moonahoshinova",
          "https://static.wikia.nocookie.net/virtualyoutuber/images/4/41/" +
              "Moona_Hoshinova_-_Icon.png/revision/latest/scale-to-width-down/130?cb=20210901132442",
          """
              **General:** #Moona_Hoshinova\s
              **Stream Talk:** #MoonA_Live\s
              **Stream Talk For Gaming Streams:** #GeeMoon\s
              **Stream Talk For Mystery Streams:** #MoonaBoona\s
              **Stream Talk For Karaoke Streams:** #MoonUtau\s
              **Schedule:** #Moona_LiveSchedule\s
              **Fan Art:** #HoshinovArt\s
              **Memes:** #GrassMoona\s""");
      case "iofifteen", "iofi", "airani" -> sendEmbed(ce, display, "Airani Iofifteen @airaniiofifteen",
          "https://twitter.com/airaniiofifteen",
          "https://static.wikia.nocookie.net/virtualyoutuber/images/6/6e/" +
              "Airani_Iofifteen_-_Icon.png/revision/latest/scale-to-width-down/130?cb=20210901131853",
          """
              **Stream:** #ioLYFE\s
              **Fan Art:** #ioarts\s
              **Memes:** #iomemes\s""");
      // ID Gen 2
      case "ollie", "kureiji" -> sendEmbed(ce, display, "Kureiji Ollie @kureijiollie",
          "https://twitter.com/kureijiollie",
          "https://static.wikia.nocookie.net/virtualyoutuber/images/3/3d/" +
              "Kureiji_Ollie_-_Icon.png/revision/latest/scale-to-width-down/130?cb=20210901132346",
          """
              **General:** #Kureiji_Ollie\s
              **Stream Talk:** #OLLIEginal\s
              **Fan Art:** #graveyART\s
              **Announcements:** #OLLInfo\s
              **Memes:** #OLLIcin\s""");
      case "anya", "melfissa" -> sendEmbed(ce, display, "Anya Melfissa @anyamelfissa",
          "https://twitter.com/anyamelfissa",
          "https://static.wikia.nocookie.net/virtualyoutuber/images/3/37/" +
              "Anya_Melfissa_-_Icon.png/revision/latest/scale-to-width-down/130?cb=20210901131956",
          """
              **Stream:** #Liveissa\s
              **Fan Art:** #anyatelier\s
              **Fans:** #melfriends\s""");
      case "reine", "paviola" -> sendEmbed(ce, display, "Paviola Reine @pavoliareine",
          "https://twitter.com/pavoliareine",
          "https://static.wikia.nocookie.net/virtualyoutuber/images/3/34/" +
              "Pavolia_Reine_-_Icon.png/revision/latest/scale-to-width-down/130?cb=20210901132741",
          """
              **General:** #Pavolia_Reine\s
              **Stream Talk:** #Pavolive\s
              **Fans:** #MERAKyats\s
              **Fan Art:** #Reinessance\s
              **Memes:** #POGVOLIA\s""");
      // EN Gen 1
      case "calliope", "calli", "mori" -> sendEmbed(ce, display, "Mori Calliope @moricalliope ",
          "https://twitter.com/moricalliope ",
          "https://static.wikia.nocookie.net/virtualyoutuber/images/c/cd/" +
              "Mori_Calliope_-_Icon.png/revision/latest/scale-to-width-down/100?cb=20210901132459",
          """
              **Stream talk (English):** #calliolive\s
              **Stream talk (Japanese):** #カリオライブ\s
              **Fans (English):** #deadbeats\s
              **Fans (Japanese):** #デッドビーツ\s
              **Fan Art (English):** #callillust\s
              **Fan Art (Japanese):** #カリイラスト\s
              **Music:** #callioP\s
              **Memes (English):** #calliomeme\s
              **Memes (Japanese):** #カリオミーム\s""");
      case "kiara", "takanashi" -> sendEmbed(ce, display, "Takanashi Kiara @takanashikiara",
          "https://twitter.com/takanashikiara",
          "https://static.wikia.nocookie.net/virtualyoutuber/images/9/9a/" +
              "Takanashi_Kiara_-_Icon.png/revision/latest/scale-to-width-down/100?cb=20210901132926",
          """
              **Stream Tag (English):** #kfp\s
              **Stream Tag (Japanese):** #キアライブ\s
              **Fan Art (English):** #artsofashes\s
              **Fan Art (Japanese):** #絵ニックス\s
              **Kiara x Calliope Fan Art:** #takamori\s""");
      case "ina'nis", "ina", "ninomae" -> sendEmbed(ce, display, "Ninomae Ina'nis @ninomaeinanis",
          "https://twitter.com/ninomaeinanis",
          "https://static.wikia.nocookie.net/virtualyoutuber/images/e/ec/" +
              "Ninomae_Ina%27nis_-_Icon.png/revision/latest/scale-to-width-down/100?cb=20210901132639",
          """
              **Stream Tag (English):** #TAKOTIME\s
              **Stream Tag (Japanese):** #タコタイム\s
              **Fan Art (English):** #inART\s
              **Fan Art (Japanese):** #いなート\s""");
      case "gura", "gawr" -> sendEmbed(ce, display, "Gawr Gura @gawrgura",
          "https://twitter.com/gawrgura",
          "https://static.wikia.nocookie.net/virtualyoutuber/images/a/a8/" +
              "Gawr_Gura_-_Icon.png/revision/latest/scale-to-width-down/100?cb=20210901132122",
          """
              **General/Stream Talk:** #gawrgura\s
              **Fan Art:** #gawrt\s
              **Fans:** #chumbuds\s""");
      case "amelia", "watson" -> sendEmbed(ce, display, "Amelia Watson @watsonameliaEn",
          "https://twitter.com/watsonameliaEn",
          "https://static.wikia.nocookie.net/virtualyoutuber/images/7/7d/" +
              "Watson_Amelia_-_Icon.png/revision/latest/scale-to-width-down/100?cb=20210901133105",
          """
              **Stream Talk:** #ameLive\s
              **Fans:** #teamates\s
              **SFW Fan Art:** #ameliaRT\s
              **NSFW Fan Art:** #amelewd\s""");
      // Project: Hope
      case "irys" -> sendEmbed(ce, display, "IRyS @irys_en",
          "https://twitter.com/irys_en",
          "https://static.wikia.nocookie.net/virtualyoutuber/images/f/ff/" +
              "IRyS_-_Icon.png/revision/latest/scale-to-width-down/100?cb=20210901132316",
          """
              **General:** #IRyS\s
              **Stream:** #IRyShow\s
              **Fan Art:** #IRySart\s""");
      // EN Gen 2
      case "sana", "tsukumo" -> sendEmbed(ce, display, "Tsukumo Sana @tsukumosana",
          "https://twitter.com/tsukumosana",
          "https://static.wikia.nocookie.net/virtualyoutuber/images/0/0f/" +
              "Tsukumo_Sana_-_Icon.png/revision/latest/scale-to-width-down/100?cb=20210901133004",
          """
              **Stream Tag:** #SanaLanding\s
              **Fan Art:** #galaxillust\s
              **Fan Tag:** #Sanallite\s""");
      case "fauna", "ceres" -> sendEmbed(ce, display, "Ceres Fauna @ceresfauna",
          "https://twitter.com/ceresfauna",
          "https://static.wikia.nocookie.net/virtualyoutuber/images/0/02/" +
              "Ceres_Fauna_-_Icon.png/revision/latest/scale-to-width-down/100?cb=20210901132109",
          """
              **General Tag:** #faunline\s
              **Art Tag:** #FineFaunart\s
              **Debut Tag:** #FirstFaun\s""");
      case "kronii", "ouro" -> sendEmbed(ce, display, "Ouro Kronii @ourokronii",
          "https://twitter.com/ourokronii",
          "https://static.wikia.nocookie.net/virtualyoutuber/images/d/d4/" +
              "Ouro_Kronii_-_Icon.png/revision/latest/scale-to-width-down/100?cb=20210901133511",
          """
              **Stream (English):** #krotime\s
              **General Tag:** #ourokronii\s
              **Fan Art (English):** #kronillust\s
              **Memes:** #kroniijokes\s
              **Stream (Japanese):** #クロタイム\s
              **Fan Art (Japanese):** クロニーラ\s""");
      case "mumei", "nanashi" -> sendEmbed(ce, display, "Nanashi Mumei @nanashimumei_en",
          "https://twitter.com/nanashimumei_en",
          "https://static.wikia.nocookie.net/virtualyoutuber/images/b/b9/" +
              "Nanashi_Mumei_-_Icon.png/revision/latest/scale-to-width-down/100?cb=20210901132548",
          """
              **Stream Tag:** #watchMEI\s
              **Fanart Tag (English):** #drawMEI\s
              **Fanart Tag (Japanese):** #ムメ絵\s
              **Meme Tag:** #memeMEI\s
              **Stream Tag:** #watchMEI\s""");
      case "baelz" -> sendEmbed(ce, display, "Hakos Baelz @hakosbaelz",
          "https://twitter.com/hakosbaelz",
          "https://static.wikia.nocookie.net/virtualyoutuber/images/0/0a/" +
              "Hakos_Baelz_-_Icon.png/revision/latest/scale-to-width-down/100?cb=20210901132137",
          """
              **Stream Tag (English):** #enterbaelz\s
              **Stream Tag (Japanese):** #ベール登場\s
              **Fan Art (English):** #illustrayBAE\s
              **Fan Art (Japanese):** #ベーアート\s
              **Fan Tag:** #baerats\s
              **Music:** #MADDOOFS\s
              **Memes (English):** #YABAEL\s
              **Memes (Japanese):** #ヤベール\s""");
      // Holostars Gen 1
      case "miyabi", "hanasaki" -> sendEmbed(ce, display, "Hanasaki Miyabi @miyabihanasaki",
          "https://twitter.com/miyabihanasaki",
          "https://static.wikia.nocookie.net/virtualyoutuber/images/7/77/" +
              "Hanasaki_Miyabi_-_Profile_Picture.jpg/revision/latest/scale-to-width-down/80?cb=20210725223223",
          """
              **General:** #花咲みやび\s
              **Stream Talk:** #生き花\s
              **Schedule:** #開花予定\s
              **Fans:** #花見組\s
              **Fan Art:** #みやびじゅつ\s
              **Video Clips:** #切り抜きみやびさん\s
              **Videos:** #花咲日和\s
              **Commentary On Released Voice Dramas:** #聞いたぞみやび\s""");
      case "izuru", "kanade" -> sendEmbed(ce, display, "Kanade Izuru @kanadeizuru",
          "https://twitter.com/kanadeizuru",
          "https://static.wikia.nocookie.net/virtualyoutuber/images/6/64/" +
              "Kanade_Izuru_-_Profile_Picture.jpg/revision/latest/scale-to-width-down/80?cb=20210412102921",
          """
              **General:** #奏手イヅル\s
              **Fan Art:** #イヅル描いたぞ\s
              **Fan Art (NSFW):** #イヅル見るなよ\s
              **RIZnote General Tag:** #RIZnote\s
              **RIZnote Stream Talk:** #RIZ生\s
              **RIZnote Fan Art:** #RIZアート\s""");
      case "arurandeisu", "aruran" -> sendEmbed(ce, display, "Arurandeisu @arurandeisu",
          "https://twitter.com/arurandeisu",
          "https://static.wikia.nocookie.net/virtualyoutuber/images/e/e9/" +
              "Arurandeisu_-_Profile_Picture.jpg/revision/latest/scale-to-width-down/80?cb=20210413011649",
          """
              **Stream Talk:** #アランストリーム\s
              **Fan Art:** #アランの画廊\s
              **Fan Art (NSFW):** #Rナンディス\s
              **Video Clips:** #切り抜きナンディス\s
              **Commentary On Released Voice Dramas:** #聞いたぞアラン\s""");
      case "rikka" -> sendEmbed(ce, display, "Rikka @rikkaroid",
          "https://twitter.com/rikkaroid",
          "https://static.wikia.nocookie.net/virtualyoutuber/images/8/8b/" +
              "Rikka_-_Profile_Picture.jpg/revision/latest/scale-to-width-down/80?cb=20210415153551",
          """
              **General:** #律可\s
              **Stream Talk:** #りつすた\s
              **Schedule:** #りつジュール\s
              **Fans:** #調りつ師\s
              **Fan Art:** #りつあーと\s
              **Fan Art (NSFW):** #律可開発中\s
              **Video Clips:** #ちょこりっか\s
              **Commentary On Released Voice Dramas:** #聞いたぞりっか\s
              **RIZnote General Tag:** #RIZnote\s
              **RIZnote Stream Talk:** #RIZ生\s
              **RIZnote Fan Art:** #RIZアート\s
              **RiTunes Labo:** #りちゅらぼ\s""");
      case "kira", "kagami" -> sendEmbed(ce, display, "Kagami Kira @kagamikirach",
          "https://twitter.com/kagamikirach",
          "https://static.wikia.nocookie.net/virtualyoutuber/images/7/70/" +
              "Kagami_Kira_-_Profile_Picture.jpg/revision/latest/scale-to-width-down/80?cb=20210202172609",
          """
              **General:** #鏡見キラ\s
              **Stream Talk:** #キラ生\s
              **Fan Art:** #キラ絵\s
              **Fan Art (NSFW):** #キラ見ろ\s
              **Video Clips:** #キラ抜いたぞ\s""");
      case "suzaku", "yakushiji" -> sendEmbed(ce, display, "Yakushiji Suzaku @YakushijiSuzaku",
          "https://twitter.com/YakushijiSuzaku",
          "https://static.wikia.nocookie.net/virtualyoutuber/images/d/d1/" +
              "Yakushiji_Suzaku_-_Profile_Picture.jpg/revision/latest/scale-to-width-down/80?cb=20210202173133",
          """
              **General:** #薬師寺朱雀\s
              **Fan Art:** #朱雀絵\s
              **Video Clips:** #朱雀動画\s""");
      // Holostars Gen 2
      case "leda", "astel" -> sendEmbed(ce, display, "Astel Leda @astelleda",
          "https://twitter.com/astelleda",
          "https://static.wikia.nocookie.net/virtualyoutuber/images/8/88/" +
              "Astel_Leda_-_Profile_Picture.jpg/revision/latest/scale-to-width-down/130?cb=20200815110151",
          """
              **General:** #アステルレダ\s
              **Stream Talk:** #アステル生ダ\s
              **Fans:** #アステラー\s
              **Fan Art:** #アステル絵ダ\s
              **Fan Art (NSFW):** #アステルエッだ\s
              **Commentary On Released Voice Dramas:** #アステル聞ケタ\s""");
      case "temma", "kishido" -> sendEmbed(ce, display, "Kishido Temma @kishidotemma",
          "https://twitter.com/kishidotemma",
          "https://static.wikia.nocookie.net/virtualyoutuber/images/9/9b/" +
              "Kishido_Temma_-_Profile_Picture.jpg/revision/latest/scale-to-width-down/130?cb=20210725223136",
          """
              **General:** #岸堂天真\s
              **Stream Talk:** #天真修行中\s
              **Fans:** #岸メン\s
              **Fan Art:** #騎士絵画\s
              **Video Clips:** 騎士抜き動画\s
              **Commentary On Released Voice Dramas:** #天真聞いたよ\s""");
      case "roberu", "yukoku" -> sendEmbed(ce, display, "Yukoku Roberu @yukokuroberu",
          "https://twitter.com/yukokuroberu",
          "https://static.wikia.nocookie.net/virtualyoutuber/images/8/84/" +
              "Yukoku_Roberu_-_Profile_Picture.jpg/revision/latest/scale-to-width-down/130?cb=20210412110008",
          """
              **General:** #夕刻ロベル\s
              **Stream Talk:** #ROBEL営業中\s
              **Fan Art:** #描クテル\s
              **Video Clips:** #ひとくちロベル\s
              **Commentary On Released Voice Dramas:** #おいロベル""");
      // Holostars Gen 3
      case "shien", "kageyama" -> sendEmbed(ce, display, "Kageyama Shien @kageyamashien",
          "https://twitter.com/kageyamashien",
          "https://static.wikia.nocookie.net/virtualyoutuber/images/7/70/" +
              "Kageyama_Shien_-_Profile_Picture.jpg/revision/latest/scale-to-width-down/130?cb=20210725223023",
          """
              **General:** #影山シエン\s
              **Stream Talk:** #エンエアー\s
              **Fans:** #シエン組\s
              **Fan Art:** #シ絵ン\s
              **Fan art (NSFW):** #シ絵ッン\s
              **Video Clips:** #お試シエン\s
              **Commentary On Released Voice Dramas:** #耳エン\s""");
      case "oga", "aragami" -> sendEmbed(ce, display, "Aragami Oga @aragamioga",
          "https://twitter.com/aragamioga",
          "https://static.wikia.nocookie.net/virtualyoutuber/images/2/2a/" +
              "Aragami_Oga_-_Profile_Picture.jpg/revision/latest/scale-to-width-down/130?cb=20210725222909",
          """
              **General:** #荒咬オウガ\s
              **Stream Talk:** #アラライブ\s
              **Fan Art:** #オウ画\s
              **Fan Art (NSFW):** #オウガ済ム\s
              **Video Clips:** #荒咬ドウガ\s
              **Commentary On Released Voice Dramas:** #荒声\s
              **Twitter Spaces Talk:** #荒スペ\s""");
      case "kaoru", "tsukishita" -> sendEmbed(ce, display, "Tsukishita Kaoru @tsukishitakaoru",
          "https://twitter.com/tsukishitakaoru",
          "https://static.wikia.nocookie.net/virtualyoutuber/images/0/05/" +
              "Tsukishita_Kaoru_-_Profile_Picture.jpg/revision/latest/scale-to-width-down/130?cb=20210202172851",
          """
              **General:** #月下カオル\s
              **Stream Talk:** #カオルがおる\s
              **Fan Art:** #着せカオル\s
              **Fan Art (NSFW):** #脱が下カオル\s""");
      default -> ce.getChannel().sendMessage("HoloLive member not found.").queue();
    }
  }

  private void sendEmbed(CommandEvent ce, EmbedBuilder display, String title, String url,
                         String imageUrl, String description) {
    display.setTitle(title, url);
    display.setThumbnail(imageUrl);
    display.setDescription(description);
    Settings.sendEmbed(ce, display);
  }
}