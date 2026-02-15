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
import javafx.scene.layout.VBox;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

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
                    // Create card layout for each user
                    HBox card = new HBox(20);
                    card.setAlignment(Pos.CENTER_LEFT);
                    card.setPadding(new javafx.geometry.Insets(15, 20, 15, 20));
                    card.setStyle(
                            "-fx-background-color: white; -fx-background-radius: 8px; -fx-border-color: #e0e0e0; -fx-border-radius: 8px; -fx-border-width: 1px;");

                    // ID
                    Label idLabel = new Label("#" + user.getId());
                    idLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #666; -fx-min-width: 50px;");

                    // Name
                    VBox nameBox = new VBox(2);
                    Label nameLabel = new Label(user.getName());
                    nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
                    Label emailLabel = new Label(user.getEmail());
                    emailLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 12px;");
                    nameBox.getChildren().addAll(nameLabel, emailLabel);
                    nameBox.setPrefWidth(250);

                    // Phone
                    Label phoneLabel = new Label("📱 " + user.getPhone());
                    phoneLabel.setStyle("-fx-text-fill: #555; -fx-min-width: 150px;");

                    // Password (masked)
                    Label passwordLabel = new Label("🔒 • • • • • • • •");
                    passwordLabel.setStyle("-fx-text-fill: #999; -fx-min-width: 120px;");

                    // Role Badge
                    Label roleBadge = new Label(user.getRole());
                    roleBadge.getStyleClass().add("role-badge");
                    roleBadge.setPadding(new javafx.geometry.Insets(5, 15, 5, 15));
                    roleBadge.setStyle("-fx-background-radius: 15px; -fx-font-size: 11px; -fx-font-weight: bold;");

                    if (user.getRole().equalsIgnoreCase("Admin")) {
                        roleBadge.setStyle(
                                roleBadge.getStyle() + "-fx-background-color: #e3f2fd; -fx-text-fill: #1976d2;");
                    } else if (user.getRole().equalsIgnoreCase("Manager")) {
                        roleBadge.setStyle(
                                roleBadge.getStyle() + "-fx-background-color: #fff3e0; -fx-text-fill: #f57c00;");
                    } else {
                        roleBadge.setStyle(
                                roleBadge.getStyle() + "-fx-background-color: #f1f8e9; -fx-text-fill: #689f38;");
                    }

                    // Spacer
                    Region spacer = new Region();
                    HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

                    // Action buttons
                    Button editBtn = new Button("✏️");
                    Button deleteBtn = new Button("🗑️");

                    editBtn.setStyle(
                            "-fx-background-color: #e3f2fd; -fx-text-fill: #1976d2; -fx-cursor: hand; -fx-background-radius: 5px; -fx-padding: 8px 12px;");
                    deleteBtn.setStyle(
                            "-fx-background-color: #ffebee; -fx-text-fill: #c62828; -fx-cursor: hand; -fx-background-radius: 5px; -fx-padding: 8px 12px;");

                    // Check admin permissions
                    boolean isAdmin = UserSession.getInstance().isAdmin();
                    editBtn.setDisable(!isAdmin);
                    deleteBtn.setDisable(!isAdmin);

                    if (!isAdmin) {
                        editBtn.setStyle(editBtn.getStyle() + "-fx-opacity: 0.5;");
                        deleteBtn.setStyle(deleteBtn.getStyle() + "-fx-opacity: 0.5;");
                    }

                    editBtn.setOnAction(e -> handleEditUser(user));
                    deleteBtn.setOnAction(e -> handleDeleteUser(user));

                    HBox actionsBox = new HBox(10, editBtn, deleteBtn);
                    actionsBox.setAlignment(Pos.CENTER_RIGHT);

                    // Add all elements to card
                    card.getChildren().addAll(idLabel, nameBox, phoneLabel, passwordLabel, roleBadge, spacer,
                            actionsBox);

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

    private void handleEditUser(User user) {
        // Check if user is admin
        if (!UserSession.getInstance().isAdmin()) {
            showAlert(Alert.AlertType.ERROR, "Accès refusé",
                    "Seuls les administrateurs peuvent modifier les utilisateurs.");
            return;
        }
        navigateToUserForm(null, user);
    }

    private void handleDeleteUser(User user) {
        // Check if user is admin
        if (!UserSession.getInstance().isAdmin()) {
            showAlert(Alert.AlertType.ERROR, "Accès refusé",
                    "Seuls les administrateurs peuvent supprimer les utilisateurs.");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmation");
        confirmAlert.setHeaderText("Supprimer l'utilisateur");
        confirmAlert.setContentText("Êtes-vous sûr de vouloir supprimer " + user.getName() + " ?");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                serviceUser.supprimer(user.getId());
                loadUsers();
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Utilisateur supprimé avec succès.");
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de supprimer l'utilisateur: " + e.getMessage());
            }
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

            Stage stage;
            if (event != null) {
                stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            } else {
                stage = (Stage) usersList.getScene().getWindow();
            }

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
