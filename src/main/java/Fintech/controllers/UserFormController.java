package Fintech.controllers;

import Fintech.entities.User;
import Fintech.servicies.ServiceUser;
import Fintech.utils.UserSession;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class UserFormController implements Initializable {

    @FXML
    private TextField nameField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField phoneField;
    @FXML
    private TextField passwordField;
    @FXML
    private ComboBox<String> roleComboBox;

    private ServiceUser serviceUser = new ServiceUser();
    private User user;
    private Runnable updateCallback;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        roleComboBox.setItems(FXCollections.observableArrayList("Admin", "User", "Manager")); // Example roles
    }

    public void setUser(User user) {
        this.user = user;
        if (user != null) {
            nameField.setText(user.getName());
            emailField.setText(user.getEmail());
            phoneField.setText(user.getPhone());
            passwordField.setText(user.getPassword());
            roleComboBox.setValue(user.getRole());
        }
    }

    public void setUpdateCallback(Runnable updateCallback) {
        this.updateCallback = updateCallback;
    }

    @FXML
    private void handleSave(ActionEvent event) {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String password = passwordField.getText();
        String role = roleComboBox.getValue();

        // Validation des champs vides
        if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty() || role == null) {
            showAlert("Erreur", "Veuillez remplir tous les champs.", Alert.AlertType.ERROR);
            return;
        }

        // Validation du nom (seulement des lettres et espaces)
        if (!isValidName(name)) {
            showAlert("Erreur de validation", "Le nom doit contenir uniquement des lettres et des espaces.",
                    Alert.AlertType.ERROR);
            return;
        }

        // Validation de l'email (doit contenir @)
        if (!isValidEmail(email)) {
            showAlert("Erreur de validation", "L'email doit contenir le caractère '@'.", Alert.AlertType.ERROR);
            return;
        }

        // Validation du téléphone (seulement des chiffres)
        if (!isValidPhone(phone)) {
            showAlert("Erreur de validation", "Le téléphone doit contenir uniquement des chiffres.",
                    Alert.AlertType.ERROR);
            return;
        }

        // Check if editing an existing user - only admins can modify
        if (user != null && !UserSession.getInstance().isAdmin()) {
            showAlert("Accès refusé", "Seuls les administrateurs peuvent modifier les utilisateurs.",
                    Alert.AlertType.ERROR);
            return;
        }

        try {
            if (user == null) {
                User newUser = new User(name, email, phone, password, role);
                serviceUser.ajouter(newUser);
            } else {
                user.setName(name);
                user.setEmail(email);
                user.setPhone(phone);
                user.setPassword(password);
                user.setRole(role);
                serviceUser.modifier(user);
            }

            if (updateCallback != null) {
                updateCallback.run();
            }

            closeWindow();
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors de l'enregistrement de l'utilisateur: " + e.getMessage(),
                    Alert.AlertType.ERROR);
        }
    }

    /**
     * Valide que le nom contient uniquement des lettres et des espaces
     */
    private boolean isValidName(String name) {
        return name.matches("[a-zA-ZÀ-ÿ\\s]+");
    }

    /**
     * Valide que l'email contient le caractère @
     */
    private boolean isValidEmail(String email) {
        return email.contains("@");
    }

    /**
     * Valide que le téléphone contient uniquement des chiffres
     */
    private boolean isValidPhone(String phone) {
        return phone.matches("\\d+");
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        closeWindow();
    }

    @FXML
    private void handleClear(ActionEvent event) {
        nameField.clear();
        emailField.clear();
        phoneField.clear();
        passwordField.clear();
        roleComboBox.setValue(null);
    }

    private void closeWindow() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/Fintech/views/UserManagement.fxml"));
            javafx.scene.Parent root = loader.load();
            javafx.stage.Stage stage = (javafx.stage.Stage) nameField.getScene().getWindow();
            javafx.scene.Scene scene = new javafx.scene.Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (java.io.IOException e) {
            showAlert("Erreur", "Erreur lors de la navigation: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
