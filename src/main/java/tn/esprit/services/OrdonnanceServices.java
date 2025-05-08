package tn.esprit.services;

import tn.esprit.entities.Ordonnance;
import tn.esprit.tools.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrdonnanceServices implements iServices<Ordonnance> {
    private final Connection cnx;

    public OrdonnanceServices() {
        cnx = MyDatabase.getInstance().getConnection();
    }

    @Override
    public void ajouter(Ordonnance ordonnance) throws SQLException {
        throw new SQLException("Patient ID is required to add an ordonnance.");
    }

    public void ajouter(Ordonnance ordonnance, int patientId) throws SQLException {
        String sql = "INSERT INTO ordonnance (medicament, posologie, date_prescription, patient_id) VALUES (?, ?, ?, ?)";

        try (PreparedStatement pstmt = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, ordonnance.getMedicament());
            pstmt.setString(2, ordonnance.getPosologie());
            pstmt.setString(3, ordonnance.getDatePrescription());
            pstmt.setInt(4, patientId);

            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    ordonnance.setId(rs.getInt(1));
                    ordonnance.setPatientId(patientId);
                    System.out.println("Ordonnance ajoutée avec ID : " + ordonnance.getId());
                }
            }
        }
    }

    public String getPatientNameById(int userId) throws SQLException {
        String sql = "SELECT nom FROM user WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString("nom");
        }
        return null;
    }

    public int getPatientIdByName(String name) throws SQLException {
        String sql = "SELECT id FROM user WHERE nom = ? AND roles LIKE '%ROLE_USER%'";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("id");
        }
        throw new SQLException("Aucun ID trouvé pour le patient : " + name);
    }

    public List<String> getAllPatientNames() throws SQLException {
        List<String> patientNames = new ArrayList<>();
        String sql = "SELECT nom FROM user WHERE roles LIKE '%ROLE_USER%'";  // Correct role for patient

        try (PreparedStatement ps = cnx.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                patientNames.add(rs.getString("nom"));
            }
        }
        return patientNames;
    }

    @Override
    public void supprimer(Ordonnance ordonnance) throws SQLException {
        String sql = "DELETE FROM ordonnance WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, ordonnance.getId());
            ps.executeUpdate();
            System.out.println("Ordonnance supprimée");
        }
    }

    @Override
    public void modifier(Ordonnance ordonnance) throws SQLException {
        int patientId = getPatientIdByName(ordonnance.getPatientName());

        String sql = "UPDATE ordonnance SET medicament = ?, posologie = ?, date_prescription = ?, patient_id = ? WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, ordonnance.getMedicament());
            ps.setString(2, ordonnance.getPosologie());
            ps.setString(3, ordonnance.getDatePrescription());
            ps.setInt(4, patientId);
            ps.setInt(5, ordonnance.getId());

            ps.executeUpdate();
            System.out.println("Ordonnance modifiée");
        }
    }

    @Override
    public List<Ordonnance> recuperer() throws SQLException {
        List<Ordonnance> ordonnances = new ArrayList<>();
        String sql = "SELECT * FROM ordonnance";
        try (Statement ste = cnx.createStatement();
             ResultSet rs = ste.executeQuery(sql)) {
            while (rs.next()) {
                int patientId = rs.getInt("patient_id");
                String patientName = getPatientNameById(patientId);

                ordonnances.add(new Ordonnance(
                        rs.getInt("id"),
                        rs.getString("medicament"),
                        rs.getString("posologie"),
                        rs.getString("date_prescription"),
                        patientName
                ));
            }
        }
        return ordonnances;
    }

    public List<Ordonnance> getAllOrdonnances() throws SQLException {
        return recuperer();
    }
}
