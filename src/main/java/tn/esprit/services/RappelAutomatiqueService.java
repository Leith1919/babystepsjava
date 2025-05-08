package tn.esprit.services;

import tn.esprit.tools.MyDataBase;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class RappelAutomatiqueService {
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final EmailService emailService = new EmailService();
    private final Connection cnx;

    public RappelAutomatiqueService() {
        cnx = MyDataBase.getInstance().getCnx();
    }

    public void demarrerServiceRappels() {
        // Planifier la tâche pour s'exécuter une fois par jour
        // Pour tester, exécutez toutes les minutes au lieu de toutes les 24 heures
        scheduler.scheduleAtFixedRate(this::envoyerRappelsJournaliers, 0, 1, TimeUnit.MINUTES);
    }

    public void arreterService() {
        scheduler.shutdown();
    }

    private void envoyerRappelsJournaliers() {
        System.out.println("Envoi des rappels journaliers démarré...");

        try {
            // Récupérer tous les rendez-vous prévus pour demain
            List<RendezVousAvecPatient> rendezVousDemain = getRendezVousPourDemain();

            // Envoyer un rappel pour chaque rendez-vous
            for (RendezVousAvecPatient rdv : rendezVousDemain) {
                envoyerRappelPourRendezVous(rdv);
            }

            System.out.println("Envoi des rappels terminé. " + rendezVousDemain.size() + " rappels envoyés.");
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'envoi des rappels: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Classe interne pour stocker les informations temporaires du patient
    private static class RendezVousAvecPatient {
        private int id;
        private String heureString;
        private LocalDate jour;
        private String nomMedecin;
        private String prenomMedecin;
        private String nomPatient;
        private String prenomPatient;
        private String emailPatient;

        public RendezVousAvecPatient() {}
    }

    private List<RendezVousAvecPatient> getRendezVousPourDemain() throws SQLException {
        List<RendezVousAvecPatient> rendezVousList = new ArrayList<>();
        LocalDate demain = LocalDate.now().plusDays(1);

        String sql = "SELECT rv.id, rv.heure_string, rv.jour, " +
                "u_medecin.nom AS nom_medecin, u_medecin.prenom AS prenom_medecin, " +
                "u_patient.nom AS nom_patient, u_patient.prenom AS prenom_patient, u_patient.email AS email_patient " +
                "FROM rendez_vous rv " +
                "JOIN user u_medecin ON rv.id_medecin_id = u_medecin.id " +
                "JOIN user u_patient ON rv.patient_id = u_patient.id " +
                "WHERE rv.jour = ?";

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(demain));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    RendezVousAvecPatient rdv = new RendezVousAvecPatient();
                    rdv.id = rs.getInt("id");
                    rdv.heureString = rs.getString("heure_string");
                    rdv.jour = rs.getDate("jour").toLocalDate();
                    rdv.nomMedecin = rs.getString("nom_medecin");
                    rdv.prenomMedecin = rs.getString("prenom_medecin");
                    rdv.nomPatient = rs.getString("nom_patient");
                    rdv.prenomPatient = rs.getString("prenom_patient");
                    rdv.emailPatient = rs.getString("email_patient");

                    rendezVousList.add(rdv);
                }
            }
        }

        return rendezVousList;
    }

    private void envoyerRappelPourRendezVous(RendezVousAvecPatient rdv) {
        try {
            if (rdv.emailPatient != null && !rdv.emailPatient.isEmpty()) {
                // Envoyer l'email de rappel
                boolean succes = emailService.envoyerRappelRendezVous(
                        rdv.emailPatient,
                        rdv.nomPatient + " " + rdv.prenomPatient,
                        rdv.nomMedecin + " " + rdv.prenomMedecin,
                        rdv.jour.toString(),
                        rdv.heureString
                );

                if (succes) {
                    System.out.println("Rappel envoyé pour le rendez-vous ID: " + rdv.id);
                } else {
                    System.err.println("Échec de l'envoi du rappel pour le rendez-vous ID: " + rdv.id);
                }
            } else {
                System.err.println("Impossible d'envoyer le rappel: email du patient manquant pour le rendez-vous ID: " + rdv.id);
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de l'envoi du rappel pour le rendez-vous ID: " + rdv.id + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
    // Dans RappelAutomatiqueService.java, ajoutez:
    public boolean envoyerRappelManuel(int rendezVousId) {
        try {
            String sql = "SELECT rv.id, rv.heure_string, rv.jour, " +
                    "u_medecin.nom AS nom_medecin, u_medecin.prenom AS prenom_medecin, " +
                    "u_patient.nom AS nom_patient, u_patient.prenom AS prenom_patient, u_patient.email AS email_patient " +
                    "FROM rendez_vous rv " +
                    "JOIN user u_medecin ON rv.id_medecin_id = u_medecin.id " +
                    "JOIN user u_patient ON rv.patient_id = u_patient.id " +
                    "WHERE rv.id = ?";

            try (PreparedStatement ps = cnx.prepareStatement(sql)) {
                ps.setInt(1, rendezVousId);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        RendezVousAvecPatient rdv = new RendezVousAvecPatient();
                        rdv.id = rs.getInt("id");
                        rdv.heureString = rs.getString("heure_string");
                        rdv.jour = rs.getDate("jour").toLocalDate();
                        rdv.nomMedecin = rs.getString("nom_medecin");
                        rdv.prenomMedecin = rs.getString("prenom_medecin");
                        rdv.nomPatient = rs.getString("nom_patient");
                        rdv.prenomPatient = rs.getString("prenom_patient");
                        rdv.emailPatient = rs.getString("email_patient");

                        envoyerRappelPourRendezVous(rdv);
                        return true;
                    } else {
                        System.err.println("Rendez-vous non trouvé avec l'ID: " + rendezVousId);
                        return false;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de l'envoi du rappel manuel: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}