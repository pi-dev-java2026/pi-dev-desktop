package org.example.services;

import org.example.entities.Activite;
import org.example.utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceActivite implements IService<Activite> {

    private final Connection connection;

    public ServiceActivite() {
        connection = MyDataBase.getInstance().getMyConnection();
    }

    @Override
    public void ajouter(Activite a) throws SQLException {
        String sql = "INSERT INTO activite (description, montant, date_activite, statut) VALUES (?, ?, ?, ?)";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, a.getDescription());
        ps.setDouble(2, a.getMontant());
        ps.setDate(3, a.getDateActivite());
        ps.setString(4, a.getStatut());
        ps.executeUpdate();
    }

    @Override
    public void modifier(Activite a) throws SQLException {
        String sql = "UPDATE activite SET description=?, montant=?, date_activite=?, statut=? WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, a.getDescription());
        ps.setDouble(2, a.getMontant());
        ps.setDate(3, a.getDateActivite());
        ps.setString(4, a.getStatut());
        ps.setInt(5, a.getId());
        ps.executeUpdate();
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM activite WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    @Override
    public List<Activite> afficher() throws SQLException {
        List<Activite> list = new ArrayList<>();
        String sql = "SELECT * FROM activite";
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            Activite a = new Activite();
            a.setId(rs.getInt("id"));
            a.setDescription(rs.getString("description"));
            a.setMontant(rs.getDouble("montant"));
            a.setDateActivite(rs.getDate("date_activite"));
            a.setStatut(rs.getString("statut"));
            list.add(a);
        }
        return list;
    }
}
