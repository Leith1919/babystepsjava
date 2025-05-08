            package controllers.User;

            import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
            import javafx.event.ActionEvent;
            import javafx.fxml.FXML;
            import javafx.fxml.FXMLLoader;
            import javafx.scene.Parent;
            import javafx.scene.Scene;
            import javafx.scene.control.*;
            import javafx.scene.control.Button;
            import javafx.scene.control.TextField;
            import javafx.stage.Stage;
            import models.User;
            import services.User.UserService;
            import javafx.scene.Node;
            import utils.UserSession;
            import utils.GoogleAuth;

            import java.io.IOException;
            import java.sql.SQLException;
            import java.util.logging.Level;
            import java.util.logging.Logger;

            public class login {
                @FXML
                private Button insription;
                @FXML
                public Hyperlink forgetP;

                @FXML
                private Button googleLoginButton;

                @FXML
                private Button login;

                @FXML
                private PasswordField pass;

                @FXML
                private TextField name;

                private final UserService userService = new UserService();
                @FXML
                private Button Home;

                @FXML
                private void loginWithGoogle(ActionEvent event) {
                    try {
                        GoogleIdToken idToken = GoogleAuth.authenticate();

                        if (idToken != null) {
                            GoogleIdToken.Payload payload = idToken.getPayload();
                            String email = payload.getEmail().trim().toLowerCase();
                            String nameGoogle = (String) payload.get("name");

                            User user = userService.findByEmail(email);
                            System.out.println("Utilisateur trouvé ? " + (user != null));

                            if (user == null) {

                                user = new User();
                                user.setNom(nameGoogle);
                                user.setEmail(email);
                                user.setPassword("");
                                user.setRoles("[\"ROLE_USER\"]");

                                try {
                                    userService.ajouterGoogleUser(user);
                                    System.out.println("Utilisateur inscrit via Google : " + nameGoogle);
                                } catch (SQLException ex) {
                                    if (ex.getMessage().contains("Duplicate entry")) {
                                        System.out.println("Email déjà utilisé, connexion utilisateur existant.");
                                        user = userService.findByEmail(email);
                                        if (user == null) {
                                            showAlert("Erreur", "Impossible de récupérer l'utilisateur existant.");
                                            return;
                                        }
                                    } else {
                                        ex.printStackTrace();
                                        showAlert("Erreur", "Erreur lors de l'enregistrement de l'utilisateur.");
                                        return;
                                    }
                                }
                            } else {
                                System.out.println("Utilisateur existant connecté via Google : " + nameGoogle);
                            }

                            // Lancer la session
                            UserSession.setCurrentUser(user);
                            openAfficherUsers(user);
                        } else {
                            showAlert("Erreur", "Échec de l'authentification Google.");
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        showAlert("Erreur", "Une erreur est survenue lors de la connexion avec Google.");
                    }
                }

                @FXML
                void connecter(ActionEvent event) {
                    String nom = name.getText();
                    String mdp = pass.getText();

                    // Attempt to login
                    User user = userService.authenticateUser(nom, mdp);

                    if (user != null) {
                        if (!user.isBanned()) {
                            System.out.println("Login successful");
                            // Proceed with any actions after successful login
                            UserSession.setCurrentUser(user);

                            // Au lieu d'appeler openAfficherUsers, ouvrir directement l'interface Back
                            openBackInterface(user);
                        } else {
                            System.out.println("Login failed: User is banned");
                            showAlert("Login Failed", "Your account has been banned. Please contact the administrator for further assistance.");
                        }
                    } else {
                        System.out.println("Login failed");
                        showAlert("Login Failed", "Incorrect username or password.");
                    }
                }

                // Nouvelle méthode pour ouvrir l'interface Back
                private void openBackInterface(User user) {
                    try {
                        FXMLLoader loader = new FXMLLoader();

                        // Vérifier le rôle de l'utilisateur
                        if (user.getRoles().equals("[\"ROLE_ADMIN\"]")) {
                            // Si l'utilisateur est un ADMIN, charger Back.fxml
                            loader.setLocation(getClass().getResource("/User/Back.fxml"));

                            Parent root = loader.load();

                            // Récupérer le contrôleur Back
                            Back backController = loader.getController();

                            // Charger automatiquement la vue des utilisateurs dans l'interface Back
                            backController.loadView("/User/AfficherUsers.fxml", "Gestion des Utilisateurs");

                            // Créer une nouvelle scène
                            Scene scene = new Scene(root);

                            // Obtenir les informations de l'étape
                            Stage stage = new Stage();
                            stage.setTitle("Administration");
                            stage.setScene(scene);
                            stage.setMaximized(true); // Ouvrir en plein écran

                            // Fermer l'étape actuelle (étape de connexion)
                            Stage currentStage = (Stage) login.getScene().getWindow();
                            currentStage.close();

                            // Afficher la nouvelle étape
                            stage.show();
                        } else {
                            // Si l'utilisateur est un USER, charger index.fxml
                            loader.setLocation(getClass().getResource("/Front/index.fxml"));

                            Parent root = loader.load();

                            // Créer une nouvelle scène
                            Scene scene = new Scene(root);

                            // Obtenir les informations de l'étape
                            Stage stage = new Stage();
                            stage.setTitle("Accueil");
                            stage.setScene(scene);

                            // Fermer l'étape actuelle (étape de connexion)
                            Stage currentStage = (Stage) login.getScene().getWindow();
                            currentStage.close();

                            // Afficher l'étape
                            stage.show();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        showAlert("Error", "Une erreur s'est produite lors de l'ouverture de l'interface d'administration.");
                    }
                }


                    @FXML
                void inscription(ActionEvent event) {
                    try {
                        // Load the FXML file
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/User/inscription.fxml"));
                        Parent root = loader.load();

                        // Create a new scene
                        Scene scene = new Scene(root);

                        // Get the stage information
                        Stage stage = new Stage();
                        stage.setTitle("Inscription Page");
                        stage.setScene(scene);

                        // Show the stage
                        stage.show();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                private void openAfficherUsers(User user) {
                    try {
                        FXMLLoader loader = new FXMLLoader();

                        // Check the role of the user
                        if (user.getRoles().equals("[\"ROLE_ADMIN\"]")) {
                            // If the user is an ADMIN, load AfficherUsers.fxml
                            loader.setLocation(getClass().getResource("/Back.fxml"));
                        } else {
                            // If the user is a USER, load index.fxml
                            loader.setLocation(getClass().getResource("/Front/Front.fxml"));
                        }

                        Parent root = loader.load();

                        // Create a new scene
                        Scene scene = new Scene(root);

                        // Get the stage information
                        Stage stage = new Stage();
                        stage.setTitle("Afficher Users");
                        stage.setScene(scene);

                        // Close the current stage (login stage)
                        Stage currentStage = (Stage) login.getScene().getWindow();
                        currentStage.close();

                        // Show the stage
                        stage.show();
                    } catch (IOException e) {
                        e.printStackTrace();
                        showAlert("Error", "An error occurred while opening the appropriate page.");
                    }
                }

                // Method to show an alert dialog
                private void showAlert(String title, String message) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle(title);
                    alert.setHeaderText(null);
                    alert.setContentText(message);
                    alert.showAndWait();
                }

                @FXML
                private void forgetPassword(ActionEvent event){
                    UserService userService = new UserService();
                    Stage resetPasswordStage = new Stage();
                    Parent resetPasswordInterface;
                    try {
                        resetPasswordInterface = FXMLLoader.load(getClass().getResource("/User/forgetPassword.fxml"));
                        Scene resetPasswordScene = new Scene(resetPasswordInterface);
                        resetPasswordStage.setScene(resetPasswordScene);
                        Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                        currentStage.close();

                        // Show the UserInterface stage
                        resetPasswordStage.show();

                    } catch (IOException ex) {
                        Logger.getLogger(login.class.getName()).log(Level.SEVERE, null, ex);
                    }

                }
                @FXML
                private void home (ActionEvent event) throws IOException {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/front/Home.fxml"));
                    Parent profileInterface = loader.load();
                    Scene profileScene = new Scene(profileInterface);
                    Stage profileStage = new Stage();
                    profileStage.setScene(profileScene);

                    // Close the current stage (assuming verifierButton is accessible from here)
                    Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                    currentStage.close();

                    // Show the login stage
                    profileStage.show();
                }


            }
