package modello;

public abstract class Veicolo {
    private String codice;
    private double livelloBatteria;
    private StatoVeicolo stato;

    public Veicolo(String codice, double livelloBatteria, StatoVeicolo stato) {
        this.codice = codice;
        this.livelloBatteria = livelloBatteria;
        this.stato = stato;
    }

    public abstract double calcolaCosto(int minuti);

    public abstract double consumoBatteriaPerMinuto();

    public void riduciBatteria(double percentuale) {
        this.livelloBatteria = Math.max(0.0, this.livelloBatteria - percentuale);
    }

    public void ricaricaCompleta() {
        this.livelloBatteria = 100.0;
        this.stato = StatoVeicolo.LIBERO;
    }

    public String getCodice() {
        return codice;
    }

    public void setCodice(String codice) {
        this.codice = codice;
    }

    public double getLivelloBatteria() {
        return livelloBatteria;
    }

    public void setLivelloBatteria(double livelloBatteria) {
        this.livelloBatteria = livelloBatteria;
    }

    public StatoVeicolo getStato() {
        return stato;
    }

    public void setStato(StatoVeicolo stato) {
        this.stato = stato;
    }
}
