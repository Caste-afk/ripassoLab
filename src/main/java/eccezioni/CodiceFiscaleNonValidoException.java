package eccezioni;

public class CodiceFiscaleNonValidoException extends RossiGoException {
    private final String codiceFiscale;

    public CodiceFiscaleNonValidoException(String message, String codiceFiscale) {
        super(message);
        this.codiceFiscale = codiceFiscale;
    }

    public CodiceFiscaleNonValidoException(String message, Throwable cause, String codiceFiscale) {
        super(message, cause);
        this.codiceFiscale = codiceFiscale;
    }

    public String getCodiceFiscale() {
        return codiceFiscale;
    }
}
