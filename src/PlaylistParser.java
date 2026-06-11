import java.util.ArrayList;

public class PlaylistParser {

    private ArrayList<Token> tokens;
    private int              pos;
    private Token            tokenActual;

    private boolean limitDeclarado       = false;
    private boolean yearDeclarado        = false;
    private boolean minDeclarado         = false;
    private boolean norepeatDeclarado    = false;
    private boolean descriptionDeclarada = false;

    public PlaylistConfig analizar(PlaylistLexer lexer) throws SyntaxException {
        this.tokens      = lexer.getTokens();
        this.pos         = 0;
        this.tokenActual = tokens.isEmpty() ? null : tokens.get(0);
        return parsePrograma();
    }

    // <Programa> ::= (CLIENT_ID CADENA)? (CLIENT_SECRET CADENA)? PLAYLIST CADENA <Reglas> CREATE
    private PlaylistConfig parsePrograma() throws SyntaxException {
        PlaylistConfig config = new PlaylistConfig();


        match(TipoToken.PLAYLIST);
        config.setNombre(match(TipoToken.CADENA));

        parseReglas(config);

        match(TipoToken.CREATE);

        if (tokenActual != null) {
            throw new SyntaxException(
                    "Tokens inesperados despues de CREATE: " + tokenActual.getNombre());
        }
        return config;
    }

    private void parseReglas(PlaylistConfig config) throws SyntaxException {
        while (tokenActual != null
                && !tokenActual.getTipo().getNombre().equals(TipoToken.CREATE)) {
            parseRegla(config);
        }
    }

    private void parseRegla(PlaylistConfig config) throws SyntaxException {
        if (tokenActual == null) {
            throw new SyntaxException("Se esperaba una regla pero el script termino sin CREATE.");
        }

        String tipo = tokenActual.getTipo().getNombre();

        switch (tipo) {
            case "GENRE":
                match(TipoToken.GENRE);
                config.agregarGenero(match(TipoToken.CADENA));
                break;

            case "ARTIST":
                match(TipoToken.ARTIST);
                config.agregarArtista(match(TipoToken.CADENA));
                break;

            case "TRACK":
                match(TipoToken.TRACK);
                config.agregarTrack(match(TipoToken.CADENA));
                break;

            case "ALBUM":
                match(TipoToken.ALBUM);
                config.agregarAlbum(match(TipoToken.CADENA));
                break;

            case "DESCRIPTION":
                if (descriptionDeclarada)
                    throw new SyntaxException("DESCRIPTION ya fue declarado. Solo se permite una vez.");
                descriptionDeclarada = true;
                match(TipoToken.DESCRIPTION);
                config.setDescripcion(match(TipoToken.CADENA));
                break;

            case "YEAR":
                if (yearDeclarado)
                    throw new SyntaxException("YEAR ya fue declarado. Solo se permite una vez.");
                yearDeclarado = true;
                match(TipoToken.YEAR);
                String anio = match(TipoToken.NUMERO);
                config.setYearInicio(Integer.parseInt(anio));
                if (tokenActual != null
                        && tokenActual.getTipo().getNombre().equals(TipoToken.GUION)) {
                    match(TipoToken.GUION);
                    config.setYearFin(Integer.parseInt(match(TipoToken.NUMERO)));
                }
                break;

            case "LIMIT":
                if (limitDeclarado)
                    throw new SyntaxException("LIMIT ya fue declarado. Solo se permite una vez.");
                limitDeclarado = true;
                match(TipoToken.LIMIT);
                config.setLimite(Integer.parseInt(match(TipoToken.NUMERO)));
                break;

            case "NOREPEAT":
                if (norepeatDeclarado)
                    throw new SyntaxException("NOREPEAT ya fue declarado. Solo se permite una vez.");
                norepeatDeclarado = true;
                match(TipoToken.NOREPEAT);
                config.setSinRepeticion(Boolean.parseBoolean(match(TipoToken.BOOLEANO)));
                break;

            case "MIN":
                if (minDeclarado)
                    throw new SyntaxException("MIN ya fue declarado. Solo se permite una vez.");
                minDeclarado = true;
                match(TipoToken.MIN);
                config.setMinimo(Integer.parseInt(match(TipoToken.NUMERO)));
                break;

            default:
                throw new SyntaxException(
                        "Token inesperado: '" + tokenActual.getNombre()
                                + "'. Se esperaba GENRE, ARTIST, TRACK, ALBUM, DESCRIPTION, YEAR, MIN, LIMIT, NOREPEAT o CREATE."
                );
        }
    }

    private String match(String tipoEsperado) throws SyntaxException {
        if (tokenActual == null) {
            throw new SyntaxException(tipoEsperado, "fin de archivo");
        }
        if (!tokenActual.getTipo().getNombre().equals(tipoEsperado)) {
            throw new SyntaxException(tipoEsperado, tokenActual.getTipo().getNombre());
        }
        String valor = tokenActual.getNombre();
        pos++;
        tokenActual = (pos < tokens.size()) ? tokens.get(pos) : null;
        return valor;
    }
}