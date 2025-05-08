package tn.esprit.utils;

import jakarta.mail.*;
import jakarta.mail.internet.*;

import java.io.File;
import java.util.Properties;

public class MailSender {

    public static void sendMailWithAttachment(String toEmail, String subject, String messageBody, String filePath) throws Exception {
        final String fromEmail = "laajilimouaadh@gmail.com";
        final String password = "ujku duni gcqa ears";

        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, password);
            }
        });

        Message msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(fromEmail));
        msg.setRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
        msg.setSubject(subject);

        // Create message and attachment
        MimeBodyPart messagePart = new MimeBodyPart();
        messagePart.setText(messageBody);

        MimeBodyPart attachmentPart = new MimeBodyPart();
        attachmentPart.attachFile(new File(filePath));

        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(messagePart);
        multipart.addBodyPart(attachmentPart);

        msg.setContent(multipart);
        Transport.send(msg);
    }
}
