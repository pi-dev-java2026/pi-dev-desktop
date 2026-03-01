package Fintech.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainDashboardController implements Initializable {

    @FXML
    private StackPane contentArea;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Load default view (Dashboard Home)
        loadView("/Fintech/views/DashboardHome.fxml");
    }

    @FXML
    private void showDashboard(ActionEvent event) {
        loadView("/Fintech/views/DashboardHome.fxml");
    }

    @FXML
    private void showUsers(ActionEvent event) {
        loadView("/Fintech/views/UserList.fxml");
    }

    @FXML
    private void showReclamations(ActionEvent event) {
        loadView("/Fintech/views/ReclamationManagement.fxml");
    }

    private void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();
            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
            // Optional: Show error alert
        }
    }
}
