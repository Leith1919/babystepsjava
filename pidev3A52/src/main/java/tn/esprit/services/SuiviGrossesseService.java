package tn.esprit.services;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import tn.esprit.entities.suiviBebe;
import tn.esprit.entities.suiviGrossesse;
import tn.esprit.tools.MyDatabase;

public class SuiviGrossesseService implements IServices<suiviGrossesse> {
    private final Connection cnx;

    public SuiviGrossesseService() {
        cnx = MyDatabase.getInstance().getCnx();
    }

    @Override
    public void ajouter(suiviGrossesse suivi) throws SQLException {
        String sql = "INSERT INTO suivi_grossesse(date_suivi, poids, tension, symptomes, etat_grossesse) VALUES (?, ?, ?, ?, ?)";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setDate(1, new java.sql.Date(suivi.getDateSuivi().getTime()));
        ps.setDouble(2, suivi.getPoids());
        ps.setDouble(3, suivi.getTension());
        ps.setString(4, suivi.getSymptomes());
        ps.setString(5, suivi.getEtatGrossesse());
        ps.executeUpdate();
        System.out.println("Suivi de grossesse ajouté avec succès.");
    }

    @Override
    public void supprimer(suiviGrossesse suivi) throws SQLException {
        String sql = "DELETE FROM suivi_grossesse WHERE id = ?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, suivi.getId());
        ps.executeUpdate();
        System.out.println("Suivi de grossesse supprimé avec succès.");
    }

    @Override
    public List<suiviGrossesse> recuperer() throws SQLException {
        List<suiviGrossesse> suivis = new ArrayList<>();
        String sql = "SELECT * FROM suivi_grossesse";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            suiviGrossesse suivi = new suiviGrossesse(
                    rs.getInt("id"),
                    rs.getDate("date_suivi"),
                    rs.getDouble("poids"),
                    rs.getDouble("tension"),
                    rs.getString("symptomes"),
                    rs.getString("etat_grossesse")
            );
            suivis.add(suivi);
        }
        return suivis;
    }

    @Override
    public void modifier(suiviBebe suivi) throws SQLException {

    }

    public void modifier(suiviGrossesse suivi) throws SQLException {
        String sql = "UPDATE suivi_grossesse SET date_suivi = ?, poids = ?, tension = ?, symptomes = ?, etat_grossesse = ? WHERE id = ?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setDate(1, new java.sql.Date(suivi.getDateSuivi().getTime()));
        ps.setDouble(2, suivi.getPoids());
        ps.setDouble(3, suivi.getTension());
        ps.setString(4, suivi.getSymptomes());
        ps.setString(5, suivi.getEtatGrossesse());
        ps.setInt(6, suivi.getId());
        ps.executeUpdate();
        System.out.println("Suivi de grossesse mis à jour avec succès.");
    }
}
