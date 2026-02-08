package tn.esprit.services;

import tn.esprit.entities.Abonnement;
import tn.esprit.utils.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AbonnementService implements IService<Abonnement> {
    private Connection connection;

    public AbonnementService() {
        connection = MyConnection.getInstance().getConnection();
    }

    @Override
    public void ajouter(Abonnement abonnement) {
        String req = "INSERT INTO abonnement (nom, prix, date_debut, frequence, categorie, actif) VALUES (?, ?, ?, ?, ?, ?)";
        try {
            PreparedStatement ps = connection.prepareStatement(req, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, abonnement.getNom());
            ps.setDouble(2, abonnement.getPrix());
            ps.setDate(3, abonnement.getDateDebut());
            ps.setString(4, abonnement.getFrequence());
            ps.setString(5, abonnement.getCategorie());
            ps.setBoolean(6, abonnement.isActif());
            ps.executeUpdate();

            // Récupérer l'ID généré
            ResultSet rs = ps.getGeneratedKeys();
            int abonnementId = 0;
            if (rs.next()) {
                abonnementId = rs.getInt(1);
            }

            System.out.println("Abonnement ajouté avec succès !");

            // Générer automatiquement 12 paiements
            genererPaiementsAutomatiques(abonnement, abonnementId, 12);

        } catch (SQLException e) {
            System.out.println("Erreur lors de l'ajout : " + e.getMessage());
        }
    }

    // NOUVELLE MÉTHODE : Génère automatiquement les paiements
    private void genererPaiementsAutomatiques(Abonnement abonnement, int abonnementId, int nombreMois) {
        try {
            Date dateDebut = abonnement.getDateDebut();

            for (int i = 0; i < nombreMois; i++) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(dateDebut);

                // Calculer la date du paiement
                if (abonnement.getFrequence().equals("Mensuel")) {
                    cal.add(Calendar.MONTH, i);
                } else if (abonnement.getFrequence().equals("Annuel")) {
                    cal.add(Calendar.YEAR, i);
                }

                Date datePaiement = new Date(cal.getTimeInMillis());

                // Le premier paiement est "Payé", les autres "À venir"
                String statut = (i == 0) ? "Payé" : "À venir";

                // Insérer le paiement
                String reqPaie = "INSERT INTO paiement (montant, date_paiement, statut, abonnement_id) VALUES (?, ?, ?, ?)";
                PreparedStatement psPaie = connection.prepareStatement(reqPaie);
                psPaie.setDouble(1, abonnement.getPrix());
                psPaie.setDate(2, datePaiement);
                psPaie.setString(3, statut);
                psPaie.setInt(4, abonnementId);
                psPaie.executeUpdate();
            }

            System.out.println("→ " + nombreMois + " paiements générés automatiquement !");

        } catch (SQLException e) {
            System.out.println("Erreur lors de la génération des paiements : " + e.getMessage());
        }
    }

    @Override
    public void modifier(Abonnement abonnement) {
        String req = "UPDATE abonnement SET nom=?, prix=?, date_debut=?, frequence=?, categorie=?, actif=? WHERE id=?";
        try {
            PreparedStatement ps = connection.prepareStatement(req);
            ps.setString(1, abonnement.getNom());
            ps.setDouble(2, abonnement.getPrix());
            ps.setDate(3, abonnement.getDateDebut());
            ps.setString(4, abonnement.getFrequence());
            ps.setString(5, abonnement.getCategorie());
            ps.setBoolean(6, abonnement.isActif());
            ps.setInt(7, abonnement.getId());
            ps.executeUpdate();
            System.out.println("Abonnement modifié avec succès !");
        } catch (SQLException e) {
            System.out.println("Erreur lors de la modification : " + e.getMessage());
        }
    }

    @Override
    public void supprimer(int id) {
        String req = "DELETE FROM abonnement WHERE id=?";
        try {
            PreparedStatement ps = connection.prepareStatement(req);
            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("Abonnement supprimé avec succès !");
        } catch (SQLException e) {
            System.out.println("Erreur lors de la suppression : " + e.getMessage());
        }
    }

    @Override
    public List<Abonnement> afficher() {
        List<Abonnement> abonnements = new ArrayList<>();
        String req = "SELECT * FROM abonnement ORDER BY id ASC";
        try {
            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery(req);
            while (rs.next()) {
                Abonnement a = new Abonnement();
                a.setId(rs.getInt("id"));
                a.setNom(rs.getString("nom"));
                a.setPrix(rs.getDouble("prix"));
                a.setDateDebut(rs.getDate("date_debut"));
                a.setFrequence(rs.getString("frequence"));
                a.setCategorie(rs.getString("categorie"));
                a.setActif(rs.getBoolean("actif"));
                abonnements.add(a);
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de l'affichage : " + e.getMessage());
        }
        return abonnements;
    }

    @Override
    public Abonnement getById(int id) {
        String req = "SELECT * FROM abonnement WHERE id=?";
        try {
            PreparedStatement ps = connection.prepareStatement(req);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Abonnement a = new Abonnement();
                a.setId(rs.getInt("id"));
                a.setNom(rs.getString("nom"));
                a.setPrix(rs.getDouble("prix"));
                a.setDateDebut(rs.getDate("date_debut"));
                a.setFrequence(rs.getString("frequence"));
                a.setCategorie(rs.getString("categorie"));
                a.setActif(rs.getBoolean("actif"));
                return a;
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la recherche : " + e.getMessage());
        }
        return null;
    }
}