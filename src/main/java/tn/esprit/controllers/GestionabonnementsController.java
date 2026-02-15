package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import tn.esprit.entities.Abonnement;
import tn.esprit.entities.Paiement;
import tn.esprit.services.AbonnementService;
import tn.esprit.services.PaiementService;
import java.sql.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GestionabonnementsController {

    @FXML private VBox pageListe, pageDetailService, pageFormulaire;
    @FXML private Label lblTotalMensuel;
    @FXML private TextField txtRecherche;
    @FXML private ComboBox<String> comboFiltreCategorie, comboTri;
    @FXML private VBox listeServices;
    @FXML private Label lblServiceNom, lblServiceSousTitre;
    @FXML private HBox statsService;
    @FXML private VBox listeAbonnementsService;
    @FXML private TextField txtNom, txtPrix;
    @FXML private ComboBox<String> comboCategorie, comboFrequence;
    @FXML private DatePicker datePicker;

    private MainController mainController;
    private final AbonnementService aboService  = new AbonnementService();
    private final PaiementService   paieService = new PaiementService();
    private List<Abonnement> tous;
    private String nomServiceSelectionne;

    public void setMainController(MainController mc) { this.mainController = mc; }

    @FXML
    public void initialize() {
        comboFiltreCategorie.setItems(FXCollections.observableArrayList(
                "Toutes", "🎬 Streaming", "🎵 Musique", "🏋️ Sport",
                "📚 Éducation", "🎮 Jeux", "☁️ Cloud", "🔧 Autre"
        ));
        comboTri.setItems(FXCollections.observableArrayList(
                "Prix croissant ↑", "Prix décroissant ↓"
        ));
        comboCategorie.setItems(FXCollections.observableArrayList(
                "🎬 Streaming", "🎵 Musique", "🏋️ Sport",
                "📚 Éducation", "🎮 Jeux", "☁️ Cloud", "🔧 Autre"
        ));
        comboFrequence.setItems(FXCollections.observableArrayList("Mensuel", "Annuel"));
        chargerTout();
    }

    // ===== NAVIGATION =====
    private void showPage(VBox p) {
        pageListe.setVisible(false);         pageListe.setManaged(false);
        pageDetailService.setVisible(false); pageDetailService.setManaged(false);
        pageFormulaire.setVisible(false);    pageFormulaire.setManaged(false);
        p.setVisible(true); p.setManaged(true);
    }

    @FXML private void ouvrirFormulaire() { vider(); showPage(pageFormulaire); }
    @FXML private void retourListe()      { chargerTout(); showPage(pageListe); }

    // ===== BOUTON REFRESH =====
    @FXML
    private void rafraichir() {
        txtRecherche.clear();
        comboFiltreCategorie.setValue(null);
        comboTri.setValue(null);
        chargerTout();
    }

    // ===== CHARGEMENT =====
    private void chargerTout() {
        tous = aboService.afficher();
        afficherListe(tous);
        majTotal(tous);
    }

    // FIX TOTAL : additionner TOUS les abonnements groupés
    private void majTotal(List<Abonnement> liste) {
        // Grouper par nom et sommer le prix mensuel de TOUS
        double total = liste.stream()
                .filter(a -> "Mensuel".equals(a.getFrequence()))
                .mapToDouble(Abonnement::getPrix)
                .sum();
        lblTotalMensuel.setText(String.format("%.3f TND", total));
    }

    private void afficherListe(List<Abonnement> liste) {
        listeServices.getChildren().clear();

        // Grouper par nom (insensible casse + espaces)
        Map<String, List<Abonnement>> parService = liste.stream()
                .collect(Collectors.groupingBy(a -> a.getNom().trim().toLowerCase()));

        if (parService.isEmpty()) {
            Label vide = new Label("Aucun abonnement. Cliquez sur + Ajouter !");
            vide.setStyle("-fx-text-fill: #8899aa; -fx-font-size: 14px; -fx-padding: 30;");
            listeServices.getChildren().add(vide);
            return;
        }

        // FIX TRI : trier les groupes par prix total
        String tri = comboTri.getValue();
        List<Map.Entry<String, List<Abonnement>>> entries =
                new java.util.ArrayList<>(parService.entrySet());

        if ("Prix croissant ↑".equals(tri)) {
            entries.sort((e1, e2) -> {
                double p1 = e1.getValue().stream().mapToDouble(Abonnement::getPrix).sum();
                double p2 = e2.getValue().stream().mapToDouble(Abonnement::getPrix).sum();
                return Double.compare(p1, p2);
            });
        } else if ("Prix décroissant ↓".equals(tri)) {
            entries.sort((e1, e2) -> {
                double p1 = e1.getValue().stream().mapToDouble(Abonnement::getPrix).sum();
                double p2 = e2.getValue().stream().mapToDouble(Abonnement::getPrix).sum();
                return Double.compare(p2, p1);
            });
        }

        for (Map.Entry<String, List<Abonnement>> entry : entries) {
            listeServices.getChildren().add(
                    creerLigne(entry.getValue().get(0), entry.getValue())
            );
        }
    }

    private HBox creerLigne(Abonnement rep, List<Abonnement> abos) {
        HBox ligne = new HBox(18);
        ligne.setAlignment(Pos.CENTER_LEFT);
        ligne.setStyle(
                "-fx-background-color: white; -fx-padding: 18 25; -fx-background-radius: 14; " +
                        "-fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 8, 0, 0, 2);"
        );

        StackPane icone = creerIcone(rep.getNom());

        VBox infos = new VBox(4);
        HBox.setHgrow(infos, Priority.ALWAYS);
        HBox top = new HBox(10);
        top.setAlignment(Pos.CENTER_LEFT);
        Label nom = new Label(rep.getNom());
        nom.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #0f1f3d;");
        String cat = rep.getCategorie();
        String catNom = cat.contains(" ") ? cat.substring(cat.indexOf(" ") + 1) : cat;
        Label badge = new Label(catNom);
        badge.setStyle("-fx-background-color: #e8eeff; -fx-text-fill: #1a3a7a; " +
                "-fx-font-size: 10px; -fx-font-weight: bold; -fx-padding: 3 10; -fx-background-radius: 20;");
        top.getChildren().addAll(nom, badge);

        long nbPayes = abos.stream()
                .flatMap(a -> paieService.afficher().stream()
                        .filter(p -> p.getAbonnementId() == a.getId() && "Payé".equals(p.getStatut())))
                .count();
        Label sous = new Label(abos.size() + " abonnement(s)  •  " + nbPayes + " paiements réglés");
        sous.setStyle("-fx-font-size: 11px; -fx-text-fill: #8899aa;");
        infos.getChildren().addAll(top, sous);

        boolean actif = abos.stream().anyMatch(Abonnement::isActif);
        Label statut = new Label(actif ? "● Actif" : "● Inactif");
        statut.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; " +
                "-fx-text-fill: " + (actif ? "#27ae60" : "#e67e22") + ";");

        // FIX TOTAL DISNEY : somme de TOUS les abonnements du service
        double prixTotal = abos.stream()
                .filter(a -> "Mensuel".equals(a.getFrequence()))
                .mapToDouble(Abonnement::getPrix).sum();
        // Si annuel, ramener au mensuel
        double prixAnnuel = abos.stream()
                .filter(a -> "Annuel".equals(a.getFrequence()))
                .mapToDouble(a -> a.getPrix() / 12.0).sum();
        double prixMensuelTotal = prixTotal + prixAnnuel;

        VBox prixBox = new VBox(2);
        prixBox.setAlignment(Pos.CENTER_RIGHT);
        Label prix = new Label(String.format("%.3f", prixMensuelTotal));
        prix.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1a3a7a;");
        Label unite = new Label("TND/mois");
        unite.setStyle("-fx-font-size: 10px; -fx-text-fill: #8899aa;");
        prixBox.getChildren().addAll(prix, unite);

        Label fleche = new Label("›");
        fleche.setStyle("-fx-font-size: 24px; -fx-text-fill: #1a3a7a; -fx-font-weight: bold;");

        ligne.getChildren().addAll(icone, infos, statut, prixBox, fleche);

        ligne.setOnMouseEntered(e -> ligne.setStyle(
                "-fx-background-color: #f0f4ff; -fx-padding: 18 25; -fx-background-radius: 14; " +
                        "-fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(26,58,122,0.12), 14, 0, 0, 4); " +
                        "-fx-border-color: #1a3a7a; -fx-border-width: 1.5; -fx-border-radius: 14;"
        ));
        ligne.setOnMouseExited(e -> ligne.setStyle(
                "-fx-background-color: white; -fx-padding: 18 25; -fx-background-radius: 14; " +
                        "-fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 8, 0, 0, 2);"
        ));
        ligne.setOnMouseClicked(e -> ouvrirDetail(rep.getNom(), abos));
        return ligne;
    }

    // ===== FILTRES =====
    @FXML
    private void appliquerFiltres() {
        String rech = txtRecherche.getText().toLowerCase().trim();
        String cat  = comboFiltreCategorie.getValue();

        List<Abonnement> f = tous.stream()
                .filter(a -> rech.isEmpty()
                        || a.getNom().toLowerCase().contains(rech)
                        || a.getCategorie().toLowerCase().contains(rech))
                .filter(a -> cat == null || "Toutes".equals(cat) || a.getCategorie().equals(cat))
                .collect(Collectors.toList());

        afficherListe(f);
        majTotal(f);
    }

    // ===== DÉTAIL SERVICE =====
    private void ouvrirDetail(String nomService, List<Abonnement> abos) {
        nomServiceSelectionne = nomService;
        lblServiceNom.setText(nomService);
        lblServiceSousTitre.setText(abos.size() + " abonnement(s)");

        statsService.getChildren().clear();
        double total = abos.stream()
                .mapToDouble(a -> "Mensuel".equals(a.getFrequence()) ? a.getPrix() : a.getPrix() / 12.0)
                .sum();
        long nbPaie = abos.stream()
                .mapToLong(a -> paieService.afficher().stream()
                        .filter(p -> p.getAbonnementId() == a.getId()).count()).sum();

        statsService.getChildren().addAll(
                statCard("💰 Total mensuel", String.format("%.3f TND", total), "#1a3a7a"),
                statCard("📋 Abonnements",   String.valueOf(abos.size()),       "#0f1f3d"),
                statCard("🧾 Paiements",     String.valueOf(nbPaie),            "#27ae60")
        );

        listeAbonnementsService.getChildren().clear();
        List<Abonnement> fresh = aboService.afficher().stream()
                .filter(a -> a.getNom().trim().equalsIgnoreCase(nomService.trim()))
                .toList();
        fresh.forEach(a -> listeAbonnementsService.getChildren().add(creerCarteAbo(a)));
        showPage(pageDetailService);
    }

    private VBox creerCarteAbo(Abonnement a) {
        VBox carte = new VBox(12);
        carte.setStyle("-fx-background-color: white; -fx-padding: 20 25; " +
                "-fx-background-radius: 14; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.07), 10, 0, 0, 3);");

        HBox top = new HBox(12);
        top.setAlignment(Pos.CENTER_LEFT);
        Label nom = new Label(a.getNom() + "  —  " + a.getFrequence());
        nom.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #0f1f3d;");
        HBox.setHgrow(nom, Priority.ALWAYS);
        Label statut = new Label(a.isActif() ? "✅ Actif" : "❌ Inactif");
        statut.setStyle("-fx-background-color: " + (a.isActif() ? "#e8fff4" : "#fff0f0") + "; " +
                "-fx-text-fill: " + (a.isActif() ? "#27ae60" : "#e74c3c") + "; " +
                "-fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 5 14; -fx-background-radius: 20;");
        top.getChildren().addAll(nom, statut);

        HBox infos = new HBox(40);
        infos.getChildren().addAll(
                infoBox("PRIX", String.format("%.3f TND", a.getPrix())),
                infoBox("DATE DÉBUT", a.getDateDebut().toString()),
                infoBox("COÛT ANNUEL", String.format("%.3f TND", a.getPrix() * 12))
        );

        List<Paiement> paies = paieService.afficher().stream()
                .filter(p -> p.getAbonnementId() == a.getId()).toList();
        long nbP = paies.stream().filter(p -> "Payé".equals(p.getStatut())).count();
        long nbAv = paies.stream().filter(p -> "À venir".equals(p.getStatut())).count();

        HBox resume = new HBox(10);
        resume.setStyle("-fx-background-color: #f0f4f8; -fx-padding: 10 18; -fx-background-radius: 8;");
        Label ri = new Label("💳  " + paies.size() + " paiements  •  ✅ " + nbP + " payés  •  ⏳ " + nbAv + " à venir");
        ri.setStyle("-fx-font-size: 12px; -fx-text-fill: #334455;");
        resume.getChildren().add(ri);

        HBox boutons = new HBox(10);
        Button btnEdit = new Button("✏️");
        btnEdit.setStyle("-fx-background-color: #e8eeff; -fx-font-size: 14px; " +
                "-fx-padding: 8 14; -fx-background-radius: 8; -fx-cursor: hand;");
        btnEdit.setTooltip(new Tooltip("Modifier"));
        btnEdit.setOnAction(e -> afficherDialogModifier(a));

        Button btnDel = new Button("🗑");
        btnDel.setStyle("-fx-background-color: #fff0f0; -fx-font-size: 14px; " +
                "-fx-padding: 8 14; -fx-background-radius: 8; -fx-cursor: hand;");
        btnDel.setTooltip(new Tooltip("Supprimer"));
        btnDel.setOnAction(e -> supprimerAbo(a));

        boutons.getChildren().addAll(btnEdit, btnDel);
        carte.getChildren().addAll(top, infos, resume, boutons);
        return carte;
    }

    // ===== BEAU FORMULAIRE MODIFIER =====
    private void afficherDialogModifier(Abonnement a) {
        Dialog<ButtonType> dlg = new Dialog<>();
        dlg.setTitle(null);
        dlg.setHeaderText(null);

        // Contenu custom
        VBox root = new VBox(0);
        root.setPrefWidth(480);

        // Header bleu
        VBox header = new VBox(5);
        header.setStyle("-fx-background-color: #0f1f3d; -fx-padding: 25 30 20 30; -fx-background-radius: 12 12 0 0;");
        StackPane iconeCercle = creerIcone(a.getNom());
        Label titreH = new Label("Modifier l'abonnement");
        titreH.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");
        Label sousTitreH = new Label(a.getNom() + "  —  " + a.getCategorie());
        sousTitreH.setStyle("-fx-font-size: 11px; -fx-text-fill: #6a8aaa;");
        HBox hdrTop = new HBox(15);
        hdrTop.setAlignment(Pos.CENTER_LEFT);
        VBox hdrText = new VBox(4);
        hdrText.getChildren().addAll(titreH, sousTitreH);
        hdrTop.getChildren().addAll(iconeCercle, hdrText);
        header.getChildren().add(hdrTop);

        // Corps blanc
        VBox corps = new VBox(18);
        corps.setStyle("-fx-background-color: white; -fx-padding: 25 30; -fx-background-radius: 0 0 12 12;");

        // Champ Nom
        VBox vNom = new VBox(7);
        Label lNom = new Label("Nom du service");
        lNom.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #445566;");
        TextField fNom = new TextField(a.getNom());
        fNom.setStyle("-fx-background-radius: 9; -fx-border-radius: 9; -fx-border-color: #e0e8f0; " +
                "-fx-border-width: 1.5; -fx-padding: 11 14; -fx-font-size: 13px; -fx-background-color: #fafbfc;");
        vNom.getChildren().addAll(lNom, fNom);

        // Champ Prix
        VBox vPrix = new VBox(7);
        Label lPrix = new Label("Prix (TND)");
        lPrix.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #445566;");
        TextField fPrix = new TextField(String.valueOf(a.getPrix()));
        fPrix.setStyle("-fx-background-radius: 9; -fx-border-radius: 9; -fx-border-color: #e0e8f0; " +
                "-fx-border-width: 1.5; -fx-padding: 11 14; -fx-font-size: 13px; -fx-background-color: #fafbfc;");
        vPrix.getChildren().addAll(lPrix, fPrix);

        // HBox Nom + Prix
        HBox row1 = new HBox(15);
        HBox.setHgrow(vNom, Priority.ALWAYS);
        HBox.setHgrow(vPrix, Priority.ALWAYS);
        row1.getChildren().addAll(vNom, vPrix);

        // Fréquence
        VBox vFreq = new VBox(7);
        Label lFreq = new Label("Fréquence");
        lFreq.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #445566;");
        ComboBox<String> fFreq = new ComboBox<>();
        fFreq.setItems(FXCollections.observableArrayList("Mensuel", "Annuel"));
        fFreq.setValue(a.getFrequence());
        fFreq.setMaxWidth(Double.MAX_VALUE);
        fFreq.setStyle("-fx-background-radius: 9; -fx-font-size: 13px;");
        vFreq.getChildren().addAll(lFreq, fFreq);

        // Statut toggle
        VBox vStatut = new VBox(7);
        Label lStatut = new Label("Statut");
        lStatut.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #445566;");
        ToggleGroup tg = new ToggleGroup();
        HBox toggleBox = new HBox(0);
        RadioButton rbActif   = new RadioButton("✅ Actif");
        RadioButton rbInactif = new RadioButton("❌ Inactif");
        rbActif.setToggleGroup(tg);
        rbInactif.setToggleGroup(tg);
        rbActif.setSelected(a.isActif());
        rbInactif.setSelected(!a.isActif());
        String styleRb = "-fx-padding: 10 20; -fx-cursor: hand; -fx-font-size: 12px;";
        rbActif.setStyle(styleRb + "-fx-background-color: " + (a.isActif() ? "#e8fff4" : "#f5f5f5") +
                "; -fx-background-radius: 8 0 0 8;");
        rbInactif.setStyle(styleRb + "-fx-background-color: " + (!a.isActif() ? "#fff0f0" : "#f5f5f5") +
                "; -fx-background-radius: 0 8 8 0;");
        tg.selectedToggleProperty().addListener((obs, o, n) -> {
            rbActif.setStyle(styleRb + "-fx-background-color: " +
                    (rbActif.isSelected() ? "#e8fff4" : "#f5f5f5") + "; -fx-background-radius: 8 0 0 8;");
            rbInactif.setStyle(styleRb + "-fx-background-color: " +
                    (rbInactif.isSelected() ? "#fff0f0" : "#f5f5f5") + "; -fx-background-radius: 0 8 8 0;");
        });
        toggleBox.getChildren().addAll(rbActif, rbInactif);
        vStatut.getChildren().addAll(lStatut, toggleBox);

        HBox row2 = new HBox(15);
        HBox.setHgrow(vFreq, Priority.ALWAYS);
        HBox.setHgrow(vStatut, Priority.ALWAYS);
        row2.getChildren().addAll(vFreq, vStatut);

        // Séparateur
        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: #e8eef5;");

        // Boutons
        HBox btnBox = new HBox(12);
        btnBox.setAlignment(Pos.CENTER_RIGHT);
        Button btnCancel = new Button("Annuler");
        btnCancel.setStyle("-fx-background-color: white; -fx-text-fill: #556677; " +
                "-fx-padding: 11 24; -fx-background-radius: 8; -fx-cursor: hand; " +
                "-fx-border-color: #dde3ea; -fx-border-radius: 8; -fx-border-width: 1.5; -fx-font-size: 13px;");
        Button btnSave = new Button("💾  Enregistrer");
        btnSave.setStyle("-fx-background-color: #1a3a7a; -fx-text-fill: white; " +
                "-fx-padding: 11 28; -fx-background-radius: 8; -fx-cursor: hand; " +
                "-fx-font-size: 13px; -fx-font-weight: bold; " +
                "-fx-effect: dropshadow(gaussian, rgba(26,58,122,0.4), 8, 0, 0, 3);");
        btnBox.getChildren().addAll(btnCancel, btnSave);

        corps.getChildren().addAll(row1, row2, sep, btnBox);
        root.getChildren().addAll(header, corps);

        dlg.getDialogPane().setContent(root);
        dlg.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        // Cacher le bouton close par défaut
        dlg.getDialogPane().lookupButton(ButtonType.CLOSE).setVisible(false);
        dlg.getDialogPane().setStyle("-fx-padding: 0; -fx-background-radius: 12;");

        btnCancel.setOnAction(e -> dlg.close());
        btnSave.setOnAction(e -> {
            try {
                a.setNom(fNom.getText());
                a.setPrix(Double.parseDouble(fPrix.getText()));
                a.setFrequence(fFreq.getValue());
                a.setActif(rbActif.isSelected());
                aboService.modifier(a);
                dlg.close();
                alerte(Alert.AlertType.INFORMATION, "✅ Modifié avec succès !");
                List<Abonnement> maj = aboService.afficher().stream()
                        .filter(ab -> ab.getNom().trim().equalsIgnoreCase(a.getNom().trim()))
                        .toList();
                ouvrirDetail(a.getNom(), maj);
            } catch (NumberFormatException ex) {
                alerte(Alert.AlertType.ERROR, "❌ Prix invalide !");
            }
        });

        dlg.showAndWait();
    }

    // ===== FORMULAIRE AJOUT =====
    @FXML
    private void ajouter() {
        if (txtNom.getText().isEmpty() || txtPrix.getText().isEmpty()
                || comboCategorie.getValue() == null
                || comboFrequence.getValue() == null
                || datePicker.getValue() == null) {
            alerte(Alert.AlertType.WARNING, "⚠️ Remplissez tous les champs !");
            return;
        }
        try {
            aboService.ajouter(new Abonnement(
                    txtNom.getText(), Double.parseDouble(txtPrix.getText()),
                    Date.valueOf(datePicker.getValue()),
                    comboFrequence.getValue(), comboCategorie.getValue(), true
            ));
            alerte(Alert.AlertType.INFORMATION, "✅ Ajouté !");
            retourListe();
        } catch (NumberFormatException e) {
            alerte(Alert.AlertType.ERROR, "❌ Prix invalide !");
        }
    }

    private void supprimerAbo(Abonnement a) {
        Alert c = new Alert(Alert.AlertType.CONFIRMATION);
        c.setHeaderText(null);
        c.setContentText("Supprimer « " + a.getNom() + " » ?");
        c.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                aboService.supprimer(a.getId());
                List<Abonnement> reste = aboService.afficher().stream()
                        .filter(ab -> ab.getNom().trim().equalsIgnoreCase(nomServiceSelectionne.trim()))
                        .toList();
                if (reste.isEmpty()) retourListe();
                else ouvrirDetail(nomServiceSelectionne, reste);
            }
        });
    }

    // ===== HELPERS =====
    private void vider() {
        txtNom.clear(); txtPrix.clear();
        comboCategorie.setValue(null); comboFrequence.setValue(null); datePicker.setValue(null);
    }
    private void alerte(Alert.AlertType t, String msg) {
        Alert a = new Alert(t); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }
    private VBox statCard(String label, String val, String color) {
        VBox c = new VBox(5); c.setPrefWidth(190);
        c.setStyle("-fx-background-color: white; -fx-padding: 16; -fx-background-radius: 12; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 8, 0, 0, 2);");
        Label l = new Label(label); l.setStyle("-fx-font-size: 11px; -fx-text-fill: #8899aa;");
        Label v = new Label(val);   v.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");
        c.getChildren().addAll(l, v); return c;
    }
    private VBox infoBox(String label, String val) {
        VBox v = new VBox(4);
        Label l = new Label(label); l.setStyle("-fx-text-fill: #8899aa; -fx-font-size: 10px; -fx-font-weight: bold;");
        Label d = new Label(val);   d.setStyle("-fx-text-fill: #0f1f3d; -fx-font-size: 14px; -fx-font-weight: bold;");
        v.getChildren().addAll(l, d); return v;
    }
    private StackPane creerIcone(String nom) {
        Circle c = new Circle(24); c.setFill(Color.web(getCouleur(nom)));
        Label l = new Label(getLogo(nom));
        l.setStyle("-fx-font-size: 13px; -fx-text-fill: white; -fx-font-weight: bold;");
        return new StackPane(c, l);
    }
    private String getCouleur(String nom) {
        String n = nom.toLowerCase();
        if (n.contains("netflix"))   return "#E50914";
        if (n.contains("spotify"))   return "#1DB954";
        if (n.contains("amazon"))    return "#FF9900";
        if (n.contains("disney"))    return "#113CCF";
        if (n.contains("youtube"))   return "#FF0000";
        if (n.contains("deezer"))    return "#A238FF";
        if (n.contains("microsoft")) return "#0078D4";
        if (n.contains("google"))    return "#4285F4";
        if (n.contains("adobe"))     return "#FF4444";
        if (n.contains("apple"))     return "#555555";
        return "#1a3a7a";
    }
    private String getLogo(String nom) {
        String n = nom.toLowerCase();
        if (n.contains("netflix"))   return "N";
        if (n.contains("spotify"))   return "S";
        if (n.contains("amazon"))    return "a";
        if (n.contains("disney"))    return "D+";
        if (n.contains("youtube"))   return "▶";
        if (n.contains("deezer"))    return "Dz";
        if (n.contains("microsoft")) return "M";
        if (n.contains("google"))    return "G";
        if (n.contains("adobe"))     return "Ai";
        return nom.substring(0, Math.min(2, nom.length())).toUpperCase();
    }
    /**
     * Ouvrir la page Paiements depuis Abonnements
     */
    @FXML
    private void ouvrirPaiements() {
        if (mainController != null) {
            mainController.switchPaiements();
        }
    }
}