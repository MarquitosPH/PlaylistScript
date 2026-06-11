import java.awt.Desktop;
import java.io.*;
import java.net.*;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class DebugTracks {

    static final String CLIENT_ID     = "352a49c7bf784f928b91f3710986bae3";
    static final String CLIENT_SECRET = "09238c7bce1d4e73b5a4546879bde72a";
    static final String REDIRECT_URI  = "http://127.0.0.1:8888/callback";
    static final String SCOPE         = "playlist-modify-public playlist-modify-private user-read-private";

    public static void main(String[] args) throws Exception {
        HttpClient http = HttpClient.newHttpClient();

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
                w.println("HTTP/1.1 200 OK\r\nContent-Type: text/html\r\n\r\n<h1>OK - cierra esta pestaña</h1>");
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

        // ── Crear playlist ────────────────────────────────────────────────
        System.out.println("=== Creando playlist de prueba ===");
        HttpResponse<String> createResp = http.send(
            HttpRequest.newBuilder()
                .uri(URI.create("https://api.spotify.com/v1/me/playlists"))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(
                    "{\"name\":\"Debug Tracks Test\",\"public\":false}"))
                .build(),
            HttpResponse.BodyHandlers.ofString());

        System.out.println("Crear status: " + createResp.statusCode());
        String playlistId = extraer(createResp.body(), "id");
        System.out.println("Playlist ID: " + playlistId);

        // Un track de prueba (Bad Bunny - BAILE INOLVIDABLE)
        String trackUri = "spotify:track:2lTm559tuIvatlT1u0JYG2";

        System.out.println("\nEsperando 2 segundos...");
        Thread.sleep(2000);

        // ── Intento 1: POST con body ──────────────────────────────────────
        System.out.println("\n=== INTENTO 1: POST /playlists/{id}/tracks con body ===");
        HttpResponse<String> r1 = http.send(
            HttpRequest.newBuilder()
                .uri(URI.create("https://api.spotify.com/v1/playlists/" + playlistId + "/tracks"))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(
                    "{\"uris\":[\"" + trackUri + "\"],\"position\":0}"))
                .build(),
            HttpResponse.BodyHandlers.ofString());
        System.out.println("Status: " + r1.statusCode());
        System.out.println("Body: " + r1.body());

        // ── Intento 2: POST con uris en query param ──────────────────────
        System.out.println("\n=== INTENTO 2: POST con uris en query param ===");
        HttpResponse<String> r2 = http.send(
            HttpRequest.newBuilder()
                .uri(URI.create("https://api.spotify.com/v1/playlists/" + playlistId
                    + "/tracks?uris=" + URLEncoder.encode(trackUri, "UTF-8")))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(""))
                .build(),
            HttpResponse.BodyHandlers.ofString());
        System.out.println("Status: " + r2.statusCode());
        System.out.println("Body: " + r2.body());

        // ── Intento 3: PUT con body ───────────────────────────────────────
        System.out.println("\n=== INTENTO 3: PUT /playlists/{id}/tracks con body ===");
        HttpResponse<String> r3 = http.send(
            HttpRequest.newBuilder()
                .uri(URI.create("https://api.spotify.com/v1/playlists/" + playlistId + "/tracks"))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(
                    "{\"uris\":[\"" + trackUri + "\"]}"))
                .build(),
            HttpResponse.BodyHandlers.ofString());
        System.out.println("Status: " + r3.statusCode());
        System.out.println("Body: " + r3.body());

        System.out.println("\n=== FIN DEBUG ===");
        System.out.println("Revisa tu Spotify: https://open.spotify.com/playlist/" + playlistId);
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
