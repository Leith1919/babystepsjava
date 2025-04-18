package tn.esprit.services;

import tn.esprit.entities.Traitement;
import tn.esprit.tools.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;

public class TraitementServices implements iServices<Traitement> {
    Connection cnx;

    public TraitementServices() {
        cnx = MyDataBase.getInstance().getCnx();
    }

    @Override
    public void ajouter(Traitement traitement) throws SQLException {
        String sql = "INSERT INTO traitement (id, ordonnance_id, date_prescription, historique_traitement) VALUES (?, ?, ?, ?)";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, traitement.getId());
        ps.setInt(2, traitement.getOrdonnanceId());
        ps.setDate(3, Date.valueOf(traitement.getDatePrescription()));
        ps.setString(4, traitement.getHistoriqueTraitement());
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
    public void modifier(Traitement t) throws SQLException {
        String sql = "UPDATE traitement SET date_prescription = ?, historique_traitement = ?, ordonnance_id = ? WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(t.getDatePrescription()));
            ps.setString(2, t.getHistoriqueTraitement());
            ps.setInt(3, t.getOrdonnanceId());
            ps.setInt(4, t.getId());

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
    public List<Traitement> recuperer() throws SQLException {
        String sql = "SELECT * FROM traitement";
        Statement ste = cnx.createStatement();
        ResultSet rs = ste.executeQuery(sql);
        List<Traitement> traitements = new ArrayList<>();
        while (rs.next()) {
            int id = rs.getInt("id");
            int ordonnanceId = rs.getInt("ordonnance_id");
            LocalDate datePrescription = rs.getDate("date_prescription").toLocalDate();
            String historiqueTraitement = rs.getString("historique_traitement");
            Traitement t = new Traitement(id, ordonnanceId, datePrescription, historiqueTraitement);
            traitements.add(t);
        }
        return traitements;
    }
}
