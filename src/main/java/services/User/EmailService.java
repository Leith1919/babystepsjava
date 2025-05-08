package services.User;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Properties;

import models.suiviGrossesse;
import models.User;

public class EmailService {

    // Constantes pour la configuration de l'email
    private static final String SMTP_HOST = "smtp.gmail.com";  // Remplacer par votre serveur SMTP
    private static final String SMTP_PORT = "587";  // Port SMTP standard
    private static final String EMAIL_FROM = "mahaallani123@gmail.com";  // Remplacer par votre email
    private static final String EMAIL_PASSWORD = "qwah gfgc xbig xujy";  // Remplacer par votre mot de passe ou mot de passe d'application

    /**
     * Envoie un email de notification de nouveau suivi de grossesse à la patiente
     *
     * @param patient        L'objet utilisateur contenant les informations de la patiente
     * @param suiviGrossesse L'objet contenant les détails du suivi de grossesse
     * @return boolean indiquant si l'envoi a réussi
     */
    public boolean envoyerEmailNouveauSuivi(User patient, suiviGrossesse suiviGrossesse) {
        if (patient == null || suiviGrossesse == null) {
            System.err.println("Patient ou suivi de grossesse est null");
            return false;
        }

        String emailDestinataire = patient.getEmail();
        if (emailDestinataire == null || emailDestinataire.isEmpty()) {
            System.err.println("L'adresse email du patient est vide ou invalide");
            return false;
        }

        try {
            // Configuration des propriétés pour la session email
            Properties properties = new Properties();
            properties.put("mail.smtp.auth", "true");
            properties.put("mail.smtp.starttls.enable", "true");
            properties.put("mail.smtp.host", SMTP_HOST);
            properties.put("mail.smtp.port", SMTP_PORT);

            // Créer une session avec authentification
            Session session = Session.getInstance(properties, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(EMAIL_FROM, EMAIL_PASSWORD);
                }
            });

            // Créer le message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(EMAIL_FROM));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(emailDestinataire));
            message.setSubject("Nouveau suivi de grossesse enregistré");

            // Formater le contenu de l'email
            String emailContent = construireContenuEmail(patient, suiviGrossesse);
            message.setContent(emailContent, "text/html; charset=utf-8");

            // Envoyer le message
            Transport.send(message);
            System.out.println("Email envoyé avec succès à " + emailDestinataire);
            return true;

        } catch (MessagingException e) {
            e.printStackTrace();
            System.err.println("Erreur lors de l'envoi de l'email: " + e.getMessage());
            return false;
        }
    }

    /**
     * Construit le contenu HTML de l'email avec les informations du suivi
     *
     * @param patient        L'objet utilisateur contenant les informations de la patiente
     * @param suiviGrossesse L'objet contenant les détails du suivi de grossesse
     * @return String contenant le HTML formaté de l'email
     */
    private String construireContenuEmail(User patient, suiviGrossesse suiviGrossesse) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        String dateStr = suiviGrossesse.getDateSuivi() != null ?
                dateFormat.format(suiviGrossesse.getDateSuivi()) : "Non spécifiée";

        return "<!DOCTYPE html>"
                + "<html>"
                + "<head>"
                + "<meta charset='utf-8'>"
                + "<style>"
                + "body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }"
                + ".container { width: 80%; margin: 0 auto; padding: 20px; }"
                + ".header { background-color: #f8d7da; color: #721c24; padding: 10px; text-align: center; border-radius: 5px; }"
                + ".content { margin-top: 20px; }"
                + "table { width: 100%; border-collapse: collapse; margin-top: 20px; }"
                + "table, th, td { border: 1px solid #ddd; }"
                + "th, td { padding: 12px; text-align: left; }"
                + "th { background-color: #f8d7da; color: #721c24; }"
                + ".footer { margin-top: 30px; font-size: 12px; color: #777; text-align: center; }"
                + "</style>"
                + "</head>"
                + "<body>"
                + "<div class='container'>"
                + "<div class='header'>"
                + "<h2>Nouveau suivi de grossesse enregistré</h2>"
                + "</div>"
                + "<div class='content'>"
                + "<p>Bonjour " + patient.getPrenom() + " " + patient.getNom() + ",</p>"
                + "<p>Nous vous informons qu'un nouveau suivi de votre grossesse a été enregistré dans notre système. Voici les détails :</p>"
                + "<table>"
                + "<tr><th>Information</th><th>Détail</th></tr>"
                + "<tr><td>Date du suivi</td><td>" + dateStr + "</td></tr>"
                + "<tr><td>Poids</td><td>" + suiviGrossesse.getPoids() + " kg</td></tr>"
                + "<tr><td>Tension</td><td>" + suiviGrossesse.getTension() + "</td></tr>"
                + "<tr><td>Symptômes</td><td>" + suiviGrossesse.getSymptomes() + "</td></tr>"
                + "<tr><td>État de la grossesse</td><td>" + suiviGrossesse.getEtatGrossesse() + "</td></tr>"
                + "</table>"
                + "<p>N'hésitez pas à nous contacter si vous avez des questions ou des préoccupations concernant votre suivi.</p>"
                + "<p>Bien cordialement,</p>"
                + "<p>L'équipe médicale</p>"
                + "</div>"
                + "<div class='footer'>"
                + "<p>Cet email est généré automatiquement. Merci de ne pas y répondre.</p>"
                + "</div>"
                + "</div>"
                + "</body>"
                + "</html>";
    }
}