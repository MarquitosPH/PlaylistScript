public class TipoToken {
    private String nombre;
    private String patron;

    public TipoToken(String nombre, String patron) {
        this.nombre = nombre;
        this.patron = patron;
    }

    public String getNombre() { return nombre; }
    public String getPatron() { return patron; }


    public static String PLAYLIST       = "PLAYLIST";
    public static String DESCRIPTION    = "DESCRIPTION";
    public static String GENRE          = "GENRE";
    public static String ARTIST         = "ARTIST";
    public static String TRACK          = "TRACK";
    public static String ALBUM          = "ALBUM";
    public static String YEAR           = "YEAR";
    public static String LIMIT          = "LIMIT";
    public static String NOREPEAT       = "NOREPEAT";
    public static String CREATE         = "CREATE";
    public static String BOOLEANO       = "BOOLEANO";
    public static String NUMERO         = "NUMERO";
    public static String CADENA         = "CADENA";
    public static String GUION          = "GUION";
    public static String ESPACIO        = "ESPACIO";
    public static String ERROR          = "ERROR";
    public static String MIN            = "MIN";
}