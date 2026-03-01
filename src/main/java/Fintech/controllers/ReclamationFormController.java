package Fintech.controllers;

import Fintech.entities.Reclamation;
import Fintech.servicies.ServiceReclamation;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class ReclamationFormController implements Initializable {

    @FXML
    private TextField emailField;
    @FXML
    private TextField subjectField;
    @FXML
    private TextArea descriptionField;

    private ServiceReclamation serviceReclamation = new ServiceReclamation();
    private Reclamation reclamation;
    private Runnable updateCallback;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Configuration initiale si nécessaire
    }

    /**
     * Définit une réclamation existante pour modification
     */
    public void setReclamation(Reclamation reclamation) {
        this.reclamation = reclamation;
        if (reclamation != null) {
            emailField.setText(reclamation.getEmail());
            subjectField.setText(reclamation.getSubject());
            descriptionField.setText(reclamation.getDescription());
        }
    }

    /**
     * Définit un callback pour rafraîchir la liste après soumission
     */
    public void setUpdateCallback(Runnable updateCallback) {
        this.updateCallback = updateCallback;
    }

    @FXML
    private void handleSubmit(ActionEvent event) {
        String email = emailField.getText().trim();
        String subject = subjectField.getText().trim();
        String description = descriptionField.getText().trim();

        // Validation des champs vides
        if (email.isEmpty() || subject.isEmpty() || description.isEmpty()) {
            showAlert("Erreur", "Veuillez remplir tous les champs.", Alert.AlertType.ERROR);
            return;
        }

        // Validation de l'email (doit contenir @)
        if (!isValidEmail(email)) {
            showAlert("Erreur de validation", "L'email doit contenir le caractère '@'.", Alert.AlertType.ERROR);
            return;
        }

        // Validation du sujet (minimum 3 caractères)
        if (subject.length() < 3) {
            showAlert("Erreur de validation", "Le sujet doit contenir au moins 3 caractères.",
                    Alert.AlertType.ERROR);
            return;
        }

        // Validation de la description (minimum 10 caractères)
        if (description.length() < 10) {
            showAlert("Erreur de validation", "La description doit contenir au moins 10 caractères.",
                    Alert.AlertType.ERROR);
            return;
        }

        try {
            if (reclamation == null) {
                // Nouvelle réclamation
                Reclamation newReclamation = new Reclamation();
                newReclamation.setEmail(email);
                newReclamation.setSubject(subject);
                newReclamation.setDescription(description);
                newReclamation.setStatut("En attente"); // Statut par défaut
                serviceReclamation.ajouter(newReclamation);
                showAlert("Succès", "Réclamation soumise avec succès!", Alert.AlertType.INFORMATION);
            } else {
                // Modification d'une réclamation existante
                reclamation.setEmail(email);
                reclamation.setSubject(subject);
                reclamation.setDescription(description);
                // Keep the existing status when modifying
                serviceReclamation.modifier(reclamation);
                showAlert("Succès", "Réclamation modifiée avec succès!", Alert.AlertType.INFORMATION);
            }

            if (updateCallback != null) {
                updateCallback.run();
            }

            // Effacer les champs après soumission réussie
            handleClear(null);

        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors de l'enregistrement de la réclamation: " + e.getMessage(),
                    Alert.AlertType.ERROR);
        }
    }

    /**
     * Valide que l'email contient le caractère @
     */
    private boolean isValidEmail(String email) {
        return email.contains("@");
    }

    @FXML
    private void handleClear(ActionEvent event) {
        emailField.clear();
        subjectField.clear();
        descriptionField.clear();
    }

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/Fintech/views/ReclamationManagement.fxml"));
            javafx.scene.Parent root = loader.load();
            javafx.stage.Stage stage = (javafx.stage.Stage) emailField.getScene().getWindow();
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
