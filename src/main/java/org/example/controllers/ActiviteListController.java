package org.example.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.example.entities.Activite;
import org.example.services.ServiceActivite;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ActiviteListController {

    @FXML private ListView<Activite> activiteLV;

    @FXML private ComboBox<String> statutCB;
    @FXML private DatePicker dateFromDP;
    @FXML private DatePicker dateToDP;

    @FXML private TextField minTF;
    @FXML private TextField maxTF;

    @FXML private ComboBox<String> sortCB;
    @FXML private Label filterMsgLabel;

    private final ServiceActivite service = new ServiceActivite();
    private List<Activite> all = new ArrayList<>();

    @FXML
    public void initialize() {
        activiteLV.setCellFactory(lv -> new ActiviteCell());

        statutCB.getItems().setAll("Tous", "EN_ATTENTE", "PAYEE", "ANNULEE");
        statutCB.setValue("Tous");

        sortCB.getItems().setAll("Montant ↑ (croissant)", "Montant ↓ (décroissant)", "Date ↑", "Date ↓");
        sortCB.setValue("Date ↓");

        refresh();
    }

    @FXML
    private void refresh() {
        try {
            all = service.afficher();
            activiteLV.getItems().setAll(all);
            if (filterMsgLabel != null) filterMsgLabel.setText("");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void applyFilterSort() {
        if (filterMsgLabel != null) {
            filterMsgLabel.setText("");
            filterMsgLabel.setStyle("-fx-text-fill: #d22;");
        }

        String statut = statutCB.getValue();
        boolean hasStatut = statut != null && !"Tous".equals(statut);

        var from = dateFromDP.getValue();
        var to = dateToDP.getValue();

        String minStr = minTF.getText() == null ? "" : minTF.getText().trim();
        String maxStr = maxTF.getText() == null ? "" : maxTF.getText().trim();

        boolean hasMin = !minStr.isEmpty();
        boolean hasMax = !maxStr.isEmpty();
        boolean hasDateFrom = from != null;
        boolean hasDateTo = to != null;

        if (!(hasStatut || hasMin || hasMax || hasDateFrom || hasDateTo)) {
            if (filterMsgLabel != null) filterMsgLabel.setText("Choisissez au moins un filtre.");
            return;
        }

        final Double min;
        final Double max;
        try {
            min = hasMin ? Double.parseDouble(minStr) : null;
            max = hasMax ? Double.parseDouble(maxStr) : null;
            if (min != null && max != null && min > max) {
                if (filterMsgLabel != null) filterMsgLabel.setText("Montant min doit être ≤ montant max.");
                return;
            }
        } catch (NumberFormatException e) {
            if (filterMsgLabel != null) filterMsgLabel.setText("Montant min/max invalide.");
            return;
        }

        final Date dFrom = hasDateFrom ? Date.valueOf(from) : null;
        final Date dTo = hasDateTo ? Date.valueOf(to) : null;

        var stream = all.stream()
                .filter(a -> !hasStatut || (a.getStatut() != null && a.getStatut().equalsIgnoreCase(statut)))
                .filter(a -> dFrom == null || (a.getDateActivite() != null && !a.getDateActivite().before(dFrom)))
                .filter(a -> dTo == null || (a.getDateActivite() != null && !a.getDateActivite().after(dTo)))
                .filter(a -> min == null || a.getMontant() >= min)
                .filter(a -> max == null || a.getMontant() <= max);

        String sort = sortCB.getValue();
        if ("Montant ↓ (décroissant)".equals(sort)) {
            stream = stream.sorted(Comparator.comparingDouble(Activite::getMontant).reversed());
        } else if ("Montant ↑ (croissant)".equals(sort)) {
            stream = stream.sorted(Comparator.comparingDouble(Activite::getMontant));
        } else if ("Date ↑".equals(sort)) {
            stream = stream.sorted(Comparator.comparing(Activite::getDateActivite, Comparator.nullsLast(Comparator.naturalOrder())));
        } else {
            stream = stream.sorted(Comparator.comparing(Activite::getDateActivite, Comparator.nullsLast(Comparator.naturalOrder())).reversed());
        }

        List<Activite> result = stream.toList();
        activiteLV.getItems().setAll(result);

        if (result.isEmpty() && filterMsgLabel != null) filterMsgLabel.setText("Aucun résultat.");
    }

    @FXML
    private void resetFilterSort() {
        statutCB.setValue("Tous");
        dateFromDP.setValue(null);
        dateToDP.setValue(null);
        minTF.clear();
        maxTF.clear();
        sortCB.setValue("Date ↓");
        if (filterMsgLabel != null) filterMsgLabel.setText("");
        activiteLV.getItems().setAll(all);
    }

    @FXML
    private void goAdd() {
        swapCenter("/ActiviteAdd.fxml");
    }

    private void openEdit(Activite a) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ActiviteEdit.fxml"));
            Parent view = loader.load();

            ActiviteEditController ctrl = loader.getController();
            ctrl.setActivite(a);

            StackPane contentPane = (StackPane) ((Node) activiteLV).getScene().lookup("#contentPane");
            if (contentPane != null) contentPane.getChildren().setAll(view);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deleteActivite(Activite a) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer cette activité ?");
        confirm.setContentText("Description: " + a.getDescription() + "\nMontant: " + a.getMontant());

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;

        try {
            service.supprimer(a.getId());
            refresh();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void swapCenter(String fxml) {
        try {
            Parent view = FXMLLoader.load(getClass().getResource(fxml));
            StackPane contentPane = (StackPane) ((Node) activiteLV).getScene().lookup("#contentPane");
            if (contentPane != null) contentPane.getChildren().setAll(view);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class ActiviteCell extends ListCell<Activite> {
        @Override
        protected void updateItem(Activite a, boolean empty) {
            super.updateItem(a, empty);

            if (empty || a == null) {
                setText(null);
                setGraphic(null);
                return;
            }

            Label title = new Label(a.getDescription());
            title.setStyle("-fx-font-size: 15; -fx-font-weight: bold;");

            Label montant = new Label("Montant: " + a.getMontant());
            Label date = new Label("Date: " + (a.getDateActivite() == null ? "" : a.getDateActivite()));
            Label statut = new Label("Statut: " + (a.getStatut() == null ? "" : a.getStatut()));

            VBox info = new VBox(4, title, montant, date, statut);

            Button btnEdit = new Button("Modifier");
            btnEdit.getStyleClass().add("secondary-btn");
            btnEdit.setOnAction(e -> openEdit(a));

            Button btnDel = new Button("Supprimer");
            btnDel.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-background-radius: 10;");
            btnDel.setOnAction(e -> deleteActivite(a));

            VBox actions = new VBox(8, btnEdit, btnDel);
            actions.setMinWidth(110);

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            HBox root = new HBox(12, info, spacer, actions);
            root.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-padding: 12; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 12, 0.2, 0, 2);");

            setGraphic(root);
        }
    }
}
