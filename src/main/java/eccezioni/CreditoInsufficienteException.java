package eccezioni;

public class CreditoInsufficienteException extends RossiGoException {
    private final String idUtente;
    private final double creditoAttuale;
    private final double creditoRichiesto;

    public CreditoInsufficienteException(String message, String idUtente, double creditoAttuale, double creditoRichiesto) {
        super(message);
        this.idUtente = idUtente;
        this.creditoAttuale = creditoAttuale;
        this.creditoRichiesto = creditoRichiesto;
    }

    public CreditoInsufficienteException(String message, Throwable cause, String idUtente, double creditoAttuale, double creditoRichiesto) {
        super(message, cause);
        this.idUtente = idUtente;
        this.creditoAttuale = creditoAttuale;
        this.creditoRichiesto = creditoRichiesto;
    }

    public String getIdUtente() {
        return idUtente;
    }

    public double getCreditoAttuale() {
        return creditoAttuale;
    }

    public double getCreditoRichiesto() {
        return creditoRichiesto;
    }
}
