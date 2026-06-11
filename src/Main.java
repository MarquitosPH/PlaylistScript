import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {

    public static void main(String[] args) {

        System.out.println("========================================================");
        System.out.println("          PlaylistScript                           ");
        System.out.println("      generador de playlists en Spotify          ");
        System.out.println("========================================================");
        System.out.println();

        String script;
        try {
            String archivo = "src/script.txt";
            if (args.length > 0) archivo = args[0];

            java.io.File f = new java.io.File(archivo);
            if (f.exists()) {
                script = new String(Files.readAllBytes(Paths.get(archivo)));
                System.out.println("  Archivo : " + archivo + "\n");
            } else {
                System.err.println("  No se encontro el archivo: " + archivo);
                System.err.println("  Crea un archivo 'script.txt' con tu script.");
                System.err.println("  Ejemplo:\n");
                System.err.println("    CLIENT_ID \"tu_id\"");
                System.err.println("    CLIENT_SECRET \"tu_secret\"");
                System.err.println("    PLAYLIST \"Mi Playlist\"");
                System.err.println("    ARTIST \"Bad Bunny\"");
                System.err.println("    LIMIT 10");
                System.err.println("    NOREPEAT true");
                System.err.println("    CREATE\n");
                return;
            }

            System.out.println("-----------------------------------------------------");
            System.out.println(script);
            System.out.println("-----------------------------------------------------\n");

        } catch (Exception e) {
            System.err.println("  No se pudo leer el archivo: " + e.getMessage());
            return;
        }

        try {
            System.out.println("[Fase 1] Analisis Lexico...");
            PlaylistLexer lexer = new PlaylistLexer();
            lexer.analizar(script);
            System.out.println("  Tokens reconocidos: " + lexer.getTokens().size());

            System.out.println("[Fase 2] Analisis Sintactico...");
            PlaylistParser parser = new PlaylistParser();
            PlaylistConfig config = parser.analizar(lexer);
            System.out.println("  Estructura sintactica correcta.");

            System.out.println("[Fase 3] Analisis Semantico...");
            PlaylistSemantico semantico = new PlaylistSemantico();
            semantico.validar(config);
            System.out.println("  Semantica valida.\n");

            System.out.println("[Fase 4] Autenticacion con Spotify...");
            SpotifyClient spotify = new SpotifyClient();
            spotify.autorizar();

            System.out.println("[Fase 5] Interpretacion del script...\n");
            PlaylistInterprete interprete = new PlaylistInterprete(spotify);
            interprete.ejecutar(config);

        } catch (LexicalException e) {
            System.err.println("\n  ERROR LEXICO      -> " + e.getMessage());
        } catch (SyntaxException e) {
            System.err.println("\n  ERROR SINTACTICO  -> " + e.getMessage());
        } catch (SemanticException e) {
            System.err.println("\n  ERROR SEMANTICO   -> " + e.getMessage());
        } catch (Exception e) {
            System.err.println("\n  ERROR DE EJECUCION -> " + e.getMessage());
            e.printStackTrace();
        }
    }
}                                       