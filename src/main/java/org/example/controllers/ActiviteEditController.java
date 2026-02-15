package org.example.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import org.example.entities.Activite;
import org.example.services.ServiceActivite;

import java.io.IOException;
import java.sql.Date;

public class ActiviteEditController {

    @FXML private TextField descriptionTF;
    @FXML private TextField montantTF;
    @FXML private DatePicker dateDP;
    @FXML private ComboBox<String> statutCB;
    @FXML private Label msgLabel;

    private Activite current;
    private final ServiceActivite service = new ServiceActivite();

    @FXML
    public void initialize() {
        statutCB.getItems().setAll("EN_ATTENTE", "PAYEE", "ANNULEE");
    }

    public void setActivite(Activite a) {
        this.current = a;
        descriptionTF.setText(a.getDescription());
        montantTF.setText(String.valueOf(a.getMontant()));
        if (a.getDateActivite() != null) dateDP.setValue(a.getDateActivite().toLocalDate());
        statutCB.setValue(a.getStatut());
    }

    @FXML
    private void save() {
        msgLabel.setText("");
        msgLabel.setStyle("-fx-text-fill: #d22;");

        if (current == null) {
            msgLabel.setText("Activité introuvable.");
            return;
        }

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
            msgLabel.setText("Montant invalide.");
            return;
        }

        current.setDescription(desc);
        current.setMontant(montant);
        current.setDateActivite(Date.valueOf(localDate));
        current.setStatut(statut);

        try {
            service.modifier(current);
            backToList();
        } catch (Exception e) {
            e.printStackTrace();
            msgLabel.setText("Erreur lors de la modification.");
        }
    }

    @FXML
    private void backToList() {
        try {
            Parent view = FXMLLoader.load(getClass().getResource("/ActiviteList.fxml"));
            StackPane contentPane = (StackPane) descriptionTF.getScene().lookup("#contentPane");
            if (contentPane != null) contentPane.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
