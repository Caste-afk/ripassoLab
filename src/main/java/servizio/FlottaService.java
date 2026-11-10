package servizio;

import eccezioni.*;
import it.kamaladafrica.codicefiscale.CodiceFiscale;
import modello.Noleggio;
import modello.StatoVeicolo;
import modello.Utente;
import modello.Veicolo;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import persistenza.Database;

import java.time.LocalDateTime;
import java.util.*;

public class FlottaService {
    private final Database database;
    private final Map<String, Veicolo> veicoli = new HashMap<>();
    private final Map<String, Utente> utenti = new HashMap<>();
    private final Map<String, Noleggio> noleggiAttivi = new HashMap<>();

    public FlottaService(Database database) throws ErroreDatabaseException {
        this.database = Objects.requireNonNull(database);
        caricaDatiDaDatabase();
    }

    private void caricaDatiDaDatabase() throws ErroreDatabaseException {
        List<Veicolo> veicoliDb = database.caricaTuttiVeicoli();
        for (Veicolo v : veicoliDb) {
            veicoli.put(v.getCodice(), v);
        }

        List<Utente> utentiDb = database.caricaTuttiUtenti();
        for (Utente u : utentiDb) {
            utenti.put(u.getId(), u);
        }

        for (Veicolo v : veicoli.values()) {
            if (v.getStato() == StatoVeicolo.IN_USO) {
                database.cercaNoleggioAttivo(v.getCodice(), new ArrayList<>(utenti.values()), new ArrayList<>(veicoli.values()))
                    .ifPresent(n -> noleggiAttivi.put(v.getCodice(), n));
            }
        }
    }

    public void registraVeicolo(Veicolo v) throws ErroreDatabaseException {
        Objects.requireNonNull(v);
        Validate.isTrue(StringUtils.isNotBlank(v.getCodice()), "Il codice veicolo non puo essere vuoto");
        veicoli.put(v.getCodice(), v);
        database.salvaVeicolo(v);
    }

    public void registraUtente(Utente u) throws CodiceFiscaleNonValidoException, ErroreDatabaseException {
        Objects.requireNonNull(u);
        Validate.isTrue(StringUtils.isNotBlank(u.getId()), "L'id utente non puo essere vuoto");
        Validate.isTrue(StringUtils.isNotBlank(u.getNome()), "Il nome non puo essere vuoto");

        if (StringUtils.isBlank(u.getCodiceFiscale()) || !CodiceFiscale.isFormatValid(u.getCodiceFiscale())) {
            throw new CodiceFiscaleNonValidoException("Il codice fiscale fornito non e formalmente valido: " + u.getCodiceFiscale(), u.getCodiceFiscale());
        }

        try {
            CodiceFiscale.of(u.getCodiceFiscale());
        } catch (Exception e) {
            throw new CodiceFiscaleNonValidoException("Errore di checksum o formato nel codice fiscale: " + u.getCodiceFiscale(), e, u.getCodiceFiscale());
        }

        utenti.put(u.getId(), u);
        database.salvaUtente(u);
    }

    public Noleggio sbloccaVeicolo(String codiceVeicolo, String idUtente) throws RossiGoException {
        Utente utente = utenti.get(idUtente);
        if (utente == null) {
            throw new UtenteNonTrovatoException("Utente non trovato nel sistema: " + idUtente, idUtente);
        }

        if (utente.getCredito() < 1.0) {
            throw new CreditoInsufficienteException("Credito insufficiente per lo sblocco. Minimo richiesto 1.00 EUR.", idUtente, utente.getCredito(), 1.0);
        }

        Veicolo veicolo = veicoli.get(codiceVeicolo);
        if (veicolo == null) {
            throw new RossiGoException("Veicolo non censito nella flotta: " + codiceVeicolo);
        }

        if (veicolo.getStato() != StatoVeicolo.LIBERO) {
            throw new VeicoloNonDisponibileException("Il veicolo non e disponibile al momento (stato: " + veicolo.getStato() + ")", codiceVeicolo, veicolo.getStato());
        }

        if (veicolo.getLivelloBatteria() < 15.0) {
            throw new BatteriaScaricaException("Batteria al " + String.format(Locale.US, "%.1f", veicolo.getLivelloBatteria()) + "%. Soglia minima di sblocco: 15%.", codiceVeicolo, veicolo.getLivelloBatteria());
        }

        veicolo.setStato(StatoVeicolo.IN_USO);
        Noleggio noleggio = new Noleggio(utente, veicolo, LocalDateTime.now(), null);
        noleggiAttivi.put(codiceVeicolo, noleggio);

        database.salvaVeicolo(veicolo);
        database.registraInizioNoleggio(noleggio);

        return noleggio;
    }

    public double terminaNoleggio(String codiceVeicolo, int minutiUsati) throws RossiGoException {
        Noleggio noleggio = noleggiAttivi.remove(codiceVeicolo);
        if (noleggio == null) {
            throw new RossiGoException("Nessun noleggio attivo trovato per il veicolo: " + codiceVeicolo);
        }

        Veicolo veicolo = noleggio.getVeicolo();
        Utente utente = noleggio.getUtente();

        double costoVariabile = veicolo.calcolaCosto(minutiUsati);
        double costoTotale = 1.0 + costoVariabile;

        double consumoBatteria = veicolo.consumoBatteriaPerMinuto() * minutiUsati;
        veicolo.riduciBatteria(consumoBatteria);

        if (veicolo.getLivelloBatteria() < 15.0) {
            veicolo.setStato(StatoVeicolo.IN_RICARICA);
        } else {
            veicolo.setStato(StatoVeicolo.LIBERO);
        }

        utente.addebita(costoTotale);

        LocalDateTime orarioFine = LocalDateTime.now();
        noleggio.setOrarioFine(orarioFine);

        database.salvaVeicolo(veicolo);
        database.salvaUtente(utente);
        database.registraFineNoleggio(codiceVeicolo, orarioFine);

        return costoTotale;
    }

    public void mettiInRicarica(String codiceVeicolo) throws RossiGoException {
        Veicolo veicolo = veicoli.get(codiceVeicolo);
        if (veicolo == null) {
            throw new RossiGoException("Veicolo non trovato: " + codiceVeicolo);
        }

        if (veicolo.getStato() == StatoVeicolo.IN_USO) {
            throw new VeicoloNonDisponibileException("Impossibile mettere in ricarica un veicolo attualmente in uso", codiceVeicolo, veicolo.getStato());
        }

        veicolo.ricaricaCompleta();
        database.salvaVeicolo(veicolo);
    }

    public double stimaCosto(String codiceVeicolo, String idUtente, int minuti) throws RossiGoException {
        Utente utente = utenti.get(idUtente);
        if (utente == null) {
            throw new UtenteNonTrovatoException("Utente non trovato: " + idUtente, idUtente);
        }

        Veicolo veicolo = veicoli.get(codiceVeicolo);
        if (veicolo == null) {
            throw new RossiGoException("Veicolo non trovato: " + codiceVeicolo);
        }

        double costoStimato = 1.0 + veicolo.calcolaCosto(minuti);

        if (utente.getCredito() < costoStimato) {
            throw new CreditoInsufficienteException("Credito insufficiente per completare la corsa stimata di " + minuti + " minuti.", idUtente, utente.getCredito(), costoStimato);
        }

        return costoStimato;
    }

    public Optional<Veicolo> getVeicolo(String codice) {
        return Optional.ofNullable(veicoli.get(codice));
    }

    public Optional<Utente> getUtente(String id) {
        return Optional.ofNullable(utenti.get(id));
    }

    public Collection<Veicolo> getTuttiVeicoli() {
        return Collections.unmodifiableCollection(veicoli.values());
    }

    public Collection<Utente> getTuttiUtenti() {
        return Collections.unmodifiableCollection(utenti.values());
    }
}
