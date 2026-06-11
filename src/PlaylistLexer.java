import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlaylistLexer {

    private final ArrayList<TipoToken> tipos  = new ArrayList<>();
    private final ArrayList<Token>     tokens = new ArrayList<>();

    public PlaylistLexer() {
        tipos.add(new TipoToken(TipoToken.PLAYLIST,     "PLAYLIST"));
        tipos.add(new TipoToken(TipoToken.DESCRIPTION,  "DESCRIPTION"));
        tipos.add(new TipoToken(TipoToken.GENRE,        "GENRE"));
        tipos.add(new TipoToken(TipoToken.ARTIST,       "ARTIST"));
        tipos.add(new TipoToken(TipoToken.TRACK,        "TRACK"));
        tipos.add(new TipoToken(TipoToken.ALBUM,        "ALBUM"));
        tipos.add(new TipoToken(TipoToken.YEAR,         "YEAR"));
        tipos.add(new TipoToken(TipoToken.MIN,         "MIN"));
        tipos.add(new TipoToken(TipoToken.LIMIT,        "LIMIT"));
        tipos.add(new TipoToken(TipoToken.NOREPEAT,     "NOREPEAT"));
        tipos.add(new TipoToken(TipoToken.CREATE,       "CREATE"));
        tipos.add(new TipoToken(TipoToken.BOOLEANO,     "true|false"));
        tipos.add(new TipoToken(TipoToken.NUMERO,       "[0-9]+"));
        tipos.add(new TipoToken(TipoToken.CADENA,       "\"[^\"]*\""));
        tipos.add(new TipoToken(TipoToken.GUION,        "\\-"));
        tipos.add(new TipoToken(TipoToken.ESPACIO,      "[ \\t\\f\\r\\n]+"));
        tipos.add(new TipoToken(TipoToken.ERROR,        "[^ \\t\\f\\r\\n]+"));
    }

    public ArrayList<Token> getTokens() { return tokens; }

    public void analizar(String entrada) throws LexicalException {
        StringBuilder er = new StringBuilder();
        for (TipoToken tt : tipos) {
            er.append(String.format("|(?<%s>%s)", tt.getNombre(), tt.getPatron()));
        }

        Pattern p = Pattern.compile(er.substring(1));
        Matcher m = p.matcher(entrada);

        while (m.find()) {
            if (m.group(TipoToken.ESPACIO) != null) continue;

            for (TipoToken tt : tipos) {
                if (m.group(tt.getNombre()) != null) {
                    if (tt.getNombre().equals(TipoToken.ERROR)) {
                        throw new LexicalException(m.group(tt.getNombre()));
                    }
                    String valor = m.group(tt.getNombre());
                    if (tt.getNombre().equals(TipoToken.CADENA)) {
                        valor = valor.substring(1, valor.length() - 1);
                    }
                    tokens.add(new Token(tt, valor));
                    break;
                }
            }
        }
    }
}