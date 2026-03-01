package Fintech.controllers;

import Fintech.entities.User;
import Fintech.servicies.ServiceUser;
import Fintech.utils.UserSession;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

public class UserProfileController {

    @FXML
    private Label profileNameLabel;
    @FXML
    private Label profileRoleLabel;

    @FXML
    private Label userIdLabel;
    @FXML
    private Label userNameLabel;
    @FXML
    private Label userNameDetailLabel;
    @FXML
    private Label userEmailLabel;
    @FXML
    private Label userPhoneLabel;
    @FXML
    private Label userRoleLabel;
    @FXML
    private Label userRoleBadge;

    @FXML
    private Button editButton;
    @FXML
    private Button deleteButton;

    private User currentUser;
    private ServiceUser serviceUser;

    public UserProfileController() {
        serviceUser = new ServiceUser();
    }

    @FXML
    public void initialize() {
        updateUserProfile();
        // Admin check will be done when setUser is called
    }

    private void updateUserProfile() {
        User loggedInUser = UserSession.getInstance().getCurrentUser();
        if (loggedInUser != null) {
            profileNameLabel.setText(loggedInUser.getName());
            profileRoleLabel.setText(loggedInUser.getRole());
        } else {
            profileNameLabel.setText("Utilisateur");
            profileRoleLabel.setText("Non connecté");
        }
    }

    public void setUser(User user) {
        this.currentUser = user;
        displayUserDetails();
        checkAdminPermissions();
    }

    private void displayUserDetails() {
        if (currentUser != null) {
            userIdLabel.setText("#" + currentUser.getId());
            userNameLabel.setText(currentUser.getName());
            userNameDetailLabel.setText(currentUser.getName());
            userEmailLabel.setText(currentUser.getEmail());
            userPhoneLabel.setText(currentUser.getPhone());
            userRoleLabel.setText(currentUser.getRole());

            // Set role badge
            userRoleBadge.setText(currentUser.getRole());
            String badgeStyle = "-fx-background-radius: 15px; -fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 6 18;";

            if (currentUser.getRole().equalsIgnoreCase("Admin")) {
                userRoleBadge.setStyle(badgeStyle + "-fx-background-color: #e3f2fd; -fx-text-fill: #1976d2;");
            } else if (currentUser.getRole().equalsIgnoreCase("Manager")) {
                userRoleBadge.setStyle(badgeStyle + "-fx-background-color: #fff3e0; -fx-text-fill: #f57c00;");
            } else {
                userRoleBadge.setStyle(badgeStyle + "-fx-background-color: #f1f8e9; -fx-text-fill: #689f38;");
            }
        }
    }

    private void checkAdminPermissions() {
        boolean isAdmin = UserSession.getInstance().isAdmin();
        editButton.setDisable(!isAdmin);
        deleteButton.setDisable(!isAdmin);

        if (!isAdmin) {
            editButton.setStyle(editButton.getStyle() + "-fx-opacity: 0.5;");
            deleteButton.setStyle(deleteButton.getStyle() + "-fx-opacity: 0.5;");
        }
    }

    @FXML
    private void handleBack(ActionEvent event) {
        navigateTo(event, "/Fintech/views/UserManagement.fxml");
    }

    @FXML
    private void handleEdit(ActionEvent event) {
        if (!UserSession.getInstance().isAdmin()) {
            showAlert(Alert.AlertType.ERROR, "Accès refusé",
                    "Seuls les administrateurs peuvent modifier les utilisateurs.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fintech/views/UserForm.fxml"));
            Parent root = loader.load();

            UserFormController controller = loader.getController();
            controller.setUser(currentUser);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger le formulaire: " + e.getMessage());
        }
    }

    @FXML
    private void handleDelete(ActionEvent event) {
        if (!UserSession.getInstance().isAdmin()) {
            showAlert(Alert.AlertType.ERROR, "Accès refusé",
                    "Seuls les administrateurs peuvent supprimer les utilisateurs.");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmation");
        confirmAlert.setHeaderText("Supprimer l'utilisateur");
        confirmAlert.setContentText("Êtes-vous sûr de vouloir supprimer " + currentUser.getName() + " ?");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                serviceUser.supprimer(currentUser.getId());
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Utilisateur supprimé avec succès.");
                handleBack(event);
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de supprimer l'utilisateur: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleUsers(ActionEvent event) {
        navigateTo(event, "/Fintech/views/UserManagement.fxml");
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
    private void handleReclamations(ActionEvent event) {
        navigateTo(event, "/Fintech/views/ReclamationManagement.fxml");
    }

    @FXML
    private void handleSettings(ActionEvent event) {
        showAlert(Alert.AlertType.INFORMATION, "Info", "Fonctionnalité Paramètres à venir");
    }

    @FXML
    private void handleLogout(ActionEvent event) {
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
