package tn.esprit.entities;

import java.sql.Date;

public class Paiement {
    private int id;
    private double montant;
    private Date datePaiement;
    private String statut;
    private int abonnementId;

    public Paiement() {
    }

    public Paiement(double montant, Date datePaiement, String statut, int abonnementId) {
        this.montant = montant;
        this.datePaiement = datePaiement;
        this.statut = statut;
        this.abonnementId = abonnementId;
    }

    public Paiement(int id, double montant, Date datePaiement, String statut, int abonnementId) {
        this.id = id;
        this.montant = montant;
        this.datePaiement = datePaiement;
        this.statut = statut;
        this.abonnementId = abonnementId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getMontant() {
        return montant;
    }

    public void setMontant(double montant) {
        this.montant = montant;
    }

    public Date getDatePaiement() {
        return datePaiement;
    }

    public void setDatePaiement(Date datePaiement) {
        this.datePaiement = datePaiement;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public int getAbonnementId() {
        return abonnementId;
    }

    public void setAbonnementId(int abonnementId) {
        this.abonnementId = abonnementId;
    }

    @Override
    public String toString() {
        return "Paiement{" +
                "id=" + id +
                ", montant=" + montant +
                ", datePaiement=" + datePaiement +
                ", statut='" + statut + '\'' +
                ", abonnementId=" + abonnementId +
                '}';
    }
}