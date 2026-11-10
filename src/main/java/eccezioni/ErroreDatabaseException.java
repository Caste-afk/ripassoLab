package eccezioni;

public class ErroreDatabaseException extends RossiGoException {
    public ErroreDatabaseException(String message) {
        super(message);
    }

    public ErroreDatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
