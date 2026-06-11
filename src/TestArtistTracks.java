import java.awt.Desktop;
import java.io.*;
import java.net.*;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class TestArtistTracks {

    static final String CLIENT_ID     = "352a49c7bf784f928b91f3710986bae3";
    static final String CLIENT_SECRET = "09238c7bce1d4e73b5a4546879bde72a";
    static final String REDIRECT_URI  = "http://127.0.0.1:8888/callback";
    static final String SCOPE         = "playlist-modify-public playlist-modify-private user-read-private";

    public static void main(String[] args) throws Exception {
        HttpClient http = HttpClient.newHttpClient();

        // OAuth
        String authUrl = "https://accounts.spotify.com/authorize"
                + "?client_id=" + CLIENT_ID
                + "&response_type=code"
                + "&redirect_uri=" + URLEncoder.encode(REDIRECT_URI, "UTF-8")
                + "&scope=" + URLEncoder.encode(SCOPE, "UTF-8");

        System.out.println("Abriendo navegador...");
        Desktop.getDesktop().browse(new URI(authUrl));

        String code;
        try (ServerSocket server = new ServerSocket(8888, 1, InetAddress.getByName("127.0.0.1"))) {
            try (Socket socket = server.accept()) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String line = reader.readLine();
                PrintWriter w = new PrintWriter(socket.getOutputStream());
                w.println("HTTP/1.1 200 OK\r\nContent-Type: text/html\r\n\r\n<h1>OK</h1>");
                w.flush();
                code = line.split("code=")[1].split("[ &]")[0];
            }
        }

        String cred = Base64.getEncoder().encodeToString(
                (CLIENT_ID + ":" + CLIENT_SECRET).getBytes(StandardCharsets.UTF_8));

        HttpResponse<String> tokenResp = http.send(
                HttpRequest.newBuilder()
                        .uri(URI.create("https://accounts.spotify.com/api/token"))
                        .header("Authorization", "Basic " + cred)
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .POST(HttpRequest.BodyPublishers.ofString(
                                "grant_type=authorization_code&code=" + code +
                                        "&redirect_uri=" + URLEncoder.encode(REDIRECT_URI, "UTF-8")))
                        .build(),
                HttpResponse.BodyHandlers.ofString());

        String token = extraer(tokenResp.body(), "access_token");
        System.out.println("Token OK\n");

        String artista = "Mon Laferte";

        // PASO 1: Buscar el artist ID
        System.out.println("=== PASO 1: Buscar artist ID de \"" + artista + "\" ===");
        String searchUrl = "https://api.spotify.com/v1/search?q="
                + URLEncoder.encode(artista, "UTF-8")
                + "&type=artist";

        HttpResponse<String> searchResp = http.send(
                HttpRequest.newBuilder()
                        .uri(URI.create(searchUrl))
                        .header("Authorization", "Bearer " + token)
                        .GET().build(),
                HttpResponse.BodyHandlers.ofString());

        System.out.println("Status: " + searchResp.statusCode());

        // Extraer primer artist ID
        String body = searchResp.body();
        String artistId = "";
        int idPos = body.indexOf("\"id\":\"");
        if (idPos != -1) {
            int start = idPos + 6;
            int end = body.indexOf("\"", start);
            artistId = body.substring(start, end);
        }
        System.out.println("Artist ID: " + artistId);

        if (artistId.isEmpty()) {
            System.out.println("No se encontro el artista.");
            System.out.println("Respuesta: " + body.substring(0, Math.min(500, body.length())));
            return;
        }

        // PASO 2: Top tracks
        System.out.println("\n=== PASO 2: GET /v1/artists/" + artistId + "/top-tracks ===");
        HttpResponse<String> topResp = http.send(
                HttpRequest.newBuilder()
                        .uri(URI.create("https://api.spotify.com/v1/artists/" + artistId + "/top-tracks"))
                        .header("Authorization", "Bearer " + token)
                        .GET().build(),
                HttpResponse.BodyHandlers.ofString());

        System.out.println("Status: " + topResp.statusCode());

        if (topResp.statusCode() != 200) {
            System.out.println("Respuesta: " + topResp.body().substring(0, Math.min(300, topResp.body().length())));
            return;
        }

        // Parsear tracks
        String data = topResp.body();
        int pos = 0;
        int count = 0;
        System.out.println("\nTop Tracks:\n");

        while (true) {
            int namePos = data.indexOf("\"name\":\"", pos);
            if (namePos == -1) break;
            int ni = namePos + 8;
            int nf = data.indexOf("\"", ni);
            if (nf == -1) break;
            String nombre = data.substring(ni, nf);

            int uriPos = data.indexOf("\"uri\":\"spotify:track:", namePos);
            if (uriPos == -1) break;
            int ui = uriPos + 7;
            int uf = data.indexOf("\"", ui);
            if (uf == -1) break;
            String uri = data.substring(ui, uf);

            count++;
            System.out.printf("  %2d. %-45s  %s%n", count, nombre, uri);
            pos = uf + 1;
        }

        System.out.println("\nTotal: " + count + " canciones");

        // PASO 3: Albums del artista
        System.out.println("\n=== PASO 3: GET /v1/artists/" + artistId + "/albums ===");
        HttpResponse<String> albumResp = http.send(
                HttpRequest.newBuilder()
                        .uri(URI.create("https://api.spotify.com/v1/artists/" + artistId + "/albums?include_groups=album,single"))
                        .header("Authorization", "Bearer " + token)
                        .GET().build(),
                HttpResponse.BodyHandlers.ofString());

        System.out.println("Status: " + albumResp.statusCode());

        String albumData = albumResp.body();
        int aPos = 0;
        int albumCount = 0;
        System.out.println("\nAlbums:\n");

        while (true) {
            int namePos = albumData.indexOf("\"name\":\"", aPos);
            if (namePos == -1) break;
            int ni = namePos + 8;
            int nf = albumData.indexOf("\"", ni);
            if (nf == -1) break;
            String albumName = albumData.substring(ni, nf);

            albumCount++;
            System.out.printf("  %2d. %s%n", albumCount, albumName);
            aPos = nf + 1;

            if (albumCount >= 10) break;
        }

        System.out.println("\n=== FIN TEST ===");
    }

    static String extraer(String json, String clave) {
        String tag = "\"" + clave + "\":\"";
        int i = json.indexOf(tag);
        if (i == -1) return "";
        i += tag.length();
        int f = json.indexOf("\"", i);
        return f == -1 ? "" : json.substring(i, f);
    }
}