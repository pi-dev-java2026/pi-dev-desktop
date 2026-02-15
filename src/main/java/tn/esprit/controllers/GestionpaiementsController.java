package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import tn.esprit.entities.Abonnement;
import tn.esprit.entities.Paiement;
import tn.esprit.services.AbonnementService;
import tn.esprit.services.PaiementService;
import java.sql.Date;
import java.util.List;
import java.util.stream.Collectors;

public class GestionpaiementsController {

    // ─────────────────────────────────────────────────────────────────────
    // VARIABLES FXML
    // ─────────────────────────────────────────────────────────────────────

    // Pages (une seule visible à la fois)
    @FXML private VBox pagePrincipale;  // liste des paiements
    @FXML private VBox pageFormulaire;  // formulaire ajout/modification (page entière)

    // Stats
    @FXML private Label lblNbPayes, lblNbAVenir, lblTotalPaye;

    // Filtres
    @FXML private TextField txtRecherche;
    @FXML private ComboBox<String> comboStatut, comboTri;

    // Liste
    @FXML private VBox listePaiements;

    // Formulaire — champs de saisie
    @FXML private Label lblTitreForm;
    @FXML private ComboBox<String> comboService;
    @FXML private TextField txtMontant, txtNomTitulaire, txtPrenomTitulaire;
    @FXML private TextField txtNumeroCarte, txtDateExpiration, txtCvv;
    @FXML private DatePicker datePickerPaie;
    @FXML private Button btnStatutPaye, btnStatutAttente;

    // Labels d'erreur (affichés si le champ est vide)
    @FXML private Label errService, errMontant, errNom, errPrenom;
    @FXML private Label errCarte, errExpiration, errCvv, errDate;

    // ─────────────────────────────────────────────────────────────────────
    // ÉTAT
    // ─────────────────────────────────────────────────────────────────────
    private MainController mainController;
    private final PaiementService   paieService = new PaiementService();
    private final AbonnementService aboService  = new AbonnementService();
    private List<Paiement> tous;
    private String statutSelectionne = "À venir";
    private Paiement enEdition = null; // null = ajout, sinon = modification

    // Styles CSS réutilisables
    private static final String CHAMP_OK    = "-fx-background-radius:9;-fx-border-radius:9;-fx-border-color:#e0e8f0;-fx-border-width:1.5;-fx-padding:11 14;-fx-font-size:13px;-fx-background-color:#fafbfc;";
    private static final String CHAMP_ERREUR= "-fx-background-radius:9;-fx-border-radius:9;-fx-border-color:#e74c3c;-fx-border-width:2;-fx-padding:11 14;-fx-font-size:13px;-fx-background-color:#fff5f5;";

    public void setMainController(MainController mc) { this.mainController = mc; }

    // ─────────────────────────────────────────────────────────────────────
    // INITIALISATION
    // ─────────────────────────────────────────────────────────────────────
    @FXML
    public void initialize() {
        comboStatut.setItems(FXCollections.observableArrayList("Tous les statuts","Payé","À venir"));
        comboTri.setItems(FXCollections.observableArrayList("Date ↑","Date ↓","Montant ↑","Montant ↓"));
        // Note : comboMode supprimé — paiement uniquement par carte bancaire
        charger();
    }

    // ─────────────────────────────────────────────────────────────────────
    // NAVIGATION — affiche une page, cache l'autre
    // ─────────────────────────────────────────────────────────────────────
    private void showPage(VBox page) {
        for (VBox v : new VBox[]{pagePrincipale, pageFormulaire}) {
            v.setVisible(false); v.setManaged(false);
        }
        page.setVisible(true); page.setManaged(true);
    }

    // ─────────────────────────────────────────────────────────────────────
    // CHARGEMENT
    // ─────────────────────────────────────────────────────────────────────
    private void charger() {
        tous = paieService.afficher();
        majStats(tous);
        afficher(tous);
    }

    private void majStats(List<Paiement> l) {
        lblNbPayes.setText(String.valueOf(l.stream().filter(p -> "Payé".equals(p.getStatut())).count()));
        lblNbAVenir.setText(String.valueOf(l.stream().filter(p -> "À venir".equals(p.getStatut())).count()));
        lblTotalPaye.setText(String.format("%.3f TND",
                l.stream().filter(p -> "Payé".equals(p.getStatut())).mapToDouble(Paiement::getMontant).sum()));
    }

    private void afficher(List<Paiement> l) {
        listePaiements.getChildren().clear();
        l.forEach(p -> listePaiements.getChildren().add(creerLigne(p)));
    }

    // ─────────────────────────────────────────────────────────────────────
    // FILTRES
    // ─────────────────────────────────────────────────────────────────────
    @FXML
    private void appliquerFiltres() {
        String r  = txtRecherche.getText().toLowerCase().trim();
        String st = comboStatut.getValue();
        String tri = comboTri.getValue();

        List<Paiement> f = tous.stream()
                .filter(p -> r.isEmpty()
                        || getNomService(p.getAbonnementId()).toLowerCase().contains(r)
                        || p.getDatePaiement().toString().contains(r)
                        || p.getStatut().toLowerCase().contains(r))
                .filter(p -> st == null || "Tous les statuts".equals(st) || p.getStatut().equals(st))
                .collect(Collectors.toList());

        if (tri != null) switch (tri) {
            case "Date ↑"    -> f.sort((a, b) -> a.getDatePaiement().compareTo(b.getDatePaiement()));
            case "Date ↓"    -> f.sort((a, b) -> b.getDatePaiement().compareTo(a.getDatePaiement()));
            case "Montant ↑" -> f.sort((a, b) -> Double.compare(a.getMontant(), b.getMontant()));
            case "Montant ↓" -> f.sort((a, b) -> Double.compare(b.getMontant(), a.getMontant()));
        }
        majStats(f); afficher(f);
    }

    @FXML private void rafraichir() {
        txtRecherche.clear(); comboStatut.setValue(null); comboTri.setValue(null); charger();
    }

    // ─────────────────────────────────────────────────────────────────────
    // FORMULAIRE — ouvrir / fermer
    // ─────────────────────────────────────────────────────────────────────

    // Ouvre le FORMULAIRE en mode AJOUT (page entière, plus d'overlay)
    @FXML
    private void ouvrirFormulaire() {
        enEdition = null;
        lblTitreForm.setText("Ajouter un Paiement");
        viderForm();
        reinitialiserErreurs(); // cache tous les messages d'erreur
        chargerServices();
        statutSelectionne = "À venir";
        majToggle();
        showPage(pageFormulaire); // affiche la page formulaire à la place de la liste
    }

    // Ouvre le FORMULAIRE en mode MODIFICATION (pré-rempli)
    private void ouvrirModifier(Paiement p) {
        enEdition = p;
        lblTitreForm.setText("Modifier le Paiement");
        chargerServices();
        reinitialiserErreurs();
        // Pré-remplit les champs avec les données du paiement
        comboService.setValue(getNomService(p.getAbonnementId()));
        txtMontant.setText(String.valueOf(p.getMontant()));
        txtNomTitulaire.setText(safe(p.getNomTitulaire(), ""));
        txtPrenomTitulaire.setText(safe(p.getPrenomTitulaire(), ""));
        txtNumeroCarte.setText(safe(p.getNumeroCarte(), ""));
        txtDateExpiration.setText(safe(p.getDateExpiration(), ""));
        txtCvv.setText(safe(p.getCvv(), ""));
        datePickerPaie.setValue(p.getDatePaiement().toLocalDate());
        statutSelectionne = p.getStatut();
        majToggle();
        showPage(pageFormulaire);
    }

    // Ferme le formulaire et retourne à la liste
    @FXML
    private void fermerFormulaire() {
        enEdition = null;
        showPage(pagePrincipale); // retour à la liste
    }

    // Boutons statut toggle
    @FXML private void setStatutPaye()    { statutSelectionne = "Payé";    majToggle(); }
    @FXML private void setStatutAttente() { statutSelectionne = "À venir"; majToggle(); }

    // Met à jour le style visuel des boutons Payé / À venir
    private void majToggle() {
        boolean p = "Payé".equals(statutSelectionne);
        btnStatutPaye.setStyle(
                "-fx-font-size:13px;-fx-font-weight:bold;-fx-padding:11 30;" +
                        "-fx-background-radius:9 0 0 9;-fx-cursor:hand;-fx-border-radius:9 0 0 9;-fx-border-width:1.5;" +
                        (p ? "-fx-background-color:#e8fff4;-fx-text-fill:#27ae60;-fx-border-color:#27ae60;"
                                : "-fx-background-color:#f5f5f5;-fx-text-fill:#8899aa;-fx-border-color:#dde3ea;"));
        btnStatutAttente.setStyle(
                "-fx-font-size:13px;-fx-padding:11 30;" +
                        "-fx-background-radius:0 9 9 0;-fx-cursor:hand;-fx-border-radius:0 9 9 0;-fx-border-width:1.5;" +
                        (!p ? "-fx-background-color:#fff8e8;-fx-text-fill:#e67e22;-fx-border-color:#e67e22;"
                                : "-fx-background-color:#f5f5f5;-fx-text-fill:#8899aa;-fx-border-color:#dde3ea;"));
    }

    // ─────────────────────────────────────────────────────────────────────
    // VALIDATION — affiche bordure rouge + message si champ vide
    // ─────────────────────────────────────────────────────────────────────

    /**
     * Vérifie tous les champs obligatoires.
     * Si un champ est vide → bordure rouge + label d'erreur visible.
     * @return true si tout est valide, false sinon
     */
    private boolean validerFormulaire() {
        boolean ok = true;

        // Service (ComboBox)
        ok &= validerCombo(comboService, errService);

        // Montant (TextField)
        ok &= validerTextField(txtMontant, errMontant);

        // Nom et Prénom
        ok &= validerTextField(txtNomTitulaire, errNom);
        ok &= validerTextField(txtPrenomTitulaire, errPrenom);

        // Carte
        ok &= validerTextField(txtNumeroCarte, errCarte);
        ok &= validerTextField(txtDateExpiration, errExpiration);
        ok &= validerTextField(txtCvv, errCvv);

        // Date paiement (DatePicker)
        if (datePickerPaie.getValue() == null) {
            errDate.setVisible(true); errDate.setManaged(true);
            ok = false;
        } else {
            errDate.setVisible(false); errDate.setManaged(false);
        }

        return ok;
    }

    // Valide un TextField : rouge si vide, vert si rempli
    private boolean validerTextField(TextField champ, Label errLabel) {
        if (champ.getText() == null || champ.getText().isBlank()) {
            champ.setStyle(CHAMP_ERREUR); // bordure rouge
            errLabel.setVisible(true); errLabel.setManaged(true);
            return false;
        }
        champ.setStyle(CHAMP_OK); // bordure normale
        errLabel.setVisible(false); errLabel.setManaged(false);
        return true;
    }

    // Valide une ComboBox : rouge si rien sélectionné
    private boolean validerCombo(ComboBox<String> combo, Label errLabel) {
        if (combo.getValue() == null) {
            combo.setStyle("-fx-background-radius:9;-fx-border-radius:9;-fx-border-color:#e74c3c;-fx-border-width:2;-fx-font-size:13px;");
            errLabel.setVisible(true); errLabel.setManaged(true);
            return false;
        }
        combo.setStyle("-fx-background-radius:9;-fx-font-size:13px;-fx-border-color:#e0e8f0;-fx-border-width:1.5;-fx-border-radius:9;");
        errLabel.setVisible(false); errLabel.setManaged(false);
        return true;
    }

    // Remet tous les champs en état normal (cache les erreurs)
    private void reinitialiserErreurs() {
        for (TextField tf : new TextField[]{txtMontant, txtNomTitulaire, txtPrenomTitulaire, txtNumeroCarte, txtDateExpiration, txtCvv})
            tf.setStyle(CHAMP_OK);
        comboService.setStyle("-fx-background-radius:9;-fx-font-size:13px;-fx-border-color:#e0e8f0;-fx-border-width:1.5;-fx-border-radius:9;");
        for (Label l : new Label[]{errService, errMontant, errNom, errPrenom, errCarte, errExpiration, errCvv, errDate}) {
            l.setVisible(false); l.setManaged(false);
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // SAUVEGARDE (CREATE / UPDATE)
    // ─────────────────────────────────────────────────────────────────────
    @FXML
    private void sauvegarder() {
        // Validation : si un champ est vide → affiche erreurs et stop
        if (!validerFormulaire()) return;

        try {
            double montant = Double.parseDouble(txtMontant.getText());
            Abonnement abo = aboService.afficher().stream()
                    .filter(a -> a.getNom().equals(comboService.getValue()))
                    .findFirst().orElse(null);
            if (abo == null) { alerte(Alert.AlertType.ERROR, "❌ Service introuvable !"); return; }

            // Le mode de paiement est toujours "💳 Carte Bancaire" (plus de ComboBox)
            String mode = "💳 Carte Bancaire";

            if (enEdition == null) {
                // MODE AJOUT — INSERT SQL
                paieService.ajouter(new Paiement(
                        montant, Date.valueOf(datePickerPaie.getValue()), statutSelectionne, abo.getId(),
                        txtNomTitulaire.getText(), txtPrenomTitulaire.getText(),
                        mode, txtNumeroCarte.getText(), txtDateExpiration.getText(), txtCvv.getText()
                ));
                alerte(Alert.AlertType.INFORMATION, "✅ Paiement ajouté !");
            } else {
                // MODE MODIFICATION — UPDATE SQL
                enEdition.setAbonnementId(abo.getId()); enEdition.setMontant(montant);
                enEdition.setDatePaiement(Date.valueOf(datePickerPaie.getValue())); enEdition.setStatut(statutSelectionne);
                enEdition.setNomTitulaire(txtNomTitulaire.getText()); enEdition.setPrenomTitulaire(txtPrenomTitulaire.getText());
                enEdition.setModePaiement(mode); enEdition.setNumeroCarte(txtNumeroCarte.getText());
                enEdition.setDateExpiration(txtDateExpiration.getText()); enEdition.setCvv(txtCvv.getText());
                paieService.modifier(enEdition);
                alerte(Alert.AlertType.INFORMATION, "✅ Paiement modifié !");
            }
            fermerFormulaire(); charger();
        } catch (NumberFormatException e) {
            // Montant n'est pas un nombre valide → marque le champ en rouge
            txtMontant.setStyle(CHAMP_ERREUR);
            errMontant.setText("⚠ Entrez un nombre valide (ex: 29.900)");
            errMontant.setVisible(true); errMontant.setManaged(true);
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // SUPPRESSION
    // ─────────────────────────────────────────────────────────────────────
    private void supprimerPaiement(Paiement p) {
        Alert c = new Alert(Alert.AlertType.CONFIRMATION); c.setHeaderText(null);
        c.setContentText("Supprimer ce paiement de " + String.format("%.3f TND", p.getMontant()) + " ?");
        c.showAndWait().ifPresent(btn -> { if (btn == ButtonType.OK) { paieService.supprimer(p.getId()); charger(); } });
    }

    // ─────────────────────────────────────────────────────────────────────
    // LIGNE EXPANDABLE (tableau)
    // ─────────────────────────────────────────────────────────────────────
    private VBox creerLigne(Paiement p) {
        boolean isPaye = "Payé".equals(p.getStatut());
        String nom = getNomService(p.getAbonnementId());

        // Ligne résumé (toujours visible)
        HBox ligne = new HBox(0); ligne.setAlignment(Pos.CENTER_LEFT);
        ligne.setStyle("-fx-background-color:white;-fx-background-radius:12;-fx-padding:14 20;-fx-cursor:hand;-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.04),6,0,0,2);");

        HBox svc = new HBox(8); svc.setPrefWidth(190); svc.setAlignment(Pos.CENTER_LEFT);
        svc.getChildren().addAll(
                lbl(isPaye ? "✅" : "⏳", "-fx-font-size:14px;"),
                lbl(nom, "-fx-font-size:13px;-fx-font-weight:bold;-fx-text-fill:#0f1f3d;"));

        // Colonnes à largeur fixe pour aligner comme un tableau
        Label dateLbl    = lblW(p.getDatePaiement().toString(), 140, "-fx-font-size:12px;-fx-text-fill:#556677;");
        Label montantLbl = lblW(String.format("%.3f TND", p.getMontant()), 140,
                "-fx-font-size:13px;-fx-font-weight:bold;-fx-text-fill:" + (isPaye ? "#27ae60" : "#1a3a7a") + ";");
        Label stLbl = lblW(isPaye ? "✅ Payé" : "⏳ À venir", 130,
                "-fx-background-color:" + (isPaye ? "#e8fff4" : "#fff8e8") + ";-fx-text-fill:" + (isPaye ? "#27ae60" : "#e67e22") +
                        ";-fx-font-size:11px;-fx-font-weight:bold;-fx-padding:4 12;-fx-background-radius:20;");

        Button bE = actionBtn("✏️", "#e8eeff"); bE.setOnAction(e -> { e.consume(); ouvrirModifier(p); });
        Button bD = actionBtn("🗑", "#fff0f0");  bD.setStyle(bD.getStyle() + "-fx-text-fill:#e74c3c;");
        bD.setOnAction(e -> { e.consume(); supprimerPaiement(p); });
        Label fleche = lbl("›", "-fx-font-size:20px;-fx-text-fill:#1a3a7a;-fx-font-weight:bold;-fx-cursor:hand;-fx-padding:0 0 0 5;");

        HBox actions = new HBox(8); HBox.setHgrow(actions, Priority.ALWAYS); actions.setAlignment(Pos.CENTER_RIGHT);
        actions.getChildren().addAll(bE, bD, fleche);
        ligne.getChildren().addAll(svc, dateLbl, montantLbl, stLbl, actions);

        // Panneau détails (caché, s'ouvre au clic)
        VBox details = new VBox(0); details.setVisible(false); details.setManaged(false);
        details.setStyle("-fx-background-color:#f8faff;-fx-padding:18 25;-fx-background-radius:0 0 12 12;-fx-border-color:#1a3a7a;-fx-border-width:0 1.5 1.5 1.5;-fx-border-radius:0 0 12 12;");
        String titulaire = (safe(p.getNomTitulaire(), "") + " " + safe(p.getPrenomTitulaire(), "")).trim();
        HBox row1 = new HBox(30);
        row1.getChildren().addAll(dBox("SERVICE", nom), dBox("DATE", p.getDatePaiement().toString()),
                dBox("MONTANT", String.format("%.3f TND", p.getMontant())), dBox("STATUT", p.getStatut()));
        HBox row2 = new HBox(30); row2.setStyle("-fx-padding:10 0 0 0;");
        row2.getChildren().addAll(dBox("TITULAIRE", titulaire.isEmpty() ? "—" : titulaire),
                dBox("N° CARTE", maskCarte(p.getNumeroCarte())),
                dBox("EXPIRATION", safe(p.getDateExpiration(), "—")), dBox("CVV", safe(p.getCvv(), "—")));
        details.getChildren().addAll(row1, new Separator(), row2);

        // Toggle ouverture/fermeture
        final boolean[] open = {false};
        Runnable toggle = () -> {
            open[0] = !open[0];
            details.setVisible(open[0]); details.setManaged(open[0]);
            fleche.setText(open[0] ? "∨" : "›");
            ligne.setStyle(open[0]
                    ? "-fx-background-color:white;-fx-padding:14 20;-fx-background-radius:12 12 0 0;-fx-cursor:hand;-fx-border-color:#1a3a7a;-fx-border-width:1.5 1.5 0 1.5;-fx-border-radius:12 12 0 0;"
                    : "-fx-background-color:white;-fx-background-radius:12;-fx-padding:14 20;-fx-cursor:hand;-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.04),6,0,0,2);");
        };
        fleche.setOnMouseClicked(e -> { e.consume(); toggle.run(); });
        ligne.setOnMouseClicked(e -> toggle.run());
        ligne.setOnMouseEntered(e -> { if (!open[0]) ligne.setStyle("-fx-background-color:#f0f4ff;-fx-background-radius:12;-fx-padding:14 20;-fx-cursor:hand;"); });
        ligne.setOnMouseExited(e  -> { if (!open[0]) ligne.setStyle("-fx-background-color:white;-fx-background-radius:12;-fx-padding:14 20;-fx-cursor:hand;-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.04),6,0,0,2);"); });

        VBox container = new VBox(0); container.getChildren().addAll(ligne, details);
        return container;
    }

    // ─────────────────────────────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────────────────────────────
    private void chargerServices() {
        comboService.setItems(FXCollections.observableArrayList(
                aboService.afficher().stream().map(Abonnement::getNom).distinct().sorted().toList()));
    }

    private void viderForm() {
        comboService.setValue(null); txtMontant.clear();
        txtNomTitulaire.clear(); txtPrenomTitulaire.clear();
        txtNumeroCarte.clear(); txtDateExpiration.clear();
        txtCvv.clear(); datePickerPaie.setValue(null);
    }

    private String getNomService(int id) {
        return aboService.afficher().stream().filter(a -> a.getId() == id)
                .map(Abonnement::getNom).findFirst().orElse("Service #" + id);
    }

    private String safe(String s, String def) { return (s == null || s.isBlank()) ? def : s; }

    private String maskCarte(String n) {
        if (n == null || n.isBlank()) return "—";
        return n.length() >= 4 ? "**** **** **** " + n.substring(n.length() - 4) : n;
    }

    private void alerte(Alert.AlertType t, String m) { Alert a = new Alert(t); a.setHeaderText(null); a.setContentText(m); a.showAndWait(); }
    private Label lbl(String t, String s)             { Label l = new Label(t); l.setStyle(s); return l; }
    private Label lblW(String t, double w, String s)  { Label l = lbl(t, s); l.setPrefWidth(w); return l; }
    private Button actionBtn(String t, String bg)     {
        Button b = new Button(t); b.setStyle("-fx-background-color:" + bg + ";-fx-font-size:12px;-fx-padding:6 10;-fx-background-radius:7;-fx-cursor:hand;");
        b.setTooltip(new Tooltip(t.equals("✏️") ? "Modifier" : "Supprimer")); return b;
    }
    private VBox dBox(String label, String val) {
        VBox v = new VBox(5);
        v.getChildren().addAll(lbl(label, "-fx-font-size:9px;-fx-font-weight:bold;-fx-text-fill:#8899aa;"),
                lbl(val, "-fx-font-size:12px;-fx-font-weight:bold;-fx-text-fill:#0f1f3d;")); return v;
    }

    @FXML private void retourAbonnements() { if (mainController != null) mainController.switchAbonnements(); }
}