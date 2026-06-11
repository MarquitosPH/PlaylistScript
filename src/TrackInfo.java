public class TrackInfo {
    public final String nombre;
    public final String artista;
    public final String uri;

    public TrackInfo(String nombre, String artista, String uri) {
        this.nombre  = nombre;
        this.artista = artista;
        this.uri     = uri;
    }

    @Override
    public String toString() {
        return String.format("  %-40s — %s", nombre, artista);
    }
}
