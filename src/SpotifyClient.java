import java.awt.Desktop;
import java.io.*;
import java.net.*;
import java.net.http.*;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;

public class SpotifyClient {

    private static final String REDIRECT_URI = "http://127.0.0.1:8888/callback";
    private static final String SCOPE =
            "playlist-modify-public playlist-modify-private user-read-private";

    private final String clientId = "352a49c7bf784f928b91f3710986bae3";
    private final String clientSecret = "09238c7bce1d4e73b5a4546879bde72a";
    private String accessToken;
    private final HttpClient http = HttpClient.newHttpClient();


    public void autorizar() throws Exception {
        String authUrl = "https://accounts.spotify.com/authorize"
                + "?client_id=" + clientId
                + "&response_type=code"
                + "&redirect_uri=" + URLEncoder.encode(REDIRECT_URI, "UTF-8")
                + "&scope=" + URLEncoder.encode(SCOPE, "UTF-8");

        System.out.println("\n  Abriendo navegador para autenticacion con Spotify...");
        System.out.println("  Si no abre automaticamente, visita:");
        System.out.println("    " + authUrl + "\n");

        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            Desktop.getDesktop().browse(new URI(authUrl));
        }

        String code = esperarCodigo();
        obtenerToken(code);
    }

    private String esperarCodigo() throws IOException {
        System.out.println("  Esperando autorizacion en " + REDIRECT_URI + " ...");
        try (ServerSocket server = new ServerSocket(8888, 1, InetAddress.getByName("127.0.0.1"))) {
            try (Socket socket = server.accept()) {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));
                String requestLine = reader.readLine();

                String html = "<html><body style='font-family:sans-serif;text-align:center;padding:40px'>"
                        + "<h1>PlaylistScript autorizado</h1>"
                        + "<p>Puedes cerrar esta pestana y volver a la consola.</p></body></html>";
                PrintWriter writer = new PrintWriter(socket.getOutputStream());
                writer.println("HTTP/1.1 200 OK");
                writer.println("Content-Type: text/html; charset=UTF-8");
                writer.println("Content-Length: " + html.length());
                writer.println();
                writer.print(html);
                writer.flush();

                if (requestLine == null || !requestLine.contains("code=")) {
                    throw new IOException("No se recibio el codigo de autorizacion.");
                }
                return requestLine.split("code=")[1].split("[ &]")[0];
            }
        }
    }

    private void obtenerToken(String code) throws Exception {
        String credenciales = Base64.getEncoder()
                .encodeToString((clientId + ":" + clientSecret).getBytes(StandardCharsets.UTF_8));

        String body = "grant_type=authorization_code"
                + "&code=" + code
                + "&redirect_uri=" + URLEncoder.encode(REDIRECT_URI, "UTF-8");

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("https://accounts.spotify.com/api/token"))
                .header("Authorization", "Basic " + credenciales)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        accessToken = jsonValor(resp.body(), "access_token");
        if (accessToken == null) {
            throw new Exception("No se pudo obtener el token. Respuesta: " + resp.body());
        }
        System.out.println("  Autenticacion exitosa.\n");
    }

    public ArrayList<TrackInfo> buscarCanciones(PlaylistConfig config) throws Exception {
        ArrayList<TrackInfo> resultado = new ArrayList<>();

        int totalCriterios = config.getArtistas().size() + config.getGeneros().size()
                + config.getTracks().size() + config.getAlbums().size();
        int porCriterio = Math.max(8, config.getLimite() / Math.max(1, totalCriterios) + 5);
        String yearFilter = config.getYearQuery();

        for (String track : config.getTracks()) {
            System.out.println("    Buscando track: " + track);
            buscarYAgregar("track:" + track + yearFilter, porCriterio, resultado, config);
            System.out.println("       acumuladas: " + resultado.size());
        }
        for (String artista : config.getArtistas()) {
            System.out.println("    Buscando artista: " + artista);
            buscarYAgregar("artist:" + artista + yearFilter, porCriterio, resultado, config);
            buscarYAgregar(artista + yearFilter, porCriterio, resultado, config);
            buscarYAgregar(artista + " songs" + yearFilter, porCriterio, resultado, config);
            buscarYAgregar(artista + " album" + yearFilter, porCriterio, resultado, config);
            System.out.println("       acumuladas: " + resultado.size());
        }
        for (String genero : config.getGeneros()) {
            System.out.println("    Buscando genero: " + genero);
            buscarYAgregar(genero + " music" + yearFilter, porCriterio, resultado, config);
            System.out.println("       acumuladas: " + resultado.size());
        }
        for (String album : config.getAlbums()) {
            System.out.println("    Buscando album: " + album);
            buscarYAgregar("album:" + album + yearFilter, porCriterio, resultado, config);
            System.out.println("       acumuladas: " + resultado.size());
        }

        while (resultado.size() > config.getLimite()) {
            resultado.remove(resultado.size() - 1);
        }
        return resultado;
    }

    private void buscarYAgregar(String query, int cantidad,
                                ArrayList<TrackInfo> lista, PlaylistConfig config) throws Exception {

        String url = "https://api.spotify.com/v1/search?q="
                + URLEncoder.encode(query, "UTF-8")
                + "&type=track";

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + accessToken)
                .GET().build();

        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (!resp.body().contains("\"items\"")) {
            System.out.println("       Sin resultados.");
            return;
        }

        ArrayList<TrackInfo> encontradas = extraerTracks(resp.body());
        for (TrackInfo track : encontradas) {
            boolean duplicado = config.isSinRepeticion() &&
                    lista.stream().anyMatch(t -> t.uri.equals(track.uri));
            if (!duplicado) lista.add(track);
        }
    }

    public String crearPlaylist(String nombre, String descripcion) throws Exception {
        String nombreJson = nombre.replace("\\", "\\\\").replace("\"", "\\\"");
        String descJson = descripcion.isEmpty()
                ? "Creada con PlaylistScript"
                : descripcion.replace("\\", "\\\\").replace("\"", "\\\"");

        String body = "{\"name\":\"" + nombreJson + "\","
                + "\"public\":true,"
                + "\"description\":\"" + descJson + "\"}";

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("https://api.spotify.com/v1/me/playlists"))
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        String id = jsonValor(resp.body(), "id");
        if (id == null) throw new Exception("No se pudo crear la playlist. Respuesta: " + resp.body());
        return id;
    }

    public void agregarCanciones(String playlistId, ArrayList<TrackInfo> tracks) throws Exception {
        for (int i = 0; i < tracks.size(); i += 100) {
            int fin = Math.min(i + 100, tracks.size());
            StringBuilder sb = new StringBuilder("[");
            for (int j = i; j < fin; j++) {
                sb.append("\"").append(tracks.get(j).uri).append("\"");
                if (j < fin - 1) sb.append(",");
            }
            sb.append("]");

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.spotify.com/v1/playlists/" + playlistId + "/items"))
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString("{\"uris\":" + sb + "}"))
                    .build();

            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() != 201) {
                System.out.println("    Error al agregar canciones: " + resp.statusCode() + " " + resp.body());
            }
        }
    }

    private String jsonValor(String json, String clave) {
        String buscar = "\"" + clave + "\":\"";
        int ini = json.indexOf(buscar);
        if (ini == -1) return null;
        ini += buscar.length();
        int fin = json.indexOf("\"", ini);
        if (fin == -1) return null;
        return json.substring(ini, fin);
    }

    private ArrayList<TrackInfo> extraerTracks(String json) {
        ArrayList<TrackInfo> lista = new ArrayList<>();
        int itemsPos = json.indexOf("\"items\"");
        if (itemsPos == -1) return lista;
        String data = json.substring(itemsPos);

        int pos = 0;
        while (true) {
            int uriIdx = data.indexOf("\"spotify:track:", pos);
            if (uriIdx == -1) break;
            int uriStart = uriIdx + 1;
            int uriEnd = data.indexOf("\"", uriStart + 1);
            if (uriEnd == -1) break;
            String uri = data.substring(uriStart, uriEnd);

            String nombre = "Desconocido";
            int objStart = data.lastIndexOf("{", uriIdx);
            if (objStart != -1) {
                String fragmento = data.substring(objStart, uriIdx);
                int ni = fragmento.indexOf("\"name\":\"");
                if (ni != -1) {
                    ni += 8;
                    int nf = fragmento.indexOf("\"", ni);
                    if (nf != -1) nombre = limpiar(fragmento.substring(ni, nf));
                }
            }

            String artista = "Desconocido";
            int artistsIdx = data.indexOf("\"artists\"", uriIdx);
            if (artistsIdx != -1 && (artistsIdx - uriIdx) < 500) {
                int nameIdx = data.indexOf("\"name\":\"", artistsIdx);
                if (nameIdx != -1 && (nameIdx - artistsIdx) < 200) {
                    int ai = nameIdx + 8;
                    int af = data.indexOf("\"", ai);
                    if (af != -1) artista = limpiar(data.substring(ai, af));
                }
            }

            lista.add(new TrackInfo(nombre, artista, uri));
            pos = uriEnd + 1;
        }
        return lista;
    }

    private String limpiar(String s) {
        return s.replace("\\u00e9", "e").replace("\\u00e1", "a")
                .replace("\\u00ed", "i").replace("\\u00f3", "o")
                .replace("\\u00fa", "u").replace("\\u00f1", "n")
                .replace("\\u00c9", "E").replace("\\u00c1", "A")
                .replace("\\u002F", "/").replace("\\/", "/")
                .replace("\\u0026", "&").replace("\\u0027", "'");
    }
}