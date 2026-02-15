package Fintech.entities;

public class Reclamation {

    private int id_reclamation;
    private String email;
    private String subject;
    private String description;
    private String statut;

    public Reclamation() {
    }

    public Reclamation(int id_reclamation,String email, String subject, String description, String statut) {
        this.id_reclamation = id_reclamation;
        this.email = email;
        this.subject = subject;
        this.description = description;
        this.statut = statut;
    }

    public Reclamation( String email, String subject, String description, String statut) {
        this.email = email;
        this.subject = subject;
        this.description = description;
        this.statut = statut;
    }

    public int getId_reclamation() {
        return id_reclamation;
    }

    public void setId_reclamation(int id_reclamation) {
        this.id_reclamation = id_reclamation;
    }



    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    @Override
    public String toString() {
        return "Reclamation{" +
                "id_reclamation=" + id_reclamation +
                ", email='" + email + '\'' +
                ", subject='" + subject + '\'' +
                ", description='" + description + '\'' +
                ", statut='" + statut + '\'' +
                '}';
    }
}
