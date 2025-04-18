package tn.esprit.services;

import tn.esprit.entites.Disponibilite;
import tn.esprit.entites.RendezVous;
import tn.esprit.entites.User;
import tn.esprit.tools.MyDataBase;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class RendezVousService implements IServices<RendezVous> {
    Connection cnx;
    public RendezVousService() {
        cnx= MyDataBase.getInstance().getCnx();
    }
    @Override
    public void ajouter(RendezVous rv) throws SQLException {
        String sql = "INSERT INTO rendez_vous (heure_r_id, motif, symptomes, traitement_en_cours, notes, statut_rendez_vous, creation, heure_string, jour, id_medecin_id, patient_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        PreparedStatement ps = cnx.prepareStatement(sql);

        // Insérer les valeurs dans la requête préparée
        ps.setInt(1, rv.getDisponibilite().getId());
        ps.setString(2, rv.getMotif());
        ps.setString(3, rv.getSymptomes());
        ps.setString(4, rv.getTraitementEnCours());
        ps.setString(5, rv.getNotes());
        ps.setString(6, rv.getStatutRendezVous());
        ps.setDate(7, Date.valueOf(rv.getCreation()));
        ps.setString(8, rv.getHeureString());
        ps.setDate(9, Date.valueOf(rv.getJour()));
        ps.setInt(10, rv.getMedecin().getId());
        ps.setInt(11, rv.getPatientId());

        try {
            // Exécuter la requête
            ps.executeUpdate();
            System.out.println("Rendez-vous ajouté avec succès !");
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout du rendez-vous: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void supprimerD(RendezVous rendezVous) throws SQLException {
            String sql = "DELETE FROM rendez_vous WHERE id = ?";
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setInt(1, rendezVous.getId());
            ps.executeUpdate();
            System.out.println("Rendez-vous supprimé avec succès !");

    }

    @Override
    public List<RendezVous> afficherDisponibilite() throws SQLException {
        return List.of();
    }


    public List<RendezVous> afficher() throws SQLException {
        List<RendezVous> liste = new ArrayList<>();
        String sql = "SELECT rv.*, u.nom AS nom_medecin " +
                "FROM rendez_vous rv " +
                "JOIN user u ON rv.id_medecin_id = u.id";

        PreparedStatement ps = cnx.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            RendezVous rv = new RendezVous();

            rv.setId(rs.getInt("id"));
            rv.setMotif(rs.getString("motif"));
            rv.setSymptomes(rs.getString("symptomes"));
            rv.setTraitementEnCours(rs.getString("traitement_en_cours"));
            rv.setNotes(rs.getString("notes"));
            rv.setStatutRendezVous(rs.getString("statut_rendez_vous"));
            rv.setCreation(rs.getDate("creation") != null ? rs.getDate("creation").toLocalDate() : null);
            rv.setHeureString(rs.getString("heure_string"));

            // Vérification de la valeur de jour avant la conversion
            java.sql.Date jourSQL = rs.getDate("jour");
            if (jourSQL != null) {
                rv.setJour(jourSQL.toLocalDate());
            } else {
                rv.setJour(null); // Ou une valeur par défaut si nécessaire
            }

            rv.setPatientId(rs.getInt("patient_id"));

            // Médecin
            User medecin = new User();
            medecin.setId(rs.getInt("id_medecin_id"));
            medecin.setNom(rs.getString("nom_medecin"));
            rv.setMedecin(medecin);

            liste.add(rv);
        }

        return liste;
    }


    @Override
    public void modifierD(RendezVous rendezVous) throws SQLException {

            String sql = "UPDATE rendez_vous SET motif = ?, symptomes = ?, traitement_en_cours = ?, notes = ?, statut_rendez_vous = ?, jour = ?, heure_string = ?, id_medecin_id = ?, patient_id = ? WHERE id = ?";

            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setString(1, rendezVous.getMotif());
            ps.setString(2, rendezVous.getSymptomes());
            ps.setString(3, rendezVous.getTraitementEnCours());
            ps.setString(4, rendezVous.getNotes());
            ps.setString(5, rendezVous.getStatutRendezVous());
            ps.setDate(6, Date.valueOf(rendezVous.getJour()));
            ps.setString(7, rendezVous.getHeureString());
            ps.setInt(8, rendezVous.getMedecin().getId());
            ps.setInt(9, rendezVous.getPatientId());
            ps.setInt(10, rendezVous.getId());

            ps.executeUpdate();
            System.out.println("Rendez-vous modifié avec succès !");
    }
}
