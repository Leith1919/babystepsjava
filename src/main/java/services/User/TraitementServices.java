package services.User;

import models.Traitement;
import models.User;
import models.suiviBebe;
import models.suiviGrossesse;
import utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;

public class TraitementServices implements IServices<Traitement> {
    Connection cnx;

    public TraitementServices() {
        cnx = MyDatabase.getInstance().getConnection();
    }

    public List<Integer> getAllOrdonnanceIds() throws SQLException {
        List<Integer> ids = new ArrayList<>();
        String req = "SELECT id FROM ordonnance";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(req);
        while (rs.next()) {
            ids.add(rs.getInt("id"));
        }
        return ids;
    }

    public List<Integer> getAllPatientIds() throws SQLException {
        List<Integer> ids = new ArrayList<>();
        String sql = "SELECT id FROM user WHERE roles LIKE '%ROLE_USER%'";
        Statement stmt = cnx.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        while (rs.next()) {
            ids.add(rs.getInt("id"));
        }
        return ids;
    }


    @Override
    public void ajouter(Traitement traitement) throws SQLException {
        String sql = "INSERT INTO traitement (id, ordonnance_id, patient_id, date_prescription, historique_traitement) VALUES (?, ?, ?, ?, ?)";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, traitement.getId());
        ps.setInt(2, traitement.getOrdonnanceId());
        ps.setInt(3, traitement.getPatientId());
        ps.setDate(4, Date.valueOf(traitement.getDatePrescription()));
        ps.setString(5, traitement.getHistoriqueTraitement());
        ps.executeUpdate();
        System.out.println("Traitement ajouté");
    }

    @Override
    public void supprimer(Traitement traitement) {
        try {
            String sql = "DELETE FROM traitement WHERE id = ?";
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setInt(1, traitement.getId());
            ps.executeUpdate();
            System.out.println("Traitement supprimé");
        } catch (SQLException e) {
            System.out.println("Erreur lors de la suppression : " + e.getMessage());
        }
    }

    @Override
    public void supprimer(int id) throws SQLException {

    }

    @Override
    public void modifier(Traitement t) throws SQLException {
        String sql = "UPDATE traitement SET date_prescription = ?, historique_traitement = ?, ordonnance_id = ?, patient_id = ? WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(t.getDatePrescription()));
            ps.setString(2, t.getHistoriqueTraitement());
            ps.setInt(3, t.getOrdonnanceId());
            ps.setInt(4, t.getPatientId());
            ps.setInt(5, t.getId());

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Traitement modifié");
            } else {
                System.out.println("Aucun traitement trouvé avec cet ID.");
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la modification : " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void modifier(User user) throws SQLException {

    }

    @Override
    public List<Traitement> recuperer() throws SQLException {
        String sql = "SELECT * FROM traitement";
        Statement ste = cnx.createStatement();
        ResultSet rs = ste.executeQuery(sql);
        List<Traitement> traitements = new ArrayList<>();
        while (rs.next()) {
            int id = rs.getInt("id");
            int ordonnanceId = rs.getInt("ordonnance_id");
            int patientId = rs.getInt("patient_id");
            LocalDate datePrescription = rs.getDate("date_prescription").toLocalDate();
            String historiqueTraitement = rs.getString("historique_traitement");

            Traitement t = new Traitement(id, ordonnanceId, datePrescription, historiqueTraitement, patientId);
            traitements.add(t);
        }
        return traitements;
    }

    @Override
    public void inscription(User user) throws SQLException {

    }

    @Override
    public void modifier(suiviGrossesse suivi) throws SQLException {

    }

    @Override
    public void modifier(suiviBebe suivi) throws SQLException {

    }

    @Override
    public void supprimerD(Traitement traitement) throws SQLException {

    }

    @Override
    public List<Traitement> afficherDisponibilite() throws SQLException {
        return List.of();
    }

    @Override
    public void modifierD(Traitement traitement) throws SQLException {

    }
}
