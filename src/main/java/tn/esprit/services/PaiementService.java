package tn.esprit.services;

import tn.esprit.entities.Paiement;
import tn.esprit.utils.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PaiementService implements IService<Paiement> {
    private Connection connection;

    public PaiementService() {
        connection = MyConnection.getInstance().getConnection();
    }

    @Override
    public void ajouter(Paiement paiement) {
        String req = "INSERT INTO paiement (montant, date_paiement, statut, abonnement_id) VALUES (?, ?, ?, ?)";
        try {
            PreparedStatement ps = connection.prepareStatement(req);
            ps.setDouble(1, paiement.getMontant());
            ps.setDate(2, paiement.getDatePaiement());
            ps.setString(3, paiement.getStatut());
            ps.setInt(4, paiement.getAbonnementId());
            ps.executeUpdate();
            System.out.println("Paiement ajouté avec succès !");
        } catch (SQLException e) {
            System.out.println("Erreur lors de l'ajout : " + e.getMessage());
        }
    }

    @Override
    public void modifier(Paiement paiement) {
        String req = "UPDATE paiement SET montant=?, date_paiement=?, statut=?, abonnement_id=? WHERE id=?";
        try {
            PreparedStatement ps = connection.prepareStatement(req);
            ps.setDouble(1, paiement.getMontant());
            ps.setDate(2, paiement.getDatePaiement());
            ps.setString(3, paiement.getStatut());
            ps.setInt(4, paiement.getAbonnementId());
            ps.setInt(5, paiement.getId());
            ps.executeUpdate();
            System.out.println("Paiement modifié avec succès !");
        } catch (SQLException e) {
            System.out.println("Erreur lors de la modification : " + e.getMessage());
        }
    }

    @Override
    public void supprimer(int id) {
        String req = "DELETE FROM paiement WHERE id=?";
        try {
            PreparedStatement ps = connection.prepareStatement(req);
            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("Paiement supprimé avec succès !");
        } catch (SQLException e) {
            System.out.println("Erreur lors de la suppression : " + e.getMessage());
        }
    }

    @Override
    public List<Paiement> afficher() {
        List<Paiement> paiements = new ArrayList<>();
        String req = "SELECT * FROM paiement ORDER BY date_paiement ASC";
        try {
            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery(req);
            while (rs.next()) {
                Paiement p = new Paiement();
                p.setId(rs.getInt("id"));
                p.setMontant(rs.getDouble("montant"));
                p.setDatePaiement(rs.getDate("date_paiement"));
                p.setStatut(rs.getString("statut"));
                p.setAbonnementId(rs.getInt("abonnement_id"));
                paiements.add(p);
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de l'affichage : " + e.getMessage());
        }
        return paiements;
    }

    @Override
    public Paiement getById(int id) {
        String req = "SELECT * FROM paiement WHERE id=?";
        try {
            PreparedStatement ps = connection.prepareStatement(req);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Paiement p = new Paiement();
                p.setId(rs.getInt("id"));
                p.setMontant(rs.getDouble("montant"));
                p.setDatePaiement(rs.getDate("date_paiement"));
                p.setStatut(rs.getString("statut"));
                p.setAbonnementId(rs.getInt("abonnement_id"));
                return p;
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la recherche : " + e.getMessage());
        }
        return null;
    }
}