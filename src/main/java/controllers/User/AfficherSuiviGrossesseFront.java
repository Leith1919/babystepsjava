package controllers.User;

import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.util.Duration;
import org.json.JSONArray;
import models.suiviGrossesse;
import services.User.SuiviGrossesseService;
import utils.UserSession;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.json.JSONObject;

public class AfficherSuiviGrossesseFront implements Initializable {

    @FXML
    private GridPane accountsGridPane;

    // These elements might be null if sidebar is removed
    @FXML
    private VBox sidebar;

    @FXML
    private Button btnComptes;

    @FXML
    private Button btnReclamation;

    @FXML
    private Button btnSignOut;

    // AI Chat Components
    @FXML
    private VBox aiChatSidebar;

    @FXML
    private VBox chatMessagesContainer;

    @FXML
    private ScrollPane chatScrollPane;

    @FXML
    private TextField chatInputField;

    @FXML
    private Button sendMessageButton;

    @FXML
    private TableColumn<suiviGrossesse, Void> colAnalyse;

    private FadeTransition fadeIn;

    private SuiviGrossesseService suiviGrossesseService;

    // Suivi grossesse actuel
    private suiviGrossesse currentSuiviGrossesse;

    // Service pour exécuter les appels API en arrière-plan
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    // ChatGPT API configuration
    private String apiKey;
    private String apiHost;
    private String apiEndpoint;

    // Flag pour débogage - mettre à true pour voir les logs détaillés
    private final boolean DEBUG_MODE = true;

    // Historique de conversation pour le contexte
    private StringBuilder conversationHistory = new StringBuilder();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        suiviGrossesseService = new SuiviGrossesseService();

        // Initialiser l'animation sur accountsGridPane (qui est toujours présent)
        fadeIn = new FadeTransition(Duration.millis(500));
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.setNode(accountsGridPane);

        // Charger les configurations API
        loadApiConfig();

        // Setup button actions safely even if sidebar is removed
        setupButtonActions();

        // Setup chat functionality
        setupChatFunctionality();

        // Display the suivi data for the current user
        afficherSuiviGrossesseByUser();

        // Start the fade in animation
        fadeIn.play();
    }

    private void loadApiConfig() {
        try {
            // Essayez d'abord de charger depuis un fichier de propriétés
            Properties props = new Properties();
            InputStream input = getClass().getClassLoader().getResourceAsStream("api_config.properties");

            if (input != null) {
                props.load(input);
                apiKey = props.getProperty("api.key");
                apiHost = props.getProperty("api.host");
                apiEndpoint = props.getProperty("api.endpoint");
                input.close();
            } else {
                // Si le fichier n'existe pas, utilisez des valeurs par défaut
                apiKey = System.getenv("RAPID_API_KEY");
                if (apiKey == null) {
                    apiKey = "de419d093dmsh8fc319ec7def47ep17412djsn23713c13a41e"; // Votre clé RapidAPI
                }

                // Configuration pour chatgpt-42.p.rapidapi.com
                apiHost = "chatgpt-42.p.rapidapi.com";
                apiEndpoint = "https://chatgpt-42.p.rapidapi.com/chatgpt";

                if (DEBUG_MODE) {
                    System.out.println("API Key: " + apiKey);
                    System.out.println("API Host: " + apiHost);
                    System.out.println("API Endpoint: " + apiEndpoint);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur de configuration",
                    "Impossible de charger la configuration de l'API: " + e.getMessage());
        }
    }

    private void setupButtonActions() {
        // Check if sidebar elements exist before setting up action handlers
        if (btnComptes != null) {
            btnComptes.setOnAction(event -> {
                System.out.println("Navigation vers Comptes");
            });
        }

        if (btnReclamation != null) {
            btnReclamation.setOnAction(event -> {
                System.out.println("Navigation vers Réclamations");
            });
        }

        if (btnSignOut != null) {
            btnSignOut.setOnAction(event -> {
                System.out.println("Déconnexion");
                // Logique de déconnexion ici
                UserSession.logout(); // Clear the user session
                executorService.shutdown(); // Fermer proprement le service d'exécution
            });
        }
    }

    private void setupChatFunctionality() {
        // Configurer l'action d'envoi de message
        sendMessageButton.setOnAction(event -> sendMessage());

        // Permettre l'envoi de message avec la touche Entrée
        chatInputField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                sendMessage();
            }
        });

        // S'assurer que le défilement suit les nouveaux messages
        chatMessagesContainer.heightProperty().addListener((observable, oldValue, newValue) -> {
            chatScrollPane.setVvalue(1.0);
        });
    }

    private void sendMessage() {
        String messageContent = chatInputField.getText().trim();
        if (!messageContent.isEmpty()) {
            // Ajouter le message de l'utilisateur
            addUserMessage(messageContent);

            // Ajouter au contexte de conversation
            conversationHistory.append("User: ").append(messageContent).append("\n");

            // Désactiver le champ et le bouton pendant le traitement
            chatInputField.setDisable(true);
            sendMessageButton.setDisable(true);

            // Afficher un indicateur de chargement
            addLoadingIndicator();

            // Traiter le message en arrière-plan
            executorService.submit(() -> {
                try {
                    // D'abord, essayer de répondre avec les données locales
                    String localResponse = generateLocalResponse(messageContent);

                    if (localResponse != null) {
                        // Si on a une réponse locale pertinente, on l'utilise
                        javafx.application.Platform.runLater(() -> {
                            removeLoadingIndicator();
                            addAIMessage(localResponse);
                            conversationHistory.append("Assistant: ").append(localResponse).append("\n");
                            chatInputField.setDisable(false);
                            sendMessageButton.setDisable(false);
                            chatInputField.clear();
                            chatInputField.requestFocus();
                        });
                    } else {
                        // Sinon, on interroge l'API ChatGPT
                        String aiResponse = callChatGptApi(messageContent);

                        javafx.application.Platform.runLater(() -> {
                            removeLoadingIndicator();
                            addAIMessage(aiResponse);
                            conversationHistory.append("Assistant: ").append(aiResponse).append("\n");
                            chatInputField.setDisable(false);
                            sendMessageButton.setDisable(false);
                            chatInputField.clear();
                            chatInputField.requestFocus();
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    javafx.application.Platform.runLater(() -> {
                        removeLoadingIndicator();
                        addAIMessage("Désolé, j'ai rencontré un problème technique. Veuillez réessayer plus tard.");
                        chatInputField.setDisable(false);
                        sendMessageButton.setDisable(false);
                        chatInputField.clear();
                    });
                }
            });
        }
    }

    private void addLoadingIndicator() {
        HBox loadingContainer = new HBox();
        loadingContainer.setId("loading-indicator");
        loadingContainer.getStyleClass().add("ai-message-container");
        loadingContainer.setAlignment(Pos.CENTER_LEFT);

        Label loadingLabel = new Label("L'assistant réfléchit...");
        loadingLabel.getStyleClass().add("loading-text");

        loadingContainer.getChildren().add(loadingLabel);
        chatMessagesContainer.getChildren().add(loadingContainer);

        // S'assurer que le défilement suit
        chatScrollPane.setVvalue(1.0);
    }

    private void removeLoadingIndicator() {
        // Supprimer l'indicateur de chargement s'il existe
        chatMessagesContainer.getChildren().removeIf(node ->
                node instanceof HBox && "loading-indicator".equals(node.getId()));
    }

    private void addUserMessage(String message) {
        HBox messageContainer = new HBox();
        messageContainer.getStyleClass().add("user-message-container");
        messageContainer.setAlignment(Pos.CENTER_RIGHT);

        VBox messageBox = new VBox();
        messageBox.getStyleClass().add("user-message");

        Label messageText = new Label(message);
        messageText.getStyleClass().add("message-text");
        messageText.setWrapText(true);

        Label timestamp = new Label(getCurrentTime());
        timestamp.getStyleClass().add("message-timestamp");

        messageBox.getChildren().addAll(messageText, timestamp);
        messageContainer.getChildren().add(messageBox);

        chatMessagesContainer.getChildren().add(messageContainer);

        // S'assurer que le défilement suit
        chatScrollPane.setVvalue(1.0);
    }

    private void addAIMessage(String message) {
        HBox messageContainer = new HBox();
        messageContainer.getStyleClass().add("ai-message-container");
        messageContainer.setAlignment(Pos.CENTER_LEFT);

        VBox messageBox = new VBox();
        messageBox.getStyleClass().add("ai-message");

        Label messageText = new Label(message);
        messageText.getStyleClass().add("message-text");
        messageText.setWrapText(true);

        Label timestamp = new Label(getCurrentTime());
        timestamp.getStyleClass().add("message-timestamp");

        messageBox.getChildren().addAll(messageText, timestamp);
        messageContainer.getChildren().add(messageBox);

        chatMessagesContainer.getChildren().add(messageContainer);

        // S'assurer que le défilement suit
        chatScrollPane.setVvalue(1.0);
    }

    private String getCurrentTime() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    private String generateLocalResponse(String userMessage) {
        // Vérifie si le message contient des mots-clés liés aux données locales
        String lowercaseMessage = userMessage.toLowerCase();

        if (currentSuiviGrossesse != null) {
            if (lowercaseMessage.contains("poids")) {
                return "Le poids actuel enregistré est de " + currentSuiviGrossesse.getPoids() + " kg. Un gain de poids modéré est recommandé pendant la grossesse.";
            } else if (lowercaseMessage.contains("tension")) {
                return "La tension artérielle enregistrée est de " + currentSuiviGrossesse.getTension() + ". Une tension stable est essentielle pour une grossesse sans complications.";
            } else if (lowercaseMessage.contains("symptôme") || lowercaseMessage.contains("symptome")) {
                return "Les symptômes notés lors du dernier suivi sont: " + currentSuiviGrossesse.getSymptomes() + ". N'hésitez pas à signaler tout nouveau symptôme à votre médecin.";
            } else if (lowercaseMessage.contains("état") || lowercaseMessage.contains("etat")) {
                return "L'état actuel de la grossesse est: " + currentSuiviGrossesse.getEtatGrossesse() + ".";
            } else if (lowercaseMessage.contains("date") || lowercaseMessage.contains("dernier suivi")) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                return "Votre dernier suivi de grossesse date du " + dateFormat.format(currentSuiviGrossesse.getDateSuivi()) + ".";
            } else if (lowercaseMessage.contains("id") || lowercaseMessage.contains("numéro")) {
                return "Le numéro de référence de votre suivi de grossesse est " + currentSuiviGrossesse.getId() + ".";
            }
        }

        // Pour les messages plus généraux
        if (lowercaseMessage.contains("bonjour") || lowercaseMessage.contains("salut") || lowercaseMessage.contains("hello")) {
            return "Bonjour ! Je suis votre assistant médical de suivi de grossesse. Comment puis-je vous aider aujourd'hui ?";
        } else if (lowercaseMessage.contains("merci")) {
            return "Je vous en prie. N'hésitez pas si vous avez d'autres questions concernant votre grossesse.";
        } else if (lowercaseMessage.contains("au revoir") || lowercaseMessage.contains("bye")) {
            return "Au revoir ! N'hésitez pas à revenir si vous avez d'autres questions concernant votre suivi de grossesse.";
        }

        return null;
    }

    private String callChatGptApi(String userMessage) throws IOException {
        // Use the correct endpoint for the chatgpt-42.p.rapidapi.com service
        URL url = new URL("https://chatgpt-42.p.rapidapi.com/chatgpt");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("X-RapidAPI-Key", apiKey);
        connection.setRequestProperty("X-RapidAPI-Host", "chatgpt-42.p.rapidapi.com");
        connection.setDoOutput(true);

        // Create context based on pregnancy monitoring data
        String context = "";
        if (currentSuiviGrossesse != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            context = "Contexte médical: Suivi de grossesse du " +
                    dateFormat.format(currentSuiviGrossesse.getDateSuivi()) +
                    ", poids: " + currentSuiviGrossesse.getPoids() +
                    "kg, tension: " + currentSuiviGrossesse.getTension() +
                    ", symptômes: " + currentSuiviGrossesse.getSymptomes();
        }

        // Format the request according to the specific API requirements
        // This format is specifically for chatgpt-42.p.rapidapi.com
        JSONObject jsonBody = new JSONObject();
        jsonBody.put("messages", new JSONArray()
                .put(new JSONObject()
                        .put("role", "system")
                        .put("content", "Vous êtes un assistant médical spécialisé en suivi de grossesse. " + context))
                .put(new JSONObject()
                        .put("role", "user")
                        .put("content", userMessage)));

        if (DEBUG_MODE) {
            System.out.println("Request URL: " + url);
            System.out.println("Request headers: " + connection.getRequestProperties());
            System.out.println("Request body: " + jsonBody.toString());
        }

        // Send the request
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonBody.toString().getBytes("UTF-8");
            os.write(input, 0, input.length);
        }

        // Process the response
        int responseCode = connection.getResponseCode();

        // Read the response content
        StringBuilder responseContent = new StringBuilder();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(
                        responseCode >= 200 && responseCode < 300
                                ? connection.getInputStream()
                                : connection.getErrorStream(),
                        "UTF-8"))) {
            String line;
            while ((line = br.readLine()) != null) {
                responseContent.append(line);
            }
        }

        if (DEBUG_MODE) {
            System.out.println("Response code: " + responseCode);
            System.out.println("Response content: " + responseContent.toString());
        }

        // Handle the response based on status code
        if (responseCode >= 200 && responseCode < 300) {
            return extractChatGptResponse(responseContent.toString());
        } else {
            // Alternative response on error
            String errorMsg = "Désolé, je rencontre des difficultés techniques. ";

            // Try to provide more details about the error
            try {
                JSONObject error = new JSONObject(responseContent.toString());
                if (error.has("error") && error.getJSONObject("error").has("message")) {
                    errorMsg += error.getJSONObject("error").getString("message");
                } else if (error.has("message")) {
                    errorMsg += error.getString("message");
                }
            } catch (Exception e) {
                errorMsg += "Erreur " + responseCode + ": " + responseContent.toString();
            }

            // Try fallback method
            try {
                return callChatGptApiFallback(userMessage);
            } catch (Exception e) {
                return errorMsg;
            }
        }
    }

    private String callChatGptApiFallback(String userMessage) throws IOException {
        // Fallback to a simpler endpoint format
        URL url = new URL("https://chatgpt-42.p.rapidapi.com/textchat");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("X-RapidAPI-Key", apiKey);
        connection.setRequestProperty("X-RapidAPI-Host", "chatgpt-42.p.rapidapi.com");
        connection.setDoOutput(true);

        // Simpler request format
        JSONObject jsonBody = new JSONObject();
        jsonBody.put("message", userMessage);

        if (DEBUG_MODE) {
            System.out.println("Fallback Request URL: " + url);
            System.out.println("Fallback Request body: " + jsonBody.toString());
        }

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonBody.toString().getBytes("UTF-8");
            os.write(input, 0, input.length);
        }

        int responseCode = connection.getResponseCode();
        StringBuilder responseContent = new StringBuilder();

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(
                        responseCode >= 200 && responseCode < 300
                                ? connection.getInputStream()
                                : connection.getErrorStream(),
                        "UTF-8"))) {
            String line;
            while ((line = br.readLine()) != null) {
                responseContent.append(line);
            }
        }

        if (responseCode >= 200 && responseCode < 300) {
            return extractChatGptResponse(responseContent.toString());
        } else {
            return "Je suis désolé, le service de chat est temporairement indisponible.";
        }
    }

    private String extractChatGptResponse(String jsonResponse) {
        try {
            if (DEBUG_MODE) {
                System.out.println("Parsing JSON response: " + jsonResponse);
            }

            JSONObject jsonObject = new JSONObject(jsonResponse);

            // Try multiple possible response formats
            if (jsonObject.has("choices") && jsonObject.getJSONArray("choices").length() > 0) {
                // Standard OpenAI format
                JSONObject choice = jsonObject.getJSONArray("choices").getJSONObject(0);
                if (choice.has("message") && choice.getJSONObject("message").has("content")) {
                    return choice.getJSONObject("message").getString("content");
                } else if (choice.has("text")) {
                    return choice.getString("text");
                }
            } else if (jsonObject.has("answer")) {
                return jsonObject.getString("answer");
            } else if (jsonObject.has("response")) {
                return jsonObject.getString("response");
            } else if (jsonObject.has("reply")) {
                return jsonObject.getString("reply");
            } else if (jsonObject.has("content")) {
                return jsonObject.getString("content");
            } else if (jsonObject.has("text")) {
                return jsonObject.getString("text");
            } else if (jsonObject.has("message")) {
                return jsonObject.getString("message");
            } else if (jsonObject.has("result")) {
                return jsonObject.getString("result");
            } else if (jsonObject.has("output")) {
                return jsonObject.getString("output");
            }

            // If we can't find a known pattern
            return "Je n'ai pas pu traiter la réponse du service. Veuillez réessayer.";
        } catch (Exception e) {
            if (DEBUG_MODE) {
                System.err.println("Error parsing JSON response: " + e.getMessage());
                e.printStackTrace();
            }

            // If it's not valid JSON, try to return the raw content
            if (jsonResponse != null && !jsonResponse.isEmpty() &&
                    !jsonResponse.trim().startsWith("{") && !jsonResponse.trim().startsWith("[")) {
                return jsonResponse.trim();
            }

            return "Désolé, je n'ai pas pu interpréter la réponse.";
        }
    }

    // Méthode utilitaire pour supprimer les accents d'une chaîne
    private String removeAccents(String input) {
        if (input == null) {
            return "";
        }

        // Supprimer les caractères accentués
        String normalized = java.text.Normalizer.normalize(input, java.text.Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }

    private String createSystemContext() {
        StringBuilder context = new StringBuilder();
        context.append("Vous êtes un assistant spécialisé en suivi de grossesse. ");

        if (currentSuiviGrossesse != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            context.append("Informations actuelles sur la patiente: ");
            context.append("ID suivi: ").append(currentSuiviGrossesse.getId()).append(", ");
            context.append("Date du dernier suivi: ").append(dateFormat.format(currentSuiviGrossesse.getDateSuivi())).append(", ");
            context.append("Poids: ").append(currentSuiviGrossesse.getPoids()).append(" kg, ");
            context.append("Tension: ").append(currentSuiviGrossesse.getTension()).append(", ");
            context.append("Symptômes: ").append(currentSuiviGrossesse.getSymptomes()).append(", ");
            context.append("État de la grossesse: ").append(currentSuiviGrossesse.getEtatGrossesse()).append(". ");
        }

        context.append("Répondez toujours avec précision, empathie et professionnalisme. ");
        context.append("Vos réponses doivent être utiles, rassurantes et fondées sur des informations médicales fiables. ");
        context.append("Suggérez de consulter un professionnel de santé pour tout problème médical spécifique.");

        return context.toString();
    }

    private String createChatGptRequest(String systemContext, String userMessage) {
        // Structure exacte requise par l'API "chatgpt-42.p.rapidapi.com"
        JSONObject jsonObject = new JSONObject();

        // Format requis pour l'endpoint /aitohuman
        jsonObject.put("inputText", userMessage);
        jsonObject.put("instructionText", systemContext);

        if (DEBUG_MODE) {
            System.out.println("JSON request payload: " + jsonObject.toString());
        }

        return jsonObject.toString();
    }

    @FXML
    public void afficherSuiviGrossesseByUser() {
        // Get the current user from UserSession
        models.User currentUser = UserSession.getCurrentUser();
        if (currentUser == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur de session",
                    "Aucun utilisateur connecté. Veuillez vous connecter pour voir vos suivis de grossesse.");
            return;
        }

        try {
            // Fetch pregnancy follow-up records for the current user
            List<suiviGrossesse> userRecords = suiviGrossesseService.getSuivisByPatientId(currentUser.getId());

            if (!userRecords.isEmpty()) {
                // Use the most recent record (since records are ordered by date_suivi DESC)
                currentSuiviGrossesse = userRecords.get(0);
                accountsGridPane.getChildren().clear();
                setupGridHeaders();
                displayRecord(currentSuiviGrossesse);
            } else {
                showAlert(Alert.AlertType.WARNING, "Aucun suivi trouvé",
                        "Aucun suivi de grossesse trouvé pour votre compte.");
                Label notFoundLabel = new Label("Aucun suivi de grossesse trouvé pour votre compte.");
                notFoundLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: red;");
                accountsGridPane.add(notFoundLabel, 0, 1, 5, 1);
            }

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR,
                    "Erreur de base de données",
                    "Erreur lors de la récupération des données: " + e.getMessage());
        }
    }

    private void setupGridHeaders() {
        String[] headers = { "Date Suivi", "Poids (kg)", "Tension", "Symptômes", "État Grossesse"};
        String headerStyle = "-fx-font-weight: bold; -fx-padding: 10px; -fx-background-color: #f0f0f0;";

        for (int i = 0; i < headers.length; i++) {
            Label headerLabel = new Label(headers[i]);
            headerLabel.setStyle(headerStyle);
            headerLabel.setPrefWidth(Double.MAX_VALUE);
            accountsGridPane.add(headerLabel, i, 0);
        }
    }

    private void displayRecord(suiviGrossesse record) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        String formattedDate = dateFormat.format(record.getDateSuivi());

        Label dateLabel = new Label(formattedDate);
        Label poidsLabel = new Label(String.valueOf(record.getPoids()));
        Label tensionLabel = new Label(String.valueOf(record.getTension()));
        Label symptomesLabel = new Label(record.getSymptomes());
        Label etatLabel = new Label(record.getEtatGrossesse());

        String cellStyle = "-fx-padding: 10px; -fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0;";

        Label[] labels = {dateLabel, poidsLabel, tensionLabel, symptomesLabel, etatLabel};
        for (Label label : labels) {
            label.setStyle(cellStyle);
            label.setPrefWidth(Double.MAX_VALUE);
            label.setWrapText(true);
        }

        accountsGridPane.add(dateLabel, 0, 1);
        accountsGridPane.add(poidsLabel, 1, 1);
        accountsGridPane.add(tensionLabel, 2, 1);
        accountsGridPane.add(symptomesLabel, 3, 1);
        accountsGridPane.add(etatLabel, 4, 1);
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}