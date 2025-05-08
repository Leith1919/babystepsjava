package services.User;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import javafx.application.Platform;
import javafx.scene.control.Alert;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class RappelEmailService {
    private Session session;
    private String senderEmail;
    private boolean initialized = false;

    public RappelEmailService() {
        loadConfig();
    }

    private void loadConfig() {
        Properties emailProps = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("email.properties")) {
            if (input == null) {
                System.err.println("Impossible de trouver le fichier email.properties");
                return;
            }
            emailProps.load(input);

            senderEmail = emailProps.getProperty("email.address");
            String password = emailProps.getProperty("email.password");
            String host = emailProps.getProperty("email.smtp.host");
            String port = emailProps.getProperty("email.smtp.port");

            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", host);
            props.put("mail.smtp.port", port);

            session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(senderEmail, password);
                }
            });

            initialized = true;
            System.out.println("Configuration Email chargée avec succès");
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement des configurations Email: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public boolean envoyerRappelRendezVous(String destinataire, String nomPatient, String nomMedecin,
                                           String dateRendezVous, String heureRendezVous) {
        if (!initialized) {
            System.err.println("Le service Email n'est pas correctement initialisé.");
            return false;
        }

        try {
            // Créer le message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(senderEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinataire));
            message.setSubject("Rappel de votre rendez-vous médical");

            // Contenu HTML simplifié
            String emailContent =
                    "<!DOCTYPE html>" +
                            "<html>" +
                            "<head>" +
                            "    <style>" +
                            "        body { font-family: Arial, sans-serif; line-height: 1.6; }" +
                            "        .container { width: 80%; margin: 0 auto; padding: 20px; }" +
                            "        .header { background-color: #f8f9fa; padding: 20px; text-align: center; }" +
                            "        .content { padding: 20px; }" +
                            "        .footer { font-size: 12px; text-align: center; margin-top: 30px; color: #777; }" +
                            "    </style>" +
                            "</head>" +
                            "<body>" +
                            "    <div class='container'>" +
                            "        <div class='header'>" +
                            "            <h2>Rappel de Rendez-vous</h2>" +
                            "        </div>" +
                            "        <div class='content'>" +
                            "            <p>Bonjour " + nomPatient + ",</p>" +
                            "            <p>Nous vous rappelons que vous avez un rendez-vous avec le Dr. " + nomMedecin + " demain, " +
                            dateRendezVous + " à " + heureRendezVous + ".</p>" +
                            "            <p>Merci de bien vouloir vous présenter 10 minutes avant l'heure de votre rendez-vous.</p>" +
                            "            <p>Si vous avez des questions ou si vous ne pouvez pas venir, merci de nous contacter par téléphone.</p>" +
                            "        </div>" +
                            "        <div class='footer'>" +
                            "            <p>Ce message a été envoyé automatiquement, merci de ne pas y répondre directement.</p>" +
                            "        </div>" +
                            "    </div>" +
                            "</body>" +
                            "</html>";

            message.setContent(emailContent, "text/html; charset=utf-8");

            // Envoyer le message
            Transport.send(message);

            System.out.println("Email de rappel envoyé avec succès à " + destinataire);
            return true;

        } catch (MessagingException e) {
            System.err.println("Erreur lors de l'envoi de l'email: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}