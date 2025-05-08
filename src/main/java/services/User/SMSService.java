package services.User;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import javafx.application.Platform;
import javafx.scene.control.Alert;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class SMSService {
    private String twilioPhone;
    private boolean initialized = false;
    private String accountSid;
    private String authToken;

    public SMSService() {
        loadConfig();
    }

    private void loadConfig() {
        Properties props = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("twilio.properties")) {
            if (input == null) {
                System.err.println("Impossible de trouver le fichier twilio.properties");
                return;
            }
            props.load(input);

            accountSid = props.getProperty("twilio.account.sid");
            authToken = props.getProperty("twilio.auth.token");
            twilioPhone = props.getProperty("twilio.phone.number");

            // Pas d'initialisation du client Twilio ici - on le fera à chaque envoi
            initialized = (accountSid != null && authToken != null && twilioPhone != null);
            if (initialized) {
                System.out.println("Configuration Twilio chargée avec succès");
            } else {
                System.err.println("Configuration Twilio incomplète");
            }
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement des configurations Twilio: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Envoyer un SMS en utilisant Twilio
     */
    public void envoyerSMS(String numeroTelephone, String message) {
        System.out.println("Tentative d'envoi de SMS à " + numeroTelephone);
        System.out.println("Status d'initialisation Twilio: " + initialized);

        if (!initialized) {
            System.err.println("Le service SMS n'est pas correctement initialisé.");
            // Réessayez de charger la configuration
            loadConfig();
            if (!initialized) {
                System.err.println("Échec de l'initialisation après réessai.");
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Erreur de configuration");
                    alert.setHeaderText("Service SMS non initialisé");
                    alert.setContentText("Le service SMS n'a pas pu être initialisé correctement. Vérifiez le fichier twilio.properties.");
                    alert.showAndWait();
                });
                return;
            }
        }

        try {
            // Initialiser Twilio à chaque appel, comme dans l'exemple fonctionnel
            Twilio.init(accountSid, authToken);

            // Formatage du numéro pour la Tunisie
            String formattedNumber = formatPhoneNumber(numeroTelephone);
            System.out.println("Numéro formaté: " + formattedNumber);
            System.out.println("Numéro Twilio utilisé: " + twilioPhone);

            // Utiliser exactement la même syntaxe que l'exemple fonctionnel
            Message twilioMessage = Message.creator(
                            new PhoneNumber(formattedNumber),
                            new PhoneNumber(twilioPhone),
                            message)
                    .create();

            System.out.println("SMS envoyé avec l'ID: " + twilioMessage.getSid());

            // Afficher une notification de succès
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Notification SMS");
                alert.setHeaderText("SMS envoyé avec succès");
                alert.setContentText("Le SMS a été envoyé au numéro " + formattedNumber);
                alert.showAndWait();
            });
        } catch (Exception e) {
            System.err.println("Erreur détaillée lors de l'envoi du SMS: " + e.getMessage());
            e.printStackTrace();

            // Afficher une notification d'erreur
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur SMS");
                alert.setHeaderText("Échec de l'envoi du SMS");
                alert.setContentText("Une erreur s'est produite: " + e.getMessage());
                alert.showAndWait();
            });
        }
    }

    private String formatPhoneNumber(String phoneNumber) {
        // Cas spécifique pour les numéros provenant de la base de données (int)
        try {
            int numTel = Integer.parseInt(phoneNumber);
            if (numTel <= 0) {
                System.out.println("Numéro de téléphone invalide (entier <= 0): " + numTel);
                return "+21698765432"; // Numéro de test pour les tests
            }

            // Convertir l'entier en chaîne
            String digitsOnly = String.valueOf(numTel);

            // Si le numéro commence déjà par 216, simplement ajouter le +
            if (digitsOnly.startsWith("216")) {
                return "+" + digitsOnly;
            }

            // Sinon, ajouter le préfixe +216 pour la Tunisie
            return "+216" + digitsOnly;
        }
        catch (NumberFormatException e) {
            // Si ce n'est pas un nombre, traiter comme une chaîne normale
            if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
                return "+21698765432"; // Numéro de test
            }

            // Supprimer tous les caractères non numériques
            String digitsOnly = phoneNumber.replaceAll("[^\\d]", "");

            // Ajouter le préfixe +216
            if (digitsOnly.startsWith("216")) {
                return "+" + digitsOnly;
            } else {
                return "+216" + digitsOnly;
            }
        }
    }
}