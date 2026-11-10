package persistenza;

import eccezioni.ErroreDatabaseException;
import modello.*;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Database {
    private static Database istanza;
    private final String url;

    private Database(String nomeFileDb) throws ErroreDatabaseException {
        this.url = "jdbc:sqlite:" + nomeFileDb;
        inizializzaTabelle();
    }

    public static synchronized Database getIstanza() throws ErroreDatabaseException {
        if (istanza == null) {
            istanza = new Database("rossigo.db");
        }
        return istanza;
    }

    public static synchronized Database getIstanza(String nomeFileDb) throws ErroreDatabaseException {
        if (istanza == null) {
            istanza = new Database(nomeFileDb);
        }
        return istanza;
    }

    private Connection getConnessione() throws SQLException {
        return DriverManager.getConnection(url);
    }

    private void inizializzaTabelle() throws ErroreDatabaseException {
        String sqlVeicoli = """
            CREATE TABLE IF NOT EXISTS veicoli (
                codice TEXT PRIMARY KEY,
                tipo TEXT NOT NULL,
                livello_batteria REAL NOT NULL,
                stato TEXT NOT NULL,
                numero_marce INTEGER,
                velocita_massima INTEGER,
                limitato_zona_pedonale INTEGER
            );
            """;

        String sqlUtenti = """
            CREATE TABLE IF NOT EXISTS utenti (
                id TEXT PRIMARY KEY,
                nome TEXT NOT NULL,
                email TEXT NOT NULL,
                codice_fiscale TEXT NOT NULL,
                credito REAL NOT NULL
            );
            """;

        String sqlNoleggi = """
            CREATE TABLE IF NOT EXISTS noleggi (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                utente_id TEXT NOT NULL,
                veicolo_codice TEXT NOT NULL,
                orario_inizio TEXT NOT NULL,
                orario_fine TEXT,
                FOREIGN KEY (utente_id) REFERENCES utenti(id),
                FOREIGN KEY (veicolo_codice) REFERENCES veicoli(codice)
            );
            """;

        try (Connection conn = getConnessione(); Statement stmt = conn.createStatement()) {
            stmt.execute(sqlVeicoli);
            stmt.execute(sqlUtenti);
            stmt.execute(sqlNoleggi);
        } catch (SQLException e) {
            throw new ErroreDatabaseException("Impossibile inizializzare le tabelle del database", e);
        }
    }

    public void salvaVeicolo(Veicolo v) throws ErroreDatabaseException {
        String sql = """
            INSERT INTO veicoli (codice, tipo, livello_batteria, stato, numero_marce, velocita_massima, limitato_zona_pedonale)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT(codice) DO UPDATE SET
                livello_batteria = excluded.livello_batteria,
                stato = excluded.stato,
                numero_marce = excluded.numero_marce,
                velocita_massima = excluded.velocita_massima,
                limitato_zona_pedonale = excluded.limitato_zona_pedonale;
            """;

        try (Connection conn = getConnessione(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, v.getCodice());
            ps.setDouble(3, v.getLivelloBatteria());
            ps.setString(4, v.getStato().name());

            if (v instanceof Bici bici) {
                ps.setString(2, "BICI");
                ps.setInt(5, bici.getNumeroMarce());
                ps.setNull(6, Types.INTEGER);
                ps.setNull(7, Types.INTEGER);
            } else if (v instanceof Monopattino mono) {
                ps.setString(2, "MONOPATTINO");
                ps.setNull(5, Types.INTEGER);
                ps.setInt(6, mono.getVelocitaMassima());
                ps.setInt(7, mono.isLimitatoInZonaPedonale() ? 1 : 0);
            } else {
                throw new ErroreDatabaseException("Tipo di veicolo non riconosciuto");
            }

            ps.executeUpdate();
        } catch (SQLException e) {
            throw new ErroreDatabaseException("Errore durante il salvataggio del veicolo " + v.getCodice(), e);
        }
    }

    public Optional<Veicolo> cercaVeicolo(String codice) throws ErroreDatabaseException {
        String sql = "SELECT * FROM veicoli WHERE codice = ?;";

        try (Connection conn = getConnessione(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, codice);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(costruisciVeicoloDaResultSet(rs));
                }
            }
        } catch (SQLException e) {
            throw new ErroreDatabaseException("Errore durante la ricerca del veicolo " + codice, e);
        }
        return Optional.empty();
    }

    public List<Veicolo> caricaTuttiVeicoli() throws ErroreDatabaseException {
        List<Veicolo> veicoli = new ArrayList<>();
        String sql = "SELECT * FROM veicoli;";

        try (Connection conn = getConnessione(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                veicoli.add(costruisciVeicoloDaResultSet(rs));
            }
        } catch (SQLException e) {
            throw new ErroreDatabaseException("Errore durante il caricamento dei veicoli", e);
        }
        return veicoli;
    }

    private Veicolo costruisciVeicoloDaResultSet(ResultSet rs) throws SQLException {
        String codice = rs.getString("codice");
        String tipo = rs.getString("tipo");
        double batteria = rs.getDouble("livello_batteria");
        StatoVeicolo stato = StatoVeicolo.valueOf(rs.getString("stato"));

        if ("BICI".equalsIgnoreCase(tipo)) {
            int marce = rs.getInt("numero_marce");
            return new Bici(codice, batteria, stato, marce);
        } else {
            int vMax = rs.getInt("velocita_massima");
            boolean limitato = rs.getInt("limitato_zona_pedonale") == 1;
            return new Monopattino(codice, batteria, stato, vMax, limitato);
        }
    }

    public void salvaUtente(Utente u) throws ErroreDatabaseException {
        String sql = """
            INSERT INTO utenti (id, nome, email, codice_fiscale, credito)
            VALUES (?, ?, ?, ?, ?)
            ON CONFLICT(id) DO UPDATE SET
                nome = excluded.nome,
                email = excluded.email,
                codice_fiscale = excluded.codice_fiscale,
                credito = excluded.credito;
            """;

        try (Connection conn = getConnessione(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, u.getId());
            ps.setString(2, u.getNome());
            ps.setString(3, u.getEmail());
            ps.setString(4, u.getCodiceFiscale());
            ps.setDouble(5, u.getCredito());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new ErroreDatabaseException("Errore durante il salvataggio dell'utente " + u.getId(), e);
        }
    }

    public Optional<Utente> cercaUtente(String id) throws ErroreDatabaseException {
        String sql = "SELECT * FROM utenti WHERE id = ?;";

        try (Connection conn = getConnessione(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new Utente(
                        rs.getString("id"),
                        rs.getString("nome"),
                        rs.getString("email"),
                        rs.getString("codice_fiscale"),
                        rs.getDouble("credito")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new ErroreDatabaseException("Errore durante la ricerca dell'utente " + id, e);
        }
        return Optional.empty();
    }

    public List<Utente> caricaTuttiUtenti() throws ErroreDatabaseException {
        List<Utente> utenti = new ArrayList<>();
        String sql = "SELECT * FROM utenti;";

        try (Connection conn = getConnessione(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                utenti.add(new Utente(
                    rs.getString("id"),
                    rs.getString("nome"),
                    rs.getString("email"),
                    rs.getString("codice_fiscale"),
                    rs.getDouble("credito")
                ));
            }
        } catch (SQLException e) {
            throw new ErroreDatabaseException("Errore durante il caricamento degli utenti", e);
        }
        return utenti;
    }

    public void registraInizioNoleggio(Noleggio n) throws ErroreDatabaseException {
        String sql = "INSERT INTO noleggi (utente_id, veicolo_codice, orario_inizio) VALUES (?, ?, ?);";

        try (Connection conn = getConnessione(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, n.getUtente().getId());
            ps.setString(2, n.getVeicolo().getCodice());
            ps.setString(3, n.getOrarioInizio().toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new ErroreDatabaseException("Errore durante la registrazione del noleggio", e);
        }
    }

    public void registraFineNoleggio(String codiceVeicolo, LocalDateTime fine) throws ErroreDatabaseException {
        String sql = """
            UPDATE noleggi
            SET orario_fine = ?
            WHERE veicolo_codice = ? AND orario_fine IS NULL;
            """;

        try (Connection conn = getConnessione(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, fine.toString());
            ps.setString(2, codiceVeicolo);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new ErroreDatabaseException("Errore durante la chiusura del noleggio per " + codiceVeicolo, e);
        }
    }

    public Optional<Noleggio> cercaNoleggioAttivo(String codiceVeicolo, List<Utente> utenti, List<Veicolo> veicoli) throws ErroreDatabaseException {
        String sql = "SELECT * FROM noleggi WHERE veicolo_codice = ? AND orario_fine IS NULL ORDER BY id DESC LIMIT 1;";

        try (Connection conn = getConnessione(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, codiceVeicolo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String utenteId = rs.getString("utente_id");
                    LocalDateTime inizio = LocalDateTime.parse(rs.getString("orario_inizio"));

                    Utente utente = utenti.stream()
                        .filter(u -> u.getId().equals(utenteId))
                        .findFirst()
                        .orElse(null);

                    Veicolo veicolo = veicoli.stream()
                        .filter(v -> v.getCodice().equals(codiceVeicolo))
                        .findFirst()
                        .orElse(null);

                    if (utente != null && veicolo != null) {
                        return Optional.of(new Noleggio(utente, veicolo, inizio, null));
                    }
                }
            }
        } catch (SQLException e) {
            throw new ErroreDatabaseException("Errore durante la ricerca del noleggio attivo per " + codiceVeicolo, e);
        }
        return Optional.empty();
    }
}
