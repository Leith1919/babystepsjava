package tn.esprit.services;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;

public class EmailService {

    private String username = "Essghir.Malek@esprit.tn"; // Remplace par ton email
    private String password = "Malekessghir22"; // Remplace par ton mot de passe

    // Méthode pour envoyer l'email
    public void sendEmail(String subject, String body) {
        // Configuration des propriétés de connexion au serveur SMTP (ex. Gmail)
        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");

        // Authentification avec le serveur
        Session session = Session.getInstance(properties, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            // Créer un message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse("admin@example.com")); // L'email de l'administrateur
            message.setSubject(subject);
            message.setText(body);

            // Envoyer le message
            Transport.send(message);
            System.out.println("Email envoyé avec succès.");
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }
}
