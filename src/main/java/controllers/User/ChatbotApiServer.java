package controllers.User;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.json.JSONArray;
import org.json.JSONObject;
import models.suiviGrossesse;
import services.User.SuiviGrossesseService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * Service API pour le chatbot de suivi de grossesse
 * Cette classe démarre un serveur HTTP qui expose une API REST
 * pour interagir avec le chatbot de suivi de grossesse
 */
public class ChatbotApiServer {

    // Port sur lequel le serveur va écouter
    private static final int PORT = 8080;

    // Clés API autorisées (dans un vrai système, stockez cela dans une base de données sécurisée)
    private static final Map<String, String> API_KEYS = new HashMap<>();

    // Instance du service de suivi de grossesse
    private final SuiviGrossesseService suiviGrossesseService;

    // Instance du serveur HTTP
    private HttpServer server;

    // Historique des conversations par utilisateur (clé API)
    private final Map<String, JSONArray> conversationHistories = new HashMap<>();

    /**
     * Constructeur du serveur API
     */
    public ChatbotApiServer() {
        this.suiviGrossesseService = new SuiviGrossesseService();

        // Générons une clé API par défaut
        String defaultApiKey = generateApiKey("admin");
        System.out.println("Clé API générée par défaut: " + defaultApiKey);
    }

    /**
     * Démarre le serveur API
     */
    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(PORT), 0);

        // Configuration des endpoints
        server.createContext("/api/chat", new ChatHandler());
        server.createContext("/api/generateKey", new ApiKeyHandler());
        server.createContext("/api/suiviGrossesse", new SuiviGrossesseHandler());

        // Utilisation d'un pool de threads pour gérer les requêtes
        server.setExecutor(Executors.newFixedThreadPool(10));
        server.start();

        System.out.println("Serveur API démarré sur le port " + PORT);
    }

    /**
     * Arrête le serveur API
     */
    public void stop() {
        if (server != null) {
            server.stop(0);
            System.out.println("Serveur API arrêté");
        }
    }

    /**
     * Génère une nouvelle clé API pour un utilisateur
     */
    private String generateApiKey(String username) {
        String apiKey = UUID.randomUUID().toString();
        API_KEYS.put(apiKey, username);
        return apiKey;
    }

    /**
     * Vérifie si une clé API est valide
     */
    private boolean isValidApiKey(String apiKey) {
        return API_KEYS.containsKey(apiKey);
    }

    /**
     * Gestionnaire pour les requêtes de chat
     */
    class ChatHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // Vérification de la méthode HTTP
            if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                sendResponse(exchange, 405, "Méthode non autorisée");
                return;
            }

            // Vérification de la clé API
            String apiKey = exchange.getRequestHeaders().getFirst("X-API-Key");
            if (apiKey == null || !isValidApiKey(apiKey)) {
                sendResponse(exchange, 401, "Clé API non valide ou manquante");
                return;
            }

            // Lecture du corps de la requête
            String requestBody = new BufferedReader(new InputStreamReader(exchange.getRequestBody()))
                    .lines().collect(Collectors.joining("\n"));

            try {
                JSONObject request = new JSONObject(requestBody);
                String message = request.getString("message");
                int suiviGrossesseId = request.optInt("suiviGrossesseId", -1);

                // Recherche du suivi de grossesse si un ID est fourni
                suiviGrossesse currentSuiviGrossesse = null;
                if (suiviGrossesseId != -1) {
                    try {
                        List<suiviGrossesse> allRecords = suiviGrossesseService.recuperer();
                        for (suiviGrossesse record : allRecords) {
                            if (record.getId() == suiviGrossesseId) {
                                currentSuiviGrossesse = record;
                                break;
                            }
                        }
                    } catch (SQLException e) {
                        sendResponse(exchange, 500, "Erreur lors de la récupération du suivi de grossesse: " + e.getMessage());
                        return;
                    }
                }

                // Génération de la réponse du chatbot
                String chatResponse = generateAIResponse(message, currentSuiviGrossesse);

                // Ajout à l'historique de conversation
                if (!conversationHistories.containsKey(apiKey)) {
                    conversationHistories.put(apiKey, new JSONArray());
                }
                JSONArray history = conversationHistories.get(apiKey);
                JSONObject conversation = new JSONObject();
                conversation.put("user", message);
                conversation.put("bot", chatResponse);
                history.put(conversation);

                // Préparation de la réponse
                JSONObject response = new JSONObject();
                response.put("response", chatResponse);

                sendResponse(exchange, 200, response.toString());

            } catch (Exception e) {
                sendResponse(exchange, 400, "Format de requête invalide: " + e.getMessage());
            }
        }
    }

    /**
     * Gestionnaire pour la génération de clés API
     */
    class ApiKeyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // Vérification de la méthode HTTP
            if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                sendResponse(exchange, 405, "Méthode non autorisée");
                return;
            }

            // Lecture du corps de la requête
            String requestBody = new BufferedReader(new InputStreamReader(exchange.getRequestBody()))
                    .lines().collect(Collectors.joining("\n"));

            try {
                JSONObject request = new JSONObject(requestBody);
                String username = request.getString("username");
                String masterKey = request.getString("masterKey");

                // Vérification de la clé maître (à remplacer par votre propre système d'authentification)
                if (!"master_secret_key".equals(masterKey)) {
                    sendResponse(exchange, 403, "Clé maître invalide");
                    return;
                }

                // Génération d'une nouvelle clé API
                String apiKey = generateApiKey(username);

                // Préparation de la réponse
                JSONObject response = new JSONObject();
                response.put("apiKey", apiKey);
                response.put("username", username);

                sendResponse(exchange, 200, response.toString());

            } catch (Exception e) {
                sendResponse(exchange, 400, "Format de requête invalide: " + e.getMessage());
            }
        }
    }

    /**
     * Gestionnaire pour les requêtes concernant les suivis de grossesse
     */
    class SuiviGrossesseHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // Vérification de la clé API
            String apiKey = exchange.getRequestHeaders().getFirst("X-API-Key");
            if (apiKey == null || !isValidApiKey(apiKey)) {
                sendResponse(exchange, 401, "Clé API non valide ou manquante");
                return;
            }

            // Traitement selon la méthode HTTP
            String method = exchange.getRequestMethod();
            switch (method) {
                case "GET":
                    handleGetSuiviGrossesse(exchange);
                    break;
                default:
                    sendResponse(exchange, 405, "Méthode non autorisée");
            }
        }

        private void handleGetSuiviGrossesse(HttpExchange exchange) throws IOException {
            try {
                // Récupération de tous les suivis de grossesse
                List<suiviGrossesse> suiviGrossesseList = suiviGrossesseService.recuperer();

                // Conversion en JSON
                JSONArray jsonArray = new JSONArray();
                for (suiviGrossesse suivi : suiviGrossesseList) {
                    JSONObject jsonSuivi = new JSONObject();
                    jsonSuivi.put("id", suivi.getId());
                    jsonSuivi.put("dateSuivi", suivi.getDateSuivi().toString());
                    jsonSuivi.put("poids", suivi.getPoids());
                    jsonSuivi.put("tension", suivi.getTension());
                    jsonSuivi.put("symptomes", suivi.getSymptomes());
                    jsonSuivi.put("etatGrossesse", suivi.getEtatGrossesse());
                    jsonArray.put(jsonSuivi);
                }

                // Envoi de la réponse
                sendResponse(exchange, 200, jsonArray.toString());

            } catch (SQLException e) {
                sendResponse(exchange, 500, "Erreur lors de la récupération des suivis de grossesse: " + e.getMessage());
            }
        }
    }

    /**
     * Méthode utilitaire pour envoyer une réponse HTTP
     */
    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, response.getBytes().length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }

    /**
     * Génère une réponse IA basée sur le message utilisateur et les données de suivi de grossesse
     */
    private String generateAIResponse(String userMessage, suiviGrossesse currentSuiviGrossesse) {
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
            } else if (lowercaseMessage.contains("conseil") || lowercaseMessage.contains("recommandation")) {
                return "Pour une grossesse saine, je vous recommande de maintenir une alimentation équilibrée, faire de l'exercice modéré, prendre vos suppléments de vitamines et respecter vos rendez-vous médicaux réguliers.";
            } else if (lowercaseMessage.contains("alimentation") || lowercaseMessage.contains("manger")) {
                return "Une alimentation équilibrée est essentielle pendant la grossesse. Privilégiez les fruits, légumes, protéines maigres et produits laitiers. Évitez l'alcool, limitez la caféine et assurez-vous que vos repas sont bien cuits pour éviter les risques de toxoplasmose.";
            } else if (lowercaseMessage.contains("exercice") || lowercaseMessage.contains("sport")) {
                return "L'exercice modéré est bénéfique pendant la grossesse. La marche, la natation et le yoga prénatal sont recommandés. Évitez les sports à risque de chute ou de traumatisme abdominal.";
            }
        }

        if (lowercaseMessage.contains("bonjour") || lowercaseMessage.contains("salut")) {
            return "Bonjour ! Je suis votre assistant médical. Comment puis-je vous aider aujourd'hui concernant votre suivi de grossesse ?";
        } else if (lowercaseMessage.contains("merci")) {
            return "Je vous en prie. N'hésitez pas si vous avez d'autres questions.";
        } else if (lowercaseMessage.contains("aide") || lowercaseMessage.contains("aider")) {
            return "Je peux vous aider avec des informations sur votre suivi de grossesse, des conseils de santé prénatale, ou répondre à vos questions sur les symptômes courants. Que souhaitez-vous savoir ?";
        } else if (lowercaseMessage.contains("trimestre")) {
            return "La grossesse est divisée en trois trimestres, chacun avec ses particularités. Le premier trimestre (semaines 1-12) est marqué par des changements hormonaux, des nausées et de la fatigue. Le deuxième trimestre (semaines 13-26) est souvent plus confortable avec les premiers mouvements du bébé. Le troisième trimestre (semaines 27-40) est la période de croissance intense du bébé et de préparation à l'accouchement.";
        } else if (lowercaseMessage.contains("échographie") || lowercaseMessage.contains("echographie")) {
            return "Les échographies sont des examens importants pendant la grossesse. La première échographie (12 semaines) confirme l'âge gestationnel, l'échographie morphologique (22 semaines) étudie l'anatomie du bébé, et la dernière échographie (32 semaines) vérifie la croissance et la position du bébé.";
        } else if (lowercaseMessage.contains("accouchement")) {
            return "L'accouchement est un processus qui comprend plusieurs étapes : la dilatation du col de l'utérus, l'expulsion du bébé et la délivrance du placenta. Préparez-vous en suivant des cours de préparation à l'accouchement et en établissant un plan de naissance avec votre équipe médicale.";
        }

        return "Je suis désolé, je n'ai pas compris votre question. Pourriez-vous reformuler ou me demander des informations spécifiques sur votre suivi de grossesse ?";
    }

    /**
     * Point d'entrée principal pour démarrer le serveur API
     */
    public static void main(String[] args) {
        try {
            ChatbotApiServer server = new ChatbotApiServer();
            server.start();

            // Ajoutez une méthode de fermeture propre (par exemple à l'aide d'un hook d'arrêt)
            Runtime.getRuntime().addShutdownHook(new Thread(server::stop));

        } catch (IOException e) {
            System.err.println("Erreur lors du démarrage du serveur API: " + e.getMessage());
            e.printStackTrace();
        }
    }
}