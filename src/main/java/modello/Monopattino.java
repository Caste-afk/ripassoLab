package modello;

public class Monopattino extends Veicolo {
    private int velocitaMassima;
    private boolean limitatoInZonaPedonale;
    private static final double TARIFFA_MINUTARIA = 0.25;
    private static final double CONSUMO_AL_MINUTO = 1.5;

    public Monopattino(String codice, double livelloBatteria, StatoVeicolo stato, int velocitaMassima, boolean limitatoInZonaPedonale) {
        super(codice, livelloBatteria, stato);
        this.velocitaMassima = velocitaMassima;
        this.limitatoInZonaPedonale = limitatoInZonaPedonale;
    }

    @Override
    public double calcolaCosto(int minuti) {
        return minuti * TARIFFA_MINUTARIA;
    }

    @Override
    public double consumoBatteriaPerMinuto() {
        return CONSUMO_AL_MINUTO;
    }

    public int getVelocitaMassima() {
        return velocitaMassima;
    }

    public void setVelocitaMassima(int velocitaMassima) {
        this.velocitaMassima = velocitaMassima;
    }

    public boolean isLimitatoInZonaPedonale() {
        return limitatoInZonaPedonale;
    }

    public void setLimitatoInZonaPedonale(boolean limitatoInZonaPedonale) {
        this.limitatoInZonaPedonale = limitatoInZonaPedonale;
    }
}
