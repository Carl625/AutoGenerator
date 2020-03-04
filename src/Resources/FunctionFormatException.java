package Resources;

public class FunctionFormatException extends Exception {

    public FunctionFormatException(String message, Throwable origin) {

        super("Illegal Resources.Function Format: " + message, origin);
    }
}
