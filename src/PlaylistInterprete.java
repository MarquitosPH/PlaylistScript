import java.util.ArrayList;
import java.util.Scanner;

public class PlaylistInterprete {

    private final SpotifyClient spotify;

    public PlaylistInterprete(SpotifyClient spotify) {
        this.spotify = spotify;
    }

    public void ejecutar(PlaylistConfig config) throws Exception {

        linea();
        System.out.println("  CONFIGURACION DEL SCRIPT");
        linea();
        System.out.println(config);
        linea();

        System.out.println("\n  Buscando canciones en Spotify...\n");
        ArrayList<TrackInfo> tracks = spotify.buscarCanciones(config);

        if (tracks.isEmpty()) {
            System.out.println("  No se encontraron canciones con los criterios indicados.");
            return;
        }

        if (config.getMinimo() > 0 && tracks.size() < config.getMinimo()) {
            System.out.println("  Se encontraron " + tracks.size() + " canciones pero el minimo es " + config.getMinimo() + ".");
            System.out.println("  Agrega mas GENRE o ARTIST para obtener suficientes canciones.");
            return;
        }


        System.out.println("  Se encontraron " + tracks.size() + " canciones:\n");
        for (int i = 0; i < tracks.size(); i++) {
            System.out.printf("  %3d. %s%n", i + 1, tracks.get(i));
        }

        System.out.println();
        linea();
        System.out.print("  Crear la playlist \"" + config.getNombre()
                + "\" con " + tracks.size() + " canciones? (s/n): ");

        Scanner scanner = new Scanner(System.in);
        String respuesta = scanner.nextLine().trim().toLowerCase();

        if (!respuesta.equals("s") && !respuesta.equals("si")) {
            System.out.println("\n  Operacion cancelada.");
            return;
        }

        System.out.println("\n  Creando playlist...");
        String playlistId = spotify.crearPlaylist(config.getNombre(), config.getDescripcion());
        spotify.agregarCanciones(playlistId, tracks);

        linea();
        System.out.println("  Playlist creada exitosamente!");
        System.out.println("  Nombre    : " + config.getNombre());
        if (!config.getDescripcion().isEmpty())
            System.out.println("  Desc.     : " + config.getDescripcion());
        System.out.println("  Canciones : " + tracks.size());
        System.out.println("  URL       : https://open.spotify.com/playlist/" + playlistId);
        System.out.println("  (La playlist queda guardada permanentemente en tu cuenta de Spotify)");
        linea();
    }

    private void linea() {
        System.out.println("  ======================================================");
    }
}