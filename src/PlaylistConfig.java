import java.util.ArrayList;

public class PlaylistConfig {


    private String            nombre;
    private String            descripcion    = "";
    private ArrayList<String> generos        = new ArrayList<>();
    private ArrayList<String> artistas       = new ArrayList<>();
    private ArrayList<String> tracks         = new ArrayList<>();
    private ArrayList<String> albums         = new ArrayList<>();
    private int               yearInicio     = -1;
    private int               yearFin        = -1;
    private int               limite         = 20;
    private boolean           sinRepeticion  = true;
    private int               minimo         = 0;


    public void setNombre(String nombre)            { this.nombre = nombre; }
    public void setDescripcion(String desc)         { this.descripcion = desc; }
    public void agregarGenero(String genero)        { generos.add(genero); }
    public void agregarArtista(String artista)      { artistas.add(artista); }
    public void agregarTrack(String track)          { tracks.add(track); }
    public void agregarAlbum(String album)          { albums.add(album); }
    public void setYearInicio(int y)                { this.yearInicio = y; }
    public void setYearFin(int y)                   { this.yearFin = y; }
    public void setLimite(int limite)               { this.limite = limite; }
    public void setSinRepeticion(boolean v)         { this.sinRepeticion = v; }
    public void setMinimo(int minimo)               { this.minimo = minimo; }
    public int               getMinimo()            { return minimo; }

    public String            getNombre()            { return nombre; }
    public String            getDescripcion()       { return descripcion; }
    public ArrayList<String> getGeneros()           { return generos; }
    public ArrayList<String> getArtistas()          { return artistas; }
    public ArrayList<String> getTracks()            { return tracks; }
    public ArrayList<String> getAlbums()            { return albums; }
    public int               getYearInicio()        { return yearInicio; }
    public int               getYearFin()           { return yearFin; }
    public int               getLimite()            { return limite; }
    public boolean           isSinRepeticion()      { return sinRepeticion; }
    public boolean           tieneYear()            { return yearInicio > 0; }

    public String getYearQuery() {
        if (yearInicio <= 0) return "";
        if (yearFin > 0 && yearFin != yearInicio)
            return " year:" + yearInicio + "-" + yearFin;
        return " year:" + yearInicio;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("  Nombre      : \"").append(nombre).append("\"\n");
        if (!descripcion.isEmpty())
            sb.append("  Descripcion : \"").append(descripcion).append("\"\n");
        if (!generos.isEmpty())  sb.append("  Generos     : ").append(generos).append("\n");
        if (!artistas.isEmpty()) sb.append("  Artistas    : ").append(artistas).append("\n");
        if (!tracks.isEmpty())   sb.append("  Tracks      : ").append(tracks).append("\n");
        if (!albums.isEmpty())   sb.append("  Albums      : ").append(albums).append("\n");
        if (minimo > 0) sb.append("  Minimo      : ").append(minimo).append("\n");
        if (yearInicio > 0) {
            sb.append("  Year        : ").append(yearInicio);
            if (yearFin > 0 && yearFin != yearInicio) sb.append("-").append(yearFin);
            sb.append("\n");
        }
        sb.append("  Limite      : ").append(limite).append("\n");
        sb.append("  Sin repetir : ").append(sinRepeticion);
        return sb.toString();
    }
}