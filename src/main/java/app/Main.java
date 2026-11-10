package app;

import com.github.lalyos.jfiglet.FigletFont;
import eccezioni.*;
import modello.Bici;
import modello.Monopattino;
import modello.StatoVeicolo;
import modello.Utente;
import persistenza.Database;
import servizio.FlottaService;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        stampaBanner();

        try {
            Database db = Database.getIstanza("rossigo.db");
            FlottaService service = new FlottaService(db);

            inizializzaFlottaIniziale(service);

            eseguiScenario1RegistrazioneCfErrato(service);
            eseguiScenario2SbloccoCreditoInsufficiente(service);
            eseguiScenario3SbloccoBatteriaScarica(service);
            eseguiScenario4SbloccoVeicoloGiaInUso(service);
            eseguiScenario5TermineCorsaEBonusStima(service);
            eseguiScenario6VerificaPersistenza();

        } catch (ErroreDatabaseException e) {
            System.err.println("[ERRORE CRITICO DB] " + e.getMessage());
        } catch (Exception e) {
            System.err.println("[ERRORE IMPREVISTO] " + e.getMessage());
        }
    }

    private static void stampaBanner() {
        try {
            String asciiArt = FigletFont.convertOneLine("RossiGo");
            System.out.println(asciiArt);
        } catch (IOException e) {
            System.out.println("==========================================");
            System.out.println("                ROSSIGO                   ");
            System.out.println("==========================================");
        }
        System.out.println("Sistema di Sharing Bici e Monopattini Elettrici");
        System.out.println("Avvio simulazione scenari operativi...\n");
    }

    private static void inizializzaFlottaIniziale(FlottaService service) throws ErroreDatabaseException {
        System.out.println("--- [FASE SETUP: ALLINEAMENTO FLOTTA E UTENTI] ---");

        service.registraVeicolo(new Bici("BK-0042", 85.0, StatoVeicolo.LIBERO, 7));
        service.registraVeicolo(new Monopattino("SC-0107", 95.0, StatoVeicolo.LIBERO, 25, true));
        service.registraVeicolo(new Monopattino("SC-0999", 11.0, StatoVeicolo.LIBERO, 20, false));

        try {
            if (service.getUtente("U001").isEmpty()) {
                service.registraUtente(new Utente("U001", "Mario Rossi", "mario.rossi@email.it", "RSSMRA85M01H501Q", 15.0));
            }
            if (service.getUtente("U002").isEmpty()) {
                service.registraUtente(new Utente("U002", "Luca Bianchi", "luca.bianchi@email.it", "BNCLCU90A01F205V", 0.50));
            }
            if (service.getUtente("U003").isEmpty()) {
                service.registraUtente(new Utente("U003", "Giulia Verdi", "giulia.verdi@email.it", "VRDGLI95E41L219Z", 20.0));
            }
            System.out.println("Allineamento completato: veicoli e utenti pronti nel sistema.\n");
        } catch (CodiceFiscaleNonValidoException e) {
            System.out.println("Errore di registrazione utente: " + e.getMessage());
        }
    }

    private static void eseguiScenario1RegistrazioneCfErrato(FlottaService service) {
        System.out.println(">>> SCENARIO 1: Registrazione utente con codice fiscale errato");
        Utente utenteErrato = new Utente("U004", "Paolo Neri", "paolo.neri@email.it", "CF_NON_VALIDO_123", 10.0);

        try {
            service.registraUtente(utenteErrato);
            System.out.println("SUCCESSO INATTESO: Utente registrato.");
        } catch (CodiceFiscaleNonValidoException e) {
            System.out.println("[ECCEZIONE CATTURATA] " + e.getClass().getSimpleName());
            System.out.println("Messaggio per l'utente: " + e.getMessage());
            System.out.println("Dettaglio tecnico: Codice fornito: " + e.getCodiceFiscale() + " non conforme.\n");
        } catch (ErroreDatabaseException e) {
            System.out.println("[ERRORE DB] " + e.getMessage());
        }
    }

    private static void eseguiScenario2SbloccoCreditoInsufficiente(FlottaService service) {
        System.out.println(">>> SCENARIO 2: Tentativo di sblocco con credito insufficiente (< 1.00 EUR)");
        try {
            service.sbloccaVeicolo("SC-0107", "U002");
            System.out.println("SUCCESSO INATTESO: Mezzo sbloccato.");
        } catch (CreditoInsufficienteException e) {
            System.out.println("[ECCEZIONE CATTURATA] " + e.getClass().getSimpleName());
            System.out.println("Messaggio per l'utente: " + e.getMessage());
            System.out.printf("Dettaglio utente %s: Credito %.2f EUR, Soglia richiesta %.2f EUR.%n%n",
                    e.getIdUtente(), e.getCreditoAttuale(), e.getCreditoRichiesto());
        } catch (RossiGoException e) {
            System.out.println("[ERRORE] " + e.getMessage());
        }
    }

    private static void eseguiScenario3SbloccoBatteriaScarica(FlottaService service) {
        System.out.println(">>> SCENARIO 3: Tentativo di sblocco con batteria sotto la soglia minima (< 15%)");
        try {
            service.sbloccaVeicolo("SC-0999", "U001");
            System.out.println("SUCCESSO INATTESO: Mezzo sbloccato.");
        } catch (BatteriaScaricaException e) {
            System.out.println("[ECCEZIONE CATTURATA] " + e.getClass().getSimpleName());
            System.out.println("Messaggio per l'utente: " + e.getMessage());
            System.out.printf("Dettaglio mezzo %s: Livello batteria attuale %.1f%%. Scegliere un altro veicolo con carica adeguata.%n%n",
                    e.getCodiceVeicolo(), e.getLivelloAttuale());
        } catch (RossiGoException e) {
            System.out.println("[ERRORE] " + e.getMessage());
        }
    }

    private static void eseguiScenario4SbloccoVeicoloGiaInUso(FlottaService service) {
        System.out.println(">>> SCENARIO 4: Sblocco valido e tentativo di sblocco concorrente sullo stesso veicolo");
        try {
            service.sbloccaVeicolo("BK-0042", "U001");
            System.out.println("[OPERAZIONE RIUSCITA] Mario Rossi (U001) ha sbloccato la bici BK-0042.");

            System.out.println("Giulia Verdi (U003) tenta di sbloccare la stessa bici BK-0042...");
            service.sbloccaVeicolo("BK-0042", "U003");
            System.out.println("SUCCESSO INATTESO: Mezzo sbloccato.");
        } catch (VeicoloNonDisponibileException e) {
            System.out.println("[ECCEZIONE CATTURATA] " + e.getClass().getSimpleName());
            System.out.println("Messaggio per l'utente: " + e.getMessage());
            System.out.printf("Dettaglio mezzo %s: Stato attuale: %s.%n%n",
                    e.getCodiceVeicolo(), e.getStatoAttuale());
        } catch (RossiGoException e) {
            System.out.println("[ERRORE] " + e.getMessage());
        }
    }

    private static void eseguiScenario5TermineCorsaEBonusStima(FlottaService service) {
        System.out.println(">>> SCENARIO 5: Termine regolare noleggio e Bonus Stima Costo");
        try {
            int minutiTrascorsi = 20;
            double costoTotale = service.terminaNoleggio("BK-0042", minutiTrascorsi);
            System.out.printf("[CORSA TERMINATA] Bici BK-0042 riconsegnata. Minuti: %d. Costo totale addebitato: %.2f EUR.%n",
                    minutiTrascorsi, costoTotale);

            Utente u1 = service.getUtente("U001").orElseThrow();
            System.out.printf("Saldo aggiornato utente U001: %.2f EUR.%n", u1.getCredito());

            System.out.println("Test Bonus Stima Costo: Stima corsa di 120 minuti con monopattino SC-0107 per Mario Rossi...");
            try {
                double costoStimato = service.stimaCosto("SC-0107", "U001", 120);
                System.out.printf("Costo stimato: %.2f EUR.%n", costoStimato);
            } catch (CreditoInsufficienteException e) {
                System.out.println("[ECCEZIONE CATTURATA] " + e.getClass().getSimpleName());
                System.out.println("Messaggio per l'utente: " + e.getMessage());
                System.out.printf("Dettaglio credito: Disponibile %.2f EUR, Richiesto stimato %.2f EUR.%n%n",
                        e.getCreditoAttuale(), e.getCreditoRichiesto());
            }

        } catch (RossiGoException e) {
            System.out.println("[ERRORE] " + e.getMessage() + "\n");
        }
    }

    private static void eseguiScenario6VerificaPersistenza() {
        System.out.println(">>> SCENARIO 6: Simulazione riavvio del programma e verifica persistenza SQLite");
        try {
            Database dbRiavviato = Database.getIstanza("rossigo.db");
            FlottaService serviceRiavviato = new FlottaService(dbRiavviato);

            Utente mario = serviceRiavviato.getUtente("U001")
                    .orElseThrow(() -> new RossiGoException("Utente non trovato a DB!"));

            var bici = serviceRiavviato.getVeicolo("BK-0042")
                    .orElseThrow(() -> new RossiGoException("Veicolo non trovato a DB!"));

            System.out.println("[DATI RECUPERATI DA DISCO CON SUCCESSO]");
            System.out.printf("- Utente U001 (%s): Credito residuo persistito = %.2f EUR%n",
                    mario.getNome(), mario.getCredito());
            System.out.printf("- Bici BK-0042: Stato = %s, Batteria residua persistita = %.1f%%%n",
                    bici.getStato(), bici.getLivelloBatteria());
            System.out.println("Persistenza SQLite verificata: i dati sopravvivono alla chiusura del programma.\n");

        } catch (RossiGoException e) {
            System.out.println("[ERRORE PERSISTENZA] " + e.getMessage());
        }
    }
}
