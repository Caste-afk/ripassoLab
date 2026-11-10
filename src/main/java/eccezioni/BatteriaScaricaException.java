package eccezioni;

public class BatteriaScaricaException extends RossiGoException {
    private final String codiceVeicolo;
    private final double livelloAttuale;

    public BatteriaScaricaException(String message, String codiceVeicolo, double livelloAttuale) {
        super(message);
        this.codiceVeicolo = codiceVeicolo;
        this.livelloAttuale = livelloAttuale;
    }

    public BatteriaScaricaException(String message, Throwable cause, String codiceVeicolo, double livelloAttuale) {
        super(message, cause);
        this.codiceVeicolo = codiceVeicolo;
        this.livelloAttuale = livelloAttuale;
    }

    public String getCodiceVeicolo() {
        return codiceVeicolo;
    }

    public double getLivelloAttuale() {
        return livelloAttuale;
    }
}
