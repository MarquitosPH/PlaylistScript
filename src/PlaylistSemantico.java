import java.util.ArrayList;

public class PlaylistSemantico {

    public void validar(PlaylistConfig config) throws SemanticException {



        if (config.getNombre() == null || config.getNombre().trim().isEmpty()) {
            throw new SemanticException("La playlist debe tener un nombre.");
        }

        if (config.getGeneros().isEmpty() && config.getArtistas().isEmpty()
                && config.getTracks().isEmpty() && config.getAlbums().isEmpty()) {
            throw new SemanticException("El script debe contener al menos un GENRE, ARTIST, TRACK o ALBUM.");
        }

        if (config.getLimite() <= 0) {
            throw new SemanticException("LIMIT debe ser mayor a 0. Valor: " + config.getLimite());
        }
        if (config.getLimite() > 100) {
            throw new SemanticException("LIMIT no puede superar 100. Valor: " + config.getLimite());
        }
        if (config.getMinimo() < 0) {
            throw new SemanticException("MIN no puede ser negativo. Valor: " + config.getMinimo());
        }
        if (config.getMinimo() > config.getLimite()) {
            throw new SemanticException("MIN (" + config.getMinimo() + ") no puede ser mayor que LIMIT (" + config.getLimite() + ").");
        }

        verificarDuplicados(config.getGeneros(), "genero");
        verificarDuplicados(config.getArtistas(), "artista");
        verificarDuplicados(config.getTracks(), "track");
        verificarDuplicados(config.getAlbums(), "album");

        // Cadenas vacias o solo espacios
        for (String g : config.getGeneros()) {
            if (g.trim().isEmpty())
                throw new SemanticException("No se permite un GENRE vacio.");
        }
        for (String a : config.getArtistas()) {
            if (a.trim().isEmpty())
                throw new SemanticException("No se permite un ARTIST vacio.");
        }
        for (String t : config.getTracks()) {
            if (t.trim().isEmpty())
                throw new SemanticException("No se permite un TRACK vacio.");
        }
        for (String al : config.getAlbums()) {
            if (al.trim().isEmpty())
                throw new SemanticException("No se permite un ALBUM vacio.");
        }

        if (config.tieneYear()) {
            int yi = config.getYearInicio();
            if (yi < 1900 || yi > 2030) {
                throw new SemanticException("YEAR fuera de rango (1900-2030). Valor: " + yi);
            }
            int yf = config.getYearFin();
            if (yf > 0) {
                if (yf < 1900 || yf > 2030) {
                    throw new SemanticException("YEAR fin fuera de rango (1900-2030). Valor: " + yf);
                }
                if (yi > yf) {
                    throw new SemanticException("YEAR inicio (" + yi + ") no puede ser mayor que YEAR fin (" + yf + ").");
                }
            }
        }
    }

    private void verificarDuplicados(ArrayList<String> lista, String tipo) throws SemanticException {
        ArrayList<String> vistos = new ArrayList<>();
        for (String item : lista) {
            String lower = item.toLowerCase();
            if (vistos.contains(lower)) {
                throw new SemanticException("El " + tipo + " \"" + item + "\" esta declarado mas de una vez.");
            }
            vistos.add(lower);
        }
    }
}