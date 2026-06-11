public class SyntaxException extends Exception {
    public SyntaxException(String message) {
        super(message);
    }
    public SyntaxException(String esperado, String encontrado) {
        super("Se esperaba un token '" + esperado + "' y se encontro '" + encontrado + "'");
    }
}