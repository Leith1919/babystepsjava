package controllers.User;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import models.User;
import services.User.UserService;
import test.HelloApplication;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class AfficherUsers {

    public Button btnModifier;
    @FXML
    private TextField searchField;

    @FXML
    private TableView<User> tableView;

    @FXML
    private AnchorPane anchorPane; // Assuming the TableView is wrapped inside an AnchorPane

    @FXML
    private TableColumn<User, Integer> idCol;

    @FXML
    private TableColumn<User, String> emailCol;

    @FXML
    private TableColumn<User, String> nomCol;

    @FXML
    private TableColumn<User, String> prenomCol;

    @FXML
    private TableColumn<User, String> nationnalieCol;

    @FXML
    private TableColumn<User, Integer> numtelCol;

    @FXML
    private TableColumn<User, Boolean> banCol;

    @FXML
    private Button auth;

    @FXML
    private RadioButton ban;

    @FXML
    private Pagination pagination;
    private ObservableList<User> usersList;
    private final int rowsPerPage = 5;
    private UserService userService;

    @FXML
    void auth(ActionEvent event) {
        System.out.println("btn");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/User/login.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void supprimer() {
        User selectedUser = tableView.getSelectionModel().getSelectedItem();
        if (selectedUser != null) {
            try {
                userService.supprimer(selectedUser.getId());
                loadUsers(); // refresh the table view
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            System.err.println("Error: No user selected.");
        }
    }

    @FXML
    private void banUser() {
        User selectedUser = tableView.getSelectionModel().getSelectedItem();
        if (selectedUser != null) {
            try {
                userService.toggleUserBanStatus(selectedUser.getId(), true);
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Ban ");
                alert.setContentText("Ban ");
                alert.showAndWait();
                loadUsers(); // refresh the table view
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            System.err.println("Error: No user selected.");
        }
    }

    private void loadUsers() {
        try {
            List<User> users = userService.recuperer();
            usersList = FXCollections.observableArrayList(users);
            pagination.setPageCount((int) Math.ceil((double) usersList.size() / rowsPerPage));

            pagination.setPageFactory(this::createPage);

            idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
            emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
            nomCol.setCellValueFactory(new PropertyValueFactory<>("nom"));
            prenomCol.setCellValueFactory(new PropertyValueFactory<>("prenom"));
            nationnalieCol.setCellValueFactory(new PropertyValueFactory<>("nationnalite"));
            numtelCol.setCellValueFactory(new PropertyValueFactory<>("numtel"));
            banCol.setCellValueFactory(new PropertyValueFactory<>("banned")); // Set cell value factory for "Banned" column

            tableView.setItems(usersList);
        } catch (SQLException e) {
            System.err.println("Error loading users: " + e.getMessage());
        }
    }


    private void setupSearch() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterUsers(newValue);
        });
    }

    private void filterUsers(String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            // Show all users if search is empty
            tableView.setItems(usersList);
        } else {
            ObservableList<User> filteredList = FXCollections.observableArrayList();
            String lowerCaseSearchText = searchText.toLowerCase();

            for (User user : usersList) {
                if (user.getNom().toLowerCase().contains(lowerCaseSearchText) ||
                        user.getPrenom().toLowerCase().contains(lowerCaseSearchText) ||
                        user.getEmail().toLowerCase().contains(lowerCaseSearchText) ||
                        user.getRoles().toLowerCase().contains(lowerCaseSearchText)) {
                    filteredList.add(user);
                }
            }

            tableView.setItems(filteredList);
        }
    }


    private Node createPage(int pageIndex) {
        int fromIndex = pageIndex * rowsPerPage;
        int toIndex = Math.min(fromIndex + rowsPerPage, usersList.size());

        tableView.setItems(FXCollections.observableArrayList(usersList.subList(fromIndex, toIndex)));

        // Wrap the TableView inside a ScrollPane
        ScrollPane scrollPane = new ScrollPane(tableView);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        scrollPane.setMinWidth(850); // Set minimum width
        scrollPane.setPrefWidth(850); // Set preferred width

        scrollPane.setMinHeight(400); // Set minimum height
        scrollPane.setPrefHeight(400); // Set preferred height

        return scrollPane;
    }


    @FXML
    void PageModifier(ActionEvent event) {
        // Get the selected user
        User selectedUser = tableView.getSelectionModel().getSelectedItem();
        if (selectedUser != null) {
            // Load the ModifierUser.fxml file
            FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("/User/ModifierUser.fxml"));
            try {
                // Load the root node
                Parent root = fxmlLoader.load();
                // Get the controller instance
                ModifierUser modifierUserController = fxmlLoader.getController();
                // Pass the selected user to the controller
                modifierUserController.setSelectedUser(selectedUser);
                // Set the scene with the modified root
                tableView.getScene().setRoot(root);
            } catch (IOException e) {
                System.err.println(e.getMessage());
                throw new RuntimeException(e);
            }
        } else {
            System.err.println("Error: No user selected.");
        }
    }


    @FXML
    void ReturnToAjouter(ActionEvent event) {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("/User/AjouterUser.fxml"));
        try {
            tableView.getScene().setRoot(fxmlLoader.load());
        } catch (IOException e) {
            System.err.println(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @FXML
    void initialize() {
        userService = new UserService();
        loadUsers();
        tableView.setPrefSize(850, 400);

        setupSearch(); // <-- NEW: setup the search behavior


    }

    @FXML
    void modifierSelectedUser(ActionEvent event) {
        User selectedUser = tableView.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Aucun utilisateur sélectionné");
            alert.setHeaderText(null);
            alert.setContentText("Veuillez sélectionner un utilisateur à modifier.");
            alert.showAndWait();
            return;
        }

        try {
            // Load the Back layout
            FXMLLoader backLoader = new FXMLLoader(getClass().getResource("/back.fxml"));
            Parent root = backLoader.load();
            Back backController = backLoader.getController();

            // Load ModifierUser.fxml and set the selected user
            FXMLLoader modifierLoader = new FXMLLoader(getClass().getResource("/User/ModifierUser.fxml"));
            Parent modifierRoot = modifierLoader.load();
            ModifierUser modifierController = modifierLoader.getController();
            modifierController.setSelectedUser(selectedUser);

            // Pass the pre-loaded modifierRoot to the Back controller
            backController.loadViewWithRoot(modifierRoot, "Modifier Utilisateur");

            // Set the scene root to the Back layout
            btnModifier.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Impossible d'ouvrir la page de modification");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }


}
