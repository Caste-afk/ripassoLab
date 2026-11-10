package modello;

import java.time.LocalDateTime;

public class Noleggio {
    private Utente utente;
    private Veicolo veicolo;
    private LocalDateTime orarioInizio;
    private LocalDateTime orarioFine;

    public Noleggio(Utente utente, Veicolo veicolo, LocalDateTime orarioInizio, LocalDateTime orarioFine) {
        this.utente = utente;
        this.veicolo = veicolo;
        this.orarioInizio = orarioInizio;
        this.orarioFine = orarioFine;
    }

    public Utente getUtente() {
        return utente;
    }

    public void setUtente(Utente utente) {
        this.utente = utente;
    }

    public Veicolo getVeicolo() {
        return veicolo;
    }

    public void setVeicolo(Veicolo veicolo) {
        this.veicolo = veicolo;
    }

    public LocalDateTime getOrarioInizio() {
        return orarioInizio;
    }

    public void setOrarioInizio(LocalDateTime orarioInizio) {
        this.orarioInizio = orarioInizio;
    }

    public LocalDateTime getOrarioFine() {
        return orarioFine;
    }

    public void setOrarioFine(LocalDateTime orarioFine) {
        this.orarioFine = orarioFine;
    }
}
