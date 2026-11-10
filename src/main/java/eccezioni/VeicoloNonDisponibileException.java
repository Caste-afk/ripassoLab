package eccezioni;

import modello.StatoVeicolo;

public class VeicoloNonDisponibileException extends RossiGoException {
    private final String codiceVeicolo;
    private final StatoVeicolo statoAttuale;

    public VeicoloNonDisponibileException(String message, String codiceVeicolo, StatoVeicolo statoAttuale) {
        super(message);
        this.codiceVeicolo = codiceVeicolo;
        this.statoAttuale = statoAttuale;
    }

    public VeicoloNonDisponibileException(String message, Throwable cause, String codiceVeicolo, StatoVeicolo statoAttuale) {
        super(message, cause);
        this.codiceVeicolo = codiceVeicolo;
        this.statoAttuale = statoAttuale;
    }

    public String getCodiceVeicolo() {
        return codiceVeicolo;
    }

    public StatoVeicolo getStatoAttuale() {
        return statoAttuale;
    }
}
