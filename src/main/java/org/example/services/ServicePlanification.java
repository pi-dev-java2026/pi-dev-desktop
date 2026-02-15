package org.example.services;

import org.example.entities.Planification;
import org.example.utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServicePlanification implements IService<Planification> {

    private Connection connection;

    public ServicePlanification() {
        connection = MyDataBase.getInstance().getMyConnection();
    }

    @Override
    public void ajouter(Planification planification) throws SQLException {

        String sql = "INSERT INTO planification (categorie, montant_alloue, priorite, mois) " +
                "VALUES (?, ?, ?, ?)";

        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, planification.getCategorie());
        ps.setDouble(2, planification.getMontantAlloue());
        ps.setString(3, planification.getPriorite());
        ps.setString(4, planification.getMois());

        ps.executeUpdate();
    }

    @Override
    public void modifier(Planification planification) throws SQLException {

        String sql = "UPDATE planification SET categorie=?, montant_alloue=?, priorite=?, mois=? " +
                "WHERE id=?";

        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, planification.getCategorie());
        ps.setDouble(2, planification.getMontantAlloue());
        ps.setString(3, planification.getPriorite());
        ps.setString(4, planification.getMois());
        ps.setInt(5, planification.getId());

        ps.executeUpdate();
    }

    @Override
    public void supprimer(int id) throws SQLException {

        String sql = "DELETE FROM planification WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    @Override
    public List<Planification> afficher() throws SQLException {

        List<Planification> planifications = new ArrayList<>();
        String sql = "SELECT * FROM planification";

        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery(sql);

        while (rs.next()) {

            Planification p = new Planification();
            p.setId(rs.getInt("id"));
            p.setCategorie(rs.getString("categorie"));
            p.setMontantAlloue(rs.getDouble("montant_alloue"));
            p.setPriorite(rs.getString("priorite"));
            p.setMois(rs.getString("mois"));

            planifications.add(p);
        }

        return planifications;
    }
}
