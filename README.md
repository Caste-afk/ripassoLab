# RossiGo - Sistema di Noleggio Bici e Monopattini Elettrici

Nucleo backend Java per la gestione del noleggio, sblocco e ricarica di veicoli di sharing urbano (bici e monopattini elettrici), con gestione polimorfica delle tariffe, gerarchia di eccezioni dedicate e persistenza tramite database SQLite (JDBC).

---

## Requisiti di Sistema
- **Java:** OpenJDK 25 (o versione compatibile con `--release 25`)
- **Build Tool:** Apache Maven 3.8+

---

## Struttura dei Package

Il codice sorgente è organizzato secondo la suddivisione modulare richiesta:
- `modello`: entità di dominio (`Veicolo`, `Bici`, `Monopattino`, `Utente`, `Noleggio`, `StatoVeicolo`).
- `eccezioni`: eccezione radice `RossiGoException` e le relative sottoclassi tipizzate con campi contestuali.
- `servizio`: classe orchestratrice `FlottaService` operante esclusivamente sul tipo astratto `Veicolo`.
- `persistenza`: classe `Database` con pattern Singleton per la gestione di tabelle e query SQLite.
- `app`: classe eseguibile `Main` con il banner ASCII e la simulazione degli scenari.

---

## Librerie Esterne Scelte e Motivazione

| Libreria | Coordinate Maven | Motivazione e Luogo di Utilizzo |
| :--- | :--- | :--- |
| **SQLite JDBC** | `org.xerial:sqlite-jdbc:3.45.1.0` | Driver JDBC per la persistenza su database `rossigo.db`. Utilizzato in `persistenza.Database`. |
| **JFiglet** | `com.github.lalyos:jfiglet:0.0.9` | Rendering del logo `RossiGo` in caratteri ASCII art all'avvio dell'applicazione. Utilizzato in `app.Main.stampaBanner()`. |
| **Codice Fiscale** | `it.kamaladafrica:codice-fiscale:1.6.0` | Validazione formale e verifica del carattere di controllo (CIN) dei codici fiscali italiani in fase di registrazione. Utilizzato in `servizio.FlottaService.registraUtente()`. |
| **Commons Lang3** | `org.apache.commons:commons-lang3:3.14.0` | Validazione difensiva di codici alfanumerici e parametri di input (`StringUtils.isNotBlank`, `Validate.isTrue`). Utilizzato in `servizio.FlottaService`. |
| **SLF4J Simple** | `org.slf4j:slf4j-simple:1.7.36` | Binding per il logger delle dipendenze esterne senza generazione di avvisi a terminale. |

---

## Istruzioni per l'Esecuzione

### Compilazione del progetto
```bash
mvn clean compile
```

### Esecuzione della simulazione completa
```bash
mvn exec:java
```

---

## Scenari Simulati nel Main

1. **Scenario 1 - Registrazione con Codice Fiscale Errato:**
   Tentativo di registrazione di un utente con stringa di codice fiscale sintatticamente non valida. Scatena `CodiceFiscaleNonValidoException` bloccando l'inserimento.
2. **Scenario 2 - Tentativo di Sblocco con Credito Insufficiente:**
   Utente con saldo inferiore alla soglia di sblocco (0.50 EUR a fronte di 1.00 EUR richiesto). Scatena `CreditoInsufficienteException`.
3. **Scenario 3 - Tentativo di Sblocco con Batteria Scarica:**
   Tentativo di sblocco di un monopattino con batteria all'11.0% (< 15%). Scatena `BatteriaScaricaException` comunicando il livello attuale all'utente.
4. **Scenario 4 - Tentativo di Sblocco di Veicolo Già in Uso:**
   Sblocco regolare di una bicicletta da parte dell'utente U001; subito dopo l'utente U003 tenta di sbloccare il medesimo veicolo. Scatena `VeicoloNonDisponibileException`.
5. **Scenario 5 - Riconsegna Veicolo e Bonus Stima Costo:**
   Chiusura regolare del noleggio dopo 20 minuti con addebito tariffario calcolato in modo polimorfico (1 EUR fisso + 0.15 EUR/min per la bici) e riduzione proporzionale della carica della batteria. Segue il test della funzionalità bonus di stima preventiva, che scatena `CreditoInsufficienteException` se la durata prevista supera il credito residuo.
6. **Scenario 6 - Persistenza al Riavvio:**
   Creazione di una nuova istanza di servizio collegata al database `rossigo.db` per verificare che utenti, crediti aggiornati, veicoli e storici di noleggio rimangano persistiti tra le esecuzioni.
