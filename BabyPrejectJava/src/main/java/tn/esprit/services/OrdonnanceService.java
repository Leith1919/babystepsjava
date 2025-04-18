package tn.esprit.services;

import tn.esprit.entities.Ordonnance;
import tn.esprit.tools.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrdonnanceService implements iServices<Ordonnance> {
    private Connection cnx;

    // Constructor to initialize the connection
    public OrdonnanceService() {
        cnx = MyDataBase.getInstance().getCnx(); // Assuming MyDataBase is a singleton class managing DB connection
    }

    // Method to add a new Ordonnance
    @Override
    public void ajouter(Ordonnance ordonnance) throws SQLException {
        String sql = "INSERT INTO ordonnance (medicament, posologie, date_prescription) VALUES (?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, ordonnance.getMedicament());
            ps.setString(2, ordonnance.getPosologie());
            ps.setString(3, ordonnance.getDatePrescription());
            ps.executeUpdate();

            // Retrieve the generated ID
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int generatedId = rs.getInt(1);
                    ordonnance.setId(generatedId);
                    System.out.println("Ordonnance ajoutée avec ID : " + generatedId);
                } else {
                    System.out.println("Échec de la récupération de l'ID généré");
                }
            }
        }
    }

    // Method to delete an Ordonnance
    @Override
    public void supprimer(Ordonnance ordonnance) throws SQLException {
        String sql = "DELETE FROM ordonnance WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, ordonnance.getId());
            ps.executeUpdate();
            System.out.println("Ordonnance supprimée");
        } catch (SQLException e) {
            System.out.println("Erreur lors de la suppression : " + e.getMessage());
        }
    }

    // Method to update an Ordonnance
    @Override
    public void modifier(Ordonnance ordonnance) throws SQLException {
        String sql = "UPDATE ordonnance SET medicament = ?, posologie = ?, date_prescription = ? WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, ordonnance.getMedicament());
            ps.setString(2, ordonnance.getPosologie());
            ps.setString(3, ordonnance.getDatePrescription());
            ps.setInt(4, ordonnance.getId());
            ps.executeUpdate();
            System.out.println("Ordonnance modifiée avec succès !");
        }
    }

    // Method to retrieve all Ordonnances from the database
    @Override
    public List<Ordonnance> recuperer() throws SQLException {
        String sql = "SELECT * FROM ordonnance";
        List<Ordonnance> ordonnances = new ArrayList<>();
        try (Statement ste = cnx.createStatement();
             ResultSet rs = ste.executeQuery(sql)) {
            while (rs.next()) {
                int id = rs.getInt("id");
                String medicament = rs.getString("medicament");
                String posologie = rs.getString("posologie");
                String datePrescription = rs.getString("date_prescription");
                Ordonnance o = new Ordonnance(id, medicament, posologie, datePrescription);
                ordonnances.add(o);
            }
        }
        return ordonnances;
    }

    // Method to retrieve all Ordonnances (same as recuperer, used for clarity)
    public List<Ordonnance> getAllOrdonnances() throws SQLException {
        return recuperer(); // Simply call recuperer() since it's already doing the job
    }
}
