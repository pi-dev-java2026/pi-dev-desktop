package org.example.entities;

import java.sql.Date;

public class Activite {
    private int id;
    private String description;
    private double montant;
    private Date dateActivite;
    private String statut;

    public Activite() {}

    public Activite(int id, String description, double montant, Date dateActivite, String statut) {
        this.id = id;
        this.description = description;
        this.montant = montant;
        this.dateActivite = dateActivite;
        this.statut = statut;
    }

    public Activite(String description, double montant, Date dateActivite, String statut) {
        this.description = description;
        this.montant = montant;
        this.dateActivite = dateActivite;
        this.statut = statut;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getMontant() { return montant; }
    public void setMontant(double montant) { this.montant = montant; }

    public Date getDateActivite() { return dateActivite; }
    public void setDateActivite(Date dateActivite) { this.dateActivite = dateActivite; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    @Override
    public String toString() {
        return "Activite{" +
                "id=" + id +
                ", description='" + description + '\'' +
                ", montant=" + montant +
                ", dateActivite=" + dateActivite +
                ", statut='" + statut + '\'' +
                '}';
    }
}
