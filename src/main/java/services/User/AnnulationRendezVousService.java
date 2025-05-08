package services.User;

import models.RendezVous;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class AnnulationRendezVousService {

    private RendezVousService rendezVousService;

    public AnnulationRendezVousService() {
        this.rendezVousService = new RendezVousService();
    }

    /**
     * Vérifie si un rendez-vous peut être annulé (plus de 24h avant)
     */
    public boolean peutEtreAnnule(RendezVous rdv) {
        if (rdv == null || rdv.getJour() == null || rdv.getHeureString() == null) {
            return false;
        }

        // Vérifier si la date est déjà passée
        if (rdv.getJour().isBefore(LocalDate.now())) {
            return false;
        }

        try {
            // Extraire l'heure de début à partir du format "HH-HH" (ex: "9-11" ou "14-16")
            String heureString = rdv.getHeureString();

            // Vérifier si le format contient un tiret (ex: "9-11" or "14-16")
            if (heureString.contains("-")) {
                // Extraire la première partie (heure de début)
                String heureDebut = heureString.split("-")[0].trim();

                // Convertir en format "HH:00"
                int heure = Integer.parseInt(heureDebut);
                LocalTime time = LocalTime.of(heure, 0);

                // Date et heure du rendez-vous
                LocalDateTime dateHeureRdv = LocalDateTime.of(rdv.getJour(), time);

                // Date et heure actuelles
                LocalDateTime maintenant = LocalDateTime.now();

                // Calculer le nombre d'heures restantes
                long heuresRestantes = ChronoUnit.HOURS.between(maintenant, dateHeureRdv);

                // Le rendez-vous peut être annulé s'il reste plus de 24h
                return heuresRestantes >= 24;
            } else {
                // Essayer le format standard "HH:mm" si pas de tiret
                LocalTime heureRdv = LocalTime.parse(heureString, DateTimeFormatter.ofPattern("HH:mm"));

                // Date et heure du rendez-vous
                LocalDateTime dateHeureRdv = LocalDateTime.of(rdv.getJour(), heureRdv);

                // Date et heure actuelles
                LocalDateTime maintenant = LocalDateTime.now();

                // Calculer le nombre d'heures restantes
                long heuresRestantes = ChronoUnit.HOURS.between(maintenant, dateHeureRdv);

                // Le rendez-vous peut être annulé s'il reste plus de 24h
                return heuresRestantes >= 24;
            }

        } catch (Exception e) {
            System.err.println("Erreur lors de la vérification du délai: " + e.getMessage());
            // Par défaut, on considère que le rendez-vous ne peut pas être annulé en cas d'erreur
            return false;
        }
    }

    /**
     * Classe pour représenter le résultat d'une annulation
     */
    public enum ResultatAnnulation {
        SUCCES("Rendez-vous annulé avec succès."),
        DELAI_DEPASSE("Impossible d'annuler : le rendez-vous est prévu dans moins de 24 heures."),
        RENDEZ_VOUS_PASSE("Impossible d'annuler : ce rendez-vous est déjà passé.");

        private String message;

        ResultatAnnulation(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }

    /**
     * Annule un rendez-vous si possible
     */
    public ResultatAnnulation annulerRendezVous(int rendezVousId, int patientId) {
        try {
            // Récupérer le rendez-vous
            RendezVous rdv = rendezVousService.getRendezVousById(rendezVousId);

            if (rdv == null) {
                return ResultatAnnulation.DELAI_DEPASSE;
            }

            // Vérifier si le rendez-vous peut être annulé
            if (!peutEtreAnnule(rdv)) {
                if (rdv.getJour().isBefore(LocalDate.now())) {
                    return ResultatAnnulation.RENDEZ_VOUS_PASSE;
                } else {
                    return ResultatAnnulation.DELAI_DEPASSE;
                }
            }

            // Supprimer le rendez-vous
            rendezVousService.supprimerD(rdv);

            // Notifier le médecin par SMS (si disponible)
            notifierMedecin(rdv);

            return ResultatAnnulation.SUCCES;

        } catch (Exception e) {
            System.err.println("Erreur lors de l'annulation: " + e.getMessage());
            e.printStackTrace();
            return ResultatAnnulation.DELAI_DEPASSE; // Par défaut, en cas d'erreur
        }
    }

    /**
     * Envoie un SMS au médecin
     */
    private void notifierMedecin(RendezVous rdv) {
        try {
            if (rdv.getMedecin() != null && rdv.getMedecin().getNumtel() > 0) {
                String numTel = String.valueOf(rdv.getMedecin().getNumtel());
                String message = "ANNULATION: Le rendez-vous du " +
                        rdv.getJour().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) +
                        " à " + rdv.getHeureString() + " a été annulé.";

                SMSService smsService = new SMSService();
                smsService.envoyerSMS(numTel, message);
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de l'envoi du SMS: " + e.getMessage());
        }
    }
}