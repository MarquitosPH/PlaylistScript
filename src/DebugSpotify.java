import java.awt.Desktop;
import java.io.*;
import java.net.*;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class DebugSpotify {

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
        if (Desktop.isDesktopSupported()) {
            Desktop.getDesktop().browse(new URI(authUrl));
        }

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

        System.out.println("\n=== TOKEN RESPONSE ===");
        System.out.println(tokenResp.body().substring(0, Math.min(200, tokenResp.body().length())));

        String token = extraer(tokenResp.body(), "access_token");
        if (token.isEmpty()) { System.out.println("NO HAY TOKEN"); return; }
        System.out.println("Token OK: " + token.substring(0, 20) + "...");

        // ── GET /v1/me ────────────────────────────────────────────────────
        HttpResponse<String> meResp = http.send(
            HttpRequest.newBuilder()
                .uri(URI.create("https://api.spotify.com/v1/me"))
                .header("Authorization", "Bearer " + token)
                .GET().build(),
            HttpResponse.BodyHandlers.ofString());

        System.out.println("\n=== /v1/me RESPONSE (completa) ===");
        System.out.println(meResp.body());

        String userId = extraer(meResp.body(), "id");
        System.out.println("\nUser ID extraido: [" + userId + "]");

        // ── POST crear playlist con /v1/users/{id}/playlists ──────────────
        System.out.println("\n=== INTENTO 1: POST /v1/users/" + userId + "/playlists ===");
        String body1 = "{\"name\":\"Debug Test\",\"public\":false,\"description\":\"test\"}";

        HttpResponse<String> r1 = http.send(
            HttpRequest.newBuilder()
                .uri(URI.create("https://api.spotify.com/v1/users/" + userId + "/playlists"))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body1))
                .build(),
            HttpResponse.BodyHandlers.ofString());

        System.out.println("Status: " + r1.statusCode());
        System.out.println("Body: " + r1.body().substring(0, Math.min(300, r1.body().length())));

        // ── POST crear playlist
        System.out.println("\n=== INTENTO 2: POST /v1/me/playlists ===");

        HttpResponse<String> r2 = http.send(
            HttpRequest.newBuilder()
                .uri(URI.create("https://api.spotify.com/v1/me/playlists"))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body1))
                .build(),
            HttpResponse.BodyHandlers.ofString());

        System.out.println("Status: " + r2.statusCode());
        System.out.println("Body: " + r2.body().substring(0, Math.min(300, r2.body().length())));

        System.out.println("\n=== FIN DEBUG ===");
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
