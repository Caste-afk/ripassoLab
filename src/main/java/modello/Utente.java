package modello;

public class Utente {
    private String id;
    private String nome;
    private String email;
    private String codiceFiscale;
    private double credito;

    public Utente(String id, String nome, String email, String codiceFiscale, double credito) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.codiceFiscale = codiceFiscale;
        this.credito = credito;
    }

    public void addebita(double importo) {
        this.credito -= importo;
    }

    public void ricarica(double importo) {
        this.credito += importo;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCodiceFiscale() {
        return codiceFiscale;
    }

    public void setCodiceFiscale(String codiceFiscale) {
        this.codiceFiscale = codiceFiscale;
    }

    public double getCredito() {
        return credito;
    }

    public void setCredito(double credito) {
        this.credito = credito;
    }
}
