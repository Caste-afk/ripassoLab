package modello;

public class Bici extends Veicolo {
    private int numeroMarce;
    private static final double TARIFFA_MINUTARIA = 0.15;
    private static final double CONSUMO_AL_MINUTO = 0.5;

    public Bici(String codice, double livelloBatteria, StatoVeicolo stato, int numeroMarce) {
        super(codice, livelloBatteria, stato);
        this.numeroMarce = numeroMarce;
    }

    @Override
    public double calcolaCosto(int minuti) {
        return minuti * TARIFFA_MINUTARIA;
    }

    @Override
    public double consumoBatteriaPerMinuto() {
        return CONSUMO_AL_MINUTO;
    }

    public int getNumeroMarce() {
        return numeroMarce;
    }

    public void setNumeroMarce(int numeroMarce) {
        this.numeroMarce = numeroMarce;
    }
}
