package services.User;

import models.*;
import utils.MyDatabase;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class RendezVousService implements IServices<RendezVous> {
    Connection cnx;
    public RendezVousService() {
        cnx = MyDatabase.getInstance().getConnection();
    }

    @Override
    public void ajouter(RendezVous rv) throws SQLException {
        String sql = "INSERT INTO rendez_vous (heure_r_id, motif, symptomes, traitement_en_cours, notes, statut_rendez_vous, creation, heure_string, jour, id_medecin_id, patient_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        PreparedStatement ps = cnx.prepareStatement(sql);

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
            ps.executeUpdate();
            System.out.println("Rendez-vous ajouté avec succès !");
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout du rendez-vous: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void modifier(RendezVous rendezVous) throws SQLException {
    }

    @Override
    public void modifier(User user) throws SQLException {
    }

    @Override
    public void supprimer(RendezVous rendezVous) throws SQLException {
    }

    @Override
    public void supprimer(int id) throws SQLException {
    }

    @Override
    public List<RendezVous> recuperer() throws SQLException {
        return List.of();
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

            java.sql.Date jourSQL = rs.getDate("jour");
            if (jourSQL != null) {
                rv.setJour(jourSQL.toLocalDate());
            } else {
                rv.setJour(null);
            }

            rv.setPatientId(rs.getInt("patient_id"));

            User medecin = new User();
            medecin.setId(rs.getInt("id_medecin_id"));
            medecin.setNom(rs.getString("nom_medecin"));
            rv.setMedecin(medecin);

            liste.add(rv);
        }

        return liste;
    }

    public List<RendezVous> afficherParPatient(int patientId) throws SQLException {
        List<RendezVous> liste = new ArrayList<>();
        String sql = "SELECT rv.*, u.nom AS nom_medecin " +
                "FROM rendez_vous rv " +
                "JOIN user u ON rv.id_medecin_id = u.id " +
                "WHERE rv.patient_id = ?";

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, patientId);
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

            java.sql.Date jourSQL = rs.getDate("jour");
            if (jourSQL != null) {
                rv.setJour(jourSQL.toLocalDate());
            } else {
                rv.setJour(null);
            }

            rv.setPatientId(rs.getInt("patient_id"));

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
        RendezVous ancienRendezVous = getRendezVousById(rendezVous.getId());
        boolean heureModifiee = !ancienRendezVous.getHeureString().equals(rendezVous.getHeureString());
        System.out.println("Ancienne heure: " + ancienRendezVous.getHeureString());
        System.out.println("Nouvelle heure: " + rendezVous.getHeureString());
        System.out.println("Heure modifiée: " + heureModifiee);
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

        if (heureModifiee) {
            System.out.println("Tentative d'envoi de SMS...");
            notifierModificationHeure(rendezVous, ancienRendezVous);
        } else {
            System.out.println("Pas de modification d'heure détectée, SMS non envoyé");
        }
    }

    private void notifierModificationHeure(RendezVous nouveauRendezVous, RendezVous ancienRendezVous) {
        try {
            System.out.println("Début de la notification par SMS");

            UserService userService = new UserService();
            User medecin = userService.getUserById(nouveauRendezVous.getMedecin().getId());

            int numTel = medecin.getNumtel();
            System.out.println("Numéro de téléphone du médecin (int): " + numTel);

            String numTelString = String.valueOf(numTel);

            User patient = userService.getUserById(nouveauRendezVous.getPatientId());

            String message = "Modification de rendez-vous: Le RDV avec " +
                    patient.getNom() + " " + patient.getPrenom() +
                    " a été déplacé de " + ancienRendezVous.getHeureString() +
                    " à " + nouveauRendezVous.getHeureString() +
                    " le " + nouveauRendezVous.getJour() +
                    ". Motif: " + nouveauRendezVous.getMotif();

            System.out.println("Message à envoyer: " + message);

            SMSService smsService = new SMSService();
            smsService.envoyerSMS(numTelString, message);

        } catch (Exception e) {
            System.err.println("Erreur générale lors de la notification SMS: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public RendezVous getRendezVousById(int id) throws SQLException {
        String sql = "SELECT rv.*, u.nom as nom_medecin, u.prenom as prenom_medecin, " +
                "u.email as email_medecin, u.roles as roles_medecin, u.numtel as numtel_medecin, " +
                "u.nationnalite as nationnalite_medecin, u.is_banned as is_banned_medecin, " +
                "u.is_verified as is_verified_medecin " +
                "FROM rendez_vous rv " +
                "JOIN user u ON rv.id_medecin_id = u.id " +
                "WHERE rv.id = ?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    RendezVous rdv = new RendezVous();
                    rdv.setId(rs.getInt("id"));
                    rdv.setMotif(rs.getString("motif"));
                    rdv.setSymptomes(rs.getString("symptomes"));
                    rdv.setTraitementEnCours(rs.getString("traitement_en_cours"));
                    rdv.setNotes(rs.getString("notes"));
                    rdv.setStatutRendezVous(rs.getString("statut_rendez_vous"));

                    if (rs.getDate("jour") != null) {
                        rdv.setJour(rs.getDate("jour").toLocalDate());
                    }

                    rdv.setHeureString(rs.getString("heure_string"));

                    if (rs.getDate("creation") != null) {
                        rdv.setCreation(rs.getDate("creation").toLocalDate());
                    }

                    User medecin = new User();
                    medecin.setId(rs.getInt("id_medecin_id"));
                    medecin.setNom(rs.getString("nom_medecin"));
                    medecin.setPrenom(rs.getString("prenom_medecin"));
                    medecin.setEmail(rs.getString("email_medecin"));
                    medecin.setRoles(rs.getString("roles_medecin"));

                    int numtel = rs.getInt("numtel_medecin");
                    medecin.setNumtel(numtel);

                    System.out.println("Médecin récupéré: " + medecin.getNom() + " " + medecin.getPrenom() + ", numtel: " + numtel);

                    medecin.setNationnalite(rs.getString("nationnalite_medecin"));

                    if (rs.getObject("is_banned_medecin") != null) {
                        medecin.setBanned(rs.getBoolean("is_banned_medecin"));
                    }

                    if (rs.getObject("is_verified_medecin") != null) {
                        medecin.setVerified(rs.getBoolean("is_verified_medecin"));
                    }

                    rdv.setMedecin(medecin);
                    rdv.setPatientId(rs.getInt("patient_id"));

                    DisponibiliteService disponibiliteService = new DisponibiliteService();
                    try {
                        if (rs.getObject("heure_r_id") != null) {
                            Disponibilite disponibilite = disponibiliteService.getDisponibiliteById(rs.getInt("heure_r_id"));
                            rdv.setDisponibilite(disponibilite);
                        }
                    } catch (SQLException e) {
                        System.err.println("Erreur lors de la récupération de la disponibilité: " + e.getMessage());
                    }

                    return rdv;
                }
                throw new SQLException("Aucun rendez-vous trouvé avec l'ID: " + id);
            }
        }
    }
}