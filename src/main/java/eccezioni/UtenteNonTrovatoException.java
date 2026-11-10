package eccezioni;

public class UtenteNonTrovatoException extends RossiGoException {
    private final String idUtente;

    public UtenteNonTrovatoException(String message, String idUtente) {
        super(message);
        this.idUtente = idUtente;
    }

    public UtenteNonTrovatoException(String message, Throwable cause, String idUtente) {
        super(message, cause);
        this.idUtente = idUtente;
    }

    public String getIdUtente() {
        return idUtente;
    }
}
