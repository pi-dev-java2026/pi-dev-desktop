package Fintech.servicies;

import Fintech.entities.Reclamation;
import Fintech.utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceReclamation implements IService<Reclamation> {

    private Connection connection;

    public ServiceReclamation() {
        connection = MyDataBase.getInstance().getMyConnection();
    }

    @Override
    public void ajouter(Reclamation reclamation) throws SQLException {
        String sql = "INSERT INTO reclamation (email, subject, description, statut) VALUES (?, ?, ?, ?)";
        PreparedStatement ps = connection.prepareStatement(sql);

        ps.setString(1, reclamation.getEmail());
        ps.setString(2, reclamation.getSubject());
        ps.setString(3, reclamation.getDescription());
        ps.setString(4, reclamation.getStatut());

        ps.executeUpdate();
    }

    private String getPrimaryKey() {
        try {
            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM reclamation WHERE 1=0");
            ResultSetMetaData meta = rs.getMetaData();
            if (meta.getColumnCount() > 0) {
                return meta.getColumnName(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "id-reclamation"; // Fallback based on user error log
    }

    @Override
    public void modifier(Reclamation reclamation) throws SQLException {
        String pk = getPrimaryKey();
        // Quote the column name in case it contains special characters like hyphens
        String sql = "UPDATE reclamation SET email = ?, subject = ?, description = ?, statut = ? WHERE `" + pk
                + "` = ?";
        PreparedStatement ps = connection.prepareStatement(sql);

        ps.setString(1, reclamation.getEmail());
        ps.setString(2, reclamation.getSubject());
        ps.setString(3, reclamation.getDescription());
        ps.setString(4, reclamation.getStatut());
        ps.setInt(5, reclamation.getId_reclamation());

        ps.executeUpdate();
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String pk = getPrimaryKey();
        // Quote the column name in case it contains special characters like hyphens
        String sql = "DELETE FROM reclamation WHERE `" + pk + "` = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    @Override
    public List<Reclamation> afficher() throws SQLException {
        List<Reclamation> reclamations = new ArrayList<>();
        String sql = "SELECT * FROM reclamation";
        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery(sql);

        while (rs.next()) {
            Reclamation r = new Reclamation();
            // Map the first column (usually PK) to id_reclamation
            r.setId_reclamation(rs.getInt(1));
            // We are using email for identification now, so we skip setting the user ID
            // r.setId(rs.getInt("user_id"));
            r.setEmail(rs.getString("email"));
            r.setSubject(rs.getString("subject"));
            r.setDescription(rs.getString("description"));
            r.setStatut(rs.getString("statut"));

            reclamations.add(r);
        }
        return reclamations;
    }
}
