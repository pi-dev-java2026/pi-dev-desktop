package Fintech.controllers;

import Fintech.entities.User;
import Fintech.servicies.ServiceUser;
import Fintech.utils.UserSession;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class UserManagementController {

    @FXML
    private ListView<User> usersList;

    @FXML
    private TextField searchField;
    @FXML
    private Label userCountLabel;

    @FXML
    private Label profileNameLabel;
    @FXML
    private Label profileRoleLabel;

    private ServiceUser serviceUser;
    private ObservableList<User> usersObservableList;
    private ObservableList<User> filteredList;

    public UserManagementController() {
        serviceUser = new ServiceUser();
    }

    @FXML
    public void initialize() {
        setupListView();
        loadUsers();
        updateUserProfile();
    }

    private void updateUserProfile() {
        User currentUser = UserSession.getInstance().getCurrentUser();
        if (currentUser != null) {
            profileNameLabel.setText(currentUser.getName());
            profileRoleLabel.setText(currentUser.getRole());
        } else {
            profileNameLabel.setText("Utilisateur");
            profileRoleLabel.setText("Non connecté");
        }
    }

    private void setupListView() {
        usersList.setCellFactory(listView -> new ListCell<User>() {
            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);

                if (empty || user == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    // Create simplified card layout showing only name
                    HBox card = new HBox(20);
                    card.setAlignment(Pos.CENTER_LEFT);
                    card.setPadding(new javafx.geometry.Insets(15, 20, 15, 20));
                    card.setStyle(
                            "-fx-background-color: white; -fx-background-radius: 8px; -fx-border-color: #e0e0e0; -fx-border-radius: 8px; -fx-border-width: 1px;");

                    // User icon
                    Label userIcon = new Label("👤");
                    userIcon.setStyle("-fx-font-size: 24px; -fx-padding: 5;");

                    // Name only
                    Label nameLabel = new Label(user.getName());
                    nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #1f2937;");

                    // Spacer
                    Region spacer = new Region();
                    HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

                    // View Profile button
                    Button viewProfileBtn = new Button("👁️  View Profile");
                    viewProfileBtn.setStyle(
                            "-fx-background-color: #3d5a80; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 8px; -fx-padding: 10px 20px; -fx-font-size: 13px; -fx-font-weight: bold;");

                    viewProfileBtn.setOnMouseEntered(e -> viewProfileBtn.setStyle(
                            "-fx-background-color: #2c4560; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 8px; -fx-padding: 10px 20px; -fx-font-size: 13px; -fx-font-weight: bold;"));

                    viewProfileBtn.setOnMouseExited(e -> viewProfileBtn.setStyle(
                            "-fx-background-color: #3d5a80; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 8px; -fx-padding: 10px 20px; -fx-font-size: 13px; -fx-font-weight: bold;"));

                    viewProfileBtn.setOnAction(e -> handleViewProfile(user));

                    // Add all elements to card
                    card.getChildren().addAll(userIcon, nameLabel, spacer, viewProfileBtn);

                    setGraphic(card);
                    setText(null);
                }
            }
        });
    }

    private void loadUsers() {
        try {
            List<User> users = serviceUser.afficher();
            usersObservableList = FXCollections.observableArrayList(users);
            filteredList = FXCollections.observableArrayList(users);
            usersList.setItems(filteredList);
            updateUserCount();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les utilisateurs: " + e.getMessage());
        }
    }

    private void updateUserCount() {
        userCountLabel.setText(filteredList.size() + " Total");
    }

    @FXML
    private void handleSearch() {
        String searchText = searchField.getText().toLowerCase();

        if (searchText.isEmpty()) {
            filteredList.setAll(usersObservableList);
        } else {
            filteredList.clear();
            for (User user : usersObservableList) {
                boolean match = false;
                if (user.getName() != null && user.getName().toLowerCase().contains(searchText))
                    match = true;
                else if (user.getEmail() != null && user.getEmail().toLowerCase().contains(searchText))
                    match = true;
                else if (user.getPhone() != null && user.getPhone().contains(searchText))
                    match = true;
                else if (user.getRole() != null && user.getRole().toLowerCase().contains(searchText))
                    match = true;

                if (match) {
                    filteredList.add(user);
                }
            }
        }
        updateUserCount();
    }

    @FXML
    private void handleAddUser(ActionEvent event) {
        navigateToUserForm(event, null);
    }

    @FXML
    private void handleAddReclamation(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fintech/views/ReclamationForm.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible de charger le formulaire de réclamation: " + e.getMessage());
        }
    }

    @FXML
    private void handleReclamations(ActionEvent event) {
        navigateTo(event, "/Fintech/views/ReclamationManagement.fxml");
    }

    private void handleViewProfile(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fintech/views/UserProfile.fxml"));
            Parent root = loader.load();

            // Pass the user to the profile controller
            UserProfileController controller = loader.getController();
            controller.setUser(user);

            Stage stage = (Stage) usersList.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger le profil: " + e.getMessage());
        }
    }

    private void navigateToUserForm(ActionEvent event, User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fintech/views/UserForm.fxml"));
            Parent root = loader.load();

            // If editing, pass the user to the form controller
            if (user != null) {
                UserFormController controller = loader.getController();
                controller.setUser(user);
            }

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger le formulaire: " + e.getMessage());
        }
    }

    @FXML
    private void handleDashboard(ActionEvent event) {
        navigateTo(event, "/Fintech/views/MainDashboard.fxml");
    }

    @FXML
    private void handleTransactions(ActionEvent event) {
        showAlert(Alert.AlertType.INFORMATION, "Info", "Fonctionnalité Transactions à venir");
    }

    @FXML
    private void handleBudgets(ActionEvent event) {
        showAlert(Alert.AlertType.INFORMATION, "Info", "Fonctionnalité Budgets à venir");
    }

    @FXML
    private void handleSettings(ActionEvent event) {
        showAlert(Alert.AlertType.INFORMATION, "Info", "Fonctionnalité Paramètres à venir");
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        // Clear the user session
        UserSession.getInstance().clearSession();
        navigateTo(event, "/Fintech/views/login.fxml");
    }

    private void navigateTo(ActionEvent event, String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger la page: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
