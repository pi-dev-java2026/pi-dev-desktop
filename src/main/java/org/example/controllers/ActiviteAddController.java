package org.example.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import org.example.entities.Activite;
import org.example.services.ServiceActivite;

import java.sql.Date;

public class ActiviteAddController {

    @FXML private TextField descriptionTF;
    @FXML private TextField montantTF;
    @FXML private DatePicker dateDP;
    @FXML private ComboBox<String> statutCB;
    @FXML private Label msgLabel;

    private final ServiceActivite service = new ServiceActivite();

    @FXML
    public void initialize() {
        statutCB.getItems().setAll("EN_ATTENTE", "PAYEE", "ANNULEE");
        statutCB.getSelectionModel().select("EN_ATTENTE");
    }

    @FXML
    private void ajouter() {
        msgLabel.setText("");
        msgLabel.setStyle("-fx-text-fill: #d22;");

        String desc = descriptionTF.getText() == null ? "" : descriptionTF.getText().trim();
        String montantStr = montantTF.getText() == null ? "" : montantTF.getText().trim();
        var localDate = dateDP.getValue();
        String statut = statutCB.getValue();

        if (desc.isEmpty() || montantStr.isEmpty() || localDate == null || statut == null) {
            msgLabel.setText("Veuillez remplir tous les champs obligatoires.");
            return;
        }

        double montant;
        try {
            montant = Double.parseDouble(montantStr);
            if (montant <= 0) {
                msgLabel.setText("Le montant doit être > 0.");
                return;
            }
        } catch (NumberFormatException e) {
            msgLabel.setText("Montant invalide (ex: 120).");
            return;
        }

        try {
            Activite a = new Activite(desc, montant, Date.valueOf(localDate), statut);
            service.ajouter(a);

            msgLabel.setStyle("-fx-text-fill: green;");
            msgLabel.setText("Activité ajoutée ");
            clear();

        } catch (Exception e) {
            e.printStackTrace();
            msgLabel.setText("Erreur lors de l'ajout.");
        }
    }

    @FXML
    private void clear() {
        descriptionTF.clear();
        montantTF.clear();
        dateDP.setValue(null);
        statutCB.getSelectionModel().select("EN_ATTENTE");
    }

    @FXML
    private void goList() {
        swapCenter("/ActiviteList.fxml");
    }

    private void swapCenter(String fxml) {
        try {
            Parent view = FXMLLoader.load(getClass().getResource(fxml));
            StackPane contentPane = (StackPane) ((Node) descriptionTF).getScene().lookup("#contentPane");
            if (contentPane != null) contentPane.getChildren().setAll(view);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
