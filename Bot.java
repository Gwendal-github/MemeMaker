import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import javax.security.auth.login.LoginException;
import java.io.BufferedReader;
import javax.imageio.ImageIO;
import java.util.regex.Pattern;
import java.awt.Color;
import java.awt.image.BufferedImage;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.io.File;
import java.net.URL;
import java.util.Calendar;
import java.util.stream.Collectors;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.io.InputStreamReader;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import net.dv8tion.jda.api.EmbedBuilder;
import java.sql.Timestamp;
import java.net.URLEncoder;

public class Bot extends ListenerAdapter {
    private String BOT_PREFIX = "/MM";
    private static String token;
    private static int numero = 0;
    private static String user = "GwendalGarenaux";
    private static String mdp = "botMemeMaker";
    private static String memes = "oui";
    private static String NasaKey = "jmVDsi8m62vp3CxehLpTceg6QKdV0FDIJUSmMFhX";

    public static void main(String[] args) throws LoginException { // Lancement du bot
        try {
            System.out.println("Let's go");
            token = args[0];
            JDA jda = new JDABuilder(AccountType.BOT).setToken(token).addEventListeners(new Bot()).build().awaitReady();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) { // Traitement d'un message reçu
        String message = event.getMessage().getContentRaw(); // On récupère le content du message
        if (message.equals(this.BOT_PREFIX + " ping")) { // Si il contient /MM ping, on répond pong
            event.getChannel().sendMessage("Pong (retrouvez-moi tout les week-end a l'Olympia pour mon spectacle)")
                    .queue();
            return;
        }
        if (message.contains(this.BOT_PREFIX + " dice")) { // Si il contient /MM dice
            try {
                int max = 6;
                if (message.length() > 9) { // On regarde si il y'a un argument en plus, si oui le max devient cet
                                            // argument, sinon il reste à 6
                    max = Integer.parseInt(message.substring(9));
                }
                double res = Math.floor(Math.random() * max) + 1.00;
                event.getChannel().sendMessage("Le resultat est : " + res).queue(); // On renvoie le résultat
            } catch (NumberFormatException e) { // Si l'argument passé n'est pas un nombre, on le fait savoir
                event.getChannel().sendMessage("Il me faut un nombre").queue();
            }
            return;
        }
        if (message.contains(this.BOT_PREFIX + " cat")) { // Si le message contient /MM cat
            try {
                if (message.contains("says")) { // Si il y'a également says + du texte derrière, on l'affiche sur
                                                // l'image
                    String texte = message.substring(13);
                    URL url = new URL("https://cataas.com/cat/says/" + texte);
                    saveImage(url, "/tmp/cat.png");
                    event.getChannel().sendFile(new File("/tmp/cat.png")).queue();
                    return;
                } // Sinon on envoie une image simple
                URL url = new URL("https://cataas.com/cat");
                saveImage(url, "/tmp/cat.png");
                event.getChannel().sendFile(new File("/tmp/cat.png")).queue();
                return;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (message.contains(this.BOT_PREFIX + " help")) { // Commande d'aide classique, avec /MM help
            EmbedBuilder eb = new EmbedBuilder();
            eb.setTitle("Commandes");
            eb.setColor(Color.RED);
            eb.addField("Ping : /MM ping", "Pong (ah ah)", false);
            eb.addField("Dice : /MM dice [max]", "Donne un nombre entre 1 et max, par defaut 6", false);
            eb.addField("Chat : /MM cat [ says [texte]]",
                    "Donne une image de chat. Il peut meme dire un truc avec le parametre texte", false);
            eb.addField("Obtenir les ID de memes : /MM getID", "Donne les ID des templates", false);
            eb.addField("Meme : /MM meme [id]|[texte1]|[texte2]",
                    "Donne un meme complete, avec le template defini par l id (voir getID), et avec vos deux phrases.",
                    false);
            eb.addField("Meteo : /MM meteo [ville]",
                    "Donne la meteo, par defaut a Calais. Vous pouvez choisir la ville en indiquant une ville en plus.",
                    false);
            eb.addField("Meteo : /MM nasa [date format YYYY-MM-DD]",
                    "Donne la photo du jour de la nasa, ainsi qu'une explication (en anglais). Si une date est donnée en paramètre, donne la photo de ce jour.",
                    false);
            event.getChannel().sendMessage(eb.build()).queue();
        }
        if (message.contains(this.BOT_PREFIX + " meteo")) { // Commande donnant la météo sur /MM meteo
            try {
                String meteo;
                if (message.length() > 9) { // Si un argument supplémentaire est passé, on vérifie sa validité. Si c'est
                                            // une ville, on la garde, sinon, on prévient l'utilisateur
                    try {
                        meteo = getMeteo(message.substring(9));
                    } catch (RuntimeException e) {
                        event.getChannel().sendMessage("J'ai pas compris").queue();
                        return;
                    }
                } else { // Sinon, on initialise à Calais
                    meteo = getMeteo("Calais");
                }
                JSONParser parser = new JSONParser();
                JSONObject jsonMeteo = (JSONObject) (parser.parse(meteo));
                EmbedBuilder eb = new EmbedBuilder();
                embedBuild(eb, jsonMeteo);
                event.getChannel().sendMessage(eb.build()).queue();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        if (message.contains(this.BOT_PREFIX + " nasa")) { // Commande donnant la photo du jour de la nasa
            String picture;
            try {
                try {
                    if (message.length() > 9) {
                        picture = getNasa(message.substring(9));
                    } else {
                        picture = getNasa("");
                    }
                } catch (RuntimeException e) {
                    event.getChannel().sendMessage("J'ai pas compris").queue();
                    return;
                }
                JSONParser parser = new JSONParser();
                JSONObject jsonNasa = (JSONObject) (parser.parse(picture));
                EmbedBuilder eb = new EmbedBuilder();
                nasaEmbedBuild(eb, jsonNasa);
                event.getChannel().sendMessage(eb.build()).queue();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        if (message.contains(this.BOT_PREFIX + " meme")) { // Fonction permettant de compléter un template
            try {
                String textes = message.substring(9);
                String[] split = textes.split(Pattern.quote("|"));
                String meme = getMeme(split);
                JSONParser parser = new JSONParser();
                JSONObject jsonMeme = (JSONObject) (parser.parse(meme));
                JSONObject un = (JSONObject) jsonMeme.get("data");
                String deux = (String) un.get("url");
                event.getChannel().sendMessage(deux).queue();
            } catch (ParseException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {
                event.getChannel().sendMessage("Une erreur s'est produite, vous etes surs que c'est le bon code?")
                        .queue();
            }
        }
        if (message.contains(this.BOT_PREFIX + " getID")) { // Fonction donnant les ID pour construire les memes
            try {
                int number = 0;
                if (memes == "oui") {
                    memes = getMemeRandom();
                }
                JSONParser parser = new JSONParser();
                JSONObject jsonMeme = (JSONObject) (parser.parse(memes));
                JSONObject memeArray = (JSONObject) jsonMeme.get("data");
                JSONArray memeArray2 = (JSONArray) memeArray.get("memes");
                EmbedBuilder eb = new EmbedBuilder();
                eb.setTitle("Commandes");
                eb.setColor(Color.RED);
                eb.addField("nom", "ID", false);
                while (number < 30) {
                    JSONObject index = (JSONObject) memeArray2.get(number);
                    eb.addField((String) index.get("name"), (String) index.get("id"), false);
                    number++;
                }
                event.getChannel().sendMessage(eb.build()).queue();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    public static void embedBuild(EmbedBuilder eb, JSONObject json) { // Méthode permettant la construction du message
                                                                      // météo à partir du fichier json récupéré
        eb.setTitle("Meteo");
        eb.setColor(Color.RED);
        JSONArray meteo = (JSONArray) json.get("weather");
        JSONObject icon = (JSONObject) meteo.get(0);
        String icone = (String) icon.get("icon");
        String url = "https://openweathermap.org/img/wn/" + (String) (icone) + ".png";
        eb.setThumbnail(url);
        eb.addField("Ville", (String) json.get("name"), false);
        JSONObject temp = (JSONObject) json.get("main");
        double tempCelsius = (Double) (temp.get("temp")) - 273.15;
        String tempFinal = tempCelsius + "";
        tempFinal = tempFinal.substring(0, 4) + " degres Celsius";
        eb.addField("Temperature", tempFinal, false);
        JSONObject soleil = (JSONObject) json.get("sys");
        String lever = getHoraire((long) (soleil.get("sunrise")), false);
        String coucher = getHoraire((long) (soleil.get("sunset")), true);
        eb.addField("Lever du soleil", lever, false);
        eb.addField("Coucher du soleil", coucher, false);

    }

    public static void nasaEmbedBuild(EmbedBuilder eb, JSONObject json) { // Méthode permettant la construction du
                                                                          // message
        // météo à partir du fichier json récupéré
        eb.setTitle("Picture of the Day");
        eb.setColor(Color.RED);
        eb.setImage((String) json.get("hdurl"));
        String explanation = (String) json.get("explanation");
        if (explanation.length() < 1024) {
            eb.addField("Explication :", explanation, false);
        } else {
            int count = 0;
            while ((count + 1) * 1024 > explanation.length()) {
                eb.addField("Explication (Suite) :", explanation.substring(count * 1024, (count + 1) * 1024), false);
                count++;
            }

        }

    }

    public static String getHoraire(long time, boolean soir) { // Méthode permettant de récupérer l'heure du matin et du
                                                               // soir à partir d'un timestamp unix
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(time * 1000);
        int heure = c.get(Calendar.HOUR);
        int minute = c.get(Calendar.MINUTE);
        if (soir == true) {
            heure = heure + 12;
        }
        String heures, minutes;
        if (heure < 10) {
            heures = "0" + heure;
        } else {
            heures = heure + "";
        }
        if (minute < 10) {
            minutes = "0" + minute;
        } else {
            minutes = minute + "";
        }
        return heures + ":" + minutes;
    }

    public static void saveImage(URL url, String destinationFile) throws IOException { // Méthode récupérant et
                                                                                       // sauvegardant l'image (pour les
                                                                                       // chats) dans le dossier C:\tmp\
        InputStream is = url.openStream();
        OutputStream os = new FileOutputStream(destinationFile);

        byte[] b = new byte[2048];
        int length;

        while ((length = is.read(b)) != -1) {
            os.write(b, 0, length);
        }

        is.close();
        os.close();
    }

    static String getMeteo(String ville) { // Méthode récupérant le JSON de la météo dans la ville fournie
        String result = "";
        try {
            String APIkey = "d5d9e22fc8147b27ccb4df479a42df01";
            String serv = "http://api.openweathermap.org/data/2.5/weather";
            String param = "q=" + ville + ",fr&appid=";
            URL url = new URL(serv + "?" + param + APIkey);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            if (conn.getResponseCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code: " + conn);
            }
            BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
            result = br.lines().collect(Collectors.joining());
            conn.disconnect();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    static String getNasa(String date) { // Méthode récupérant le JSON de l'image du jour de la Nasa
        String result = "";
        String serv = "https://api.nasa.gov/planetary/apod?api_key=" + NasaKey;
        try {
            if (date != "") {
                serv = serv + "&date=" + date;
            }
            URL url = new URL(serv);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            if (conn.getResponseCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code: " + conn);
            }
            BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
            result = br.lines().collect(Collectors.joining());
            conn.disconnect();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    static String connexionServeur(URL url, HttpURLConnection conn, String urlParameters) { // Code de connexion à un
                                                                                            // serveur, commun à
                                                                                            // plusieurs méthodes
        String result = "";
        try {
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);
            try (DataOutputStream wr = new DataOutputStream(conn.getOutputStream())) {
                wr.writeBytes(urlParameters);
                wr.flush();
            }
            if (conn.getResponseCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code: " + conn.getResponseCode());
            }
            BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
            result = br.lines().collect(Collectors.joining());
            conn.disconnect();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    static String getMeme(String[] split) { // Fonction gérant l'envoi des textes à placer et récupère le meme entier
        try {
            URL url = new URL("https://api.imgflip.com/caption_image");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            String urlParameters = "template_id=" + split[0] + "&username=" + user + "&password=" + mdp;
            for (int i = 1; i < split.length; i++) {
                urlParameters = urlParameters + "&boxes[" + (i - 1) + "][type]=text&boxes[" + (i - 1) + "][text]="
                        + split[i];
            }
            String result = connexionServeur(url, conn, urlParameters);
            return result;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Une erreur est survenue";
    }

}
