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

    // ===== FXML =====
    @FXML private Label lblNbPayes, lblNbAVenir, lblTotalPaye, lblTitreForm;
    @FXML private TextField txtRecherche, txtMontant, txtNomTitulaire, txtPrenomTitulaire, txtNumeroCarte, txtDateExpiration, txtCvv;
    @FXML private ComboBox<String> comboStatut, comboTri, comboService, comboMode;
    @FXML private VBox listePaiements;
    @FXML private StackPane overlayForm;
    @FXML private DatePicker datePickerPaie;
    @FXML private Button btnStatutPaye, btnStatutAttente;

    // ===== ÉTAT =====
    private MainController mainController;
    private final PaiementService   paieService = new PaiementService();
    private final AbonnementService aboService  = new AbonnementService();
    private List<Paiement> tous;
    private String statutSel = "À venir";
    private Paiement enEdition = null;

    public void setMainController(MainController mc) { this.mainController = mc; }

    // ===== STYLES =====
    private static final String S_LIGNE =
            "-fx-background-color:white;-fx-background-radius:12;-fx-padding:14 20;-fx-cursor:hand;-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.04),6,0,0,2);";
    private static final String S_LIGNE_OPEN =
            "-fx-background-color:white;-fx-padding:14 20;-fx-background-radius:12 12 0 0;-fx-cursor:hand;-fx-border-color:#1a3a7a;-fx-border-width:1.5 1.5 0 1.5;-fx-border-radius:12 12 0 0;";

    // ===== INIT =====
    @FXML public void initialize() {
        comboStatut.setItems(FXCollections.observableArrayList("Tous les statuts","Payé","À venir"));
        comboTri.setItems(FXCollections.observableArrayList("Date ↑","Date ↓","Montant ↑","Montant ↓"));
        comboMode.setItems(FXCollections.observableArrayList("💳 Carte Bancaire","💵 Espèces","📲 Virement","💰 Wallet","🔄 Prélèvement"));
        charger();
    }

    // ===== DONNÉES =====
    private void charger() { tous = paieService.afficher(); majStats(tous); afficher(tous); }

    private void majStats(List<Paiement> l) {
        lblNbPayes.setText(String.valueOf(l.stream().filter(p->"Payé".equals(p.getStatut())).count()));
        lblNbAVenir.setText(String.valueOf(l.stream().filter(p->"À venir".equals(p.getStatut())).count()));
        lblTotalPaye.setText(String.format("%.3f TND", l.stream().filter(p->"Payé".equals(p.getStatut())).mapToDouble(Paiement::getMontant).sum()));
    }

    private void afficher(List<Paiement> l) {
        listePaiements.getChildren().clear();
        l.forEach(p -> listePaiements.getChildren().add(creerLigne(p)));
    }

    // ===== FILTRES =====
    @FXML private void appliquerFiltres() {
        String r=txtRecherche.getText().toLowerCase().trim(), st=comboStatut.getValue(), tri=comboTri.getValue();
        List<Paiement> f = tous.stream()
                .filter(p -> r.isEmpty() || getNom(p.getAbonnementId()).toLowerCase().contains(r) || p.getDatePaiement().toString().contains(r) || p.getStatut().toLowerCase().contains(r))
                .filter(p -> st==null || "Tous les statuts".equals(st) || p.getStatut().equals(st))
                .collect(Collectors.toList());
        if (tri!=null) switch(tri) {
            case "Date ↑"    -> f.sort((a,b)->a.getDatePaiement().compareTo(b.getDatePaiement()));
            case "Date ↓"    -> f.sort((a,b)->b.getDatePaiement().compareTo(a.getDatePaiement()));
            case "Montant ↑" -> f.sort((a,b)->Double.compare(a.getMontant(),b.getMontant()));
            case "Montant ↓" -> f.sort((a,b)->Double.compare(b.getMontant(),a.getMontant()));
        }
        majStats(f); afficher(f);
    }
    @FXML private void rafraichir() { txtRecherche.clear(); comboStatut.setValue(null); comboTri.setValue(null); charger(); }

    // ===== LIGNE EXPANDABLE =====
    private VBox creerLigne(Paiement p) {
        boolean isPaye = "Payé".equals(p.getStatut());
        String nom = getNom(p.getAbonnementId());

        // Ligne principale
        HBox ligne = new HBox(0); ligne.setAlignment(Pos.CENTER_LEFT); ligne.setStyle(S_LIGNE);

        HBox svc = new HBox(8); svc.setPrefWidth(190); svc.setAlignment(Pos.CENTER_LEFT);
        svc.getChildren().addAll(lbl(isPaye?"✅":"⏳","-fx-font-size:14px;"), lbl(nom,"-fx-font-size:13px;-fx-font-weight:bold;-fx-text-fill:#0f1f3d;"));

        Label dateLbl    = lblW(p.getDatePaiement().toString(), 140, "-fx-font-size:12px;-fx-text-fill:#556677;");
        Label montantLbl = lblW(String.format("%.3f TND",p.getMontant()), 140, "-fx-font-size:13px;-fx-font-weight:bold;-fx-text-fill:"+(isPaye?"#27ae60":"#1a3a7a")+";");
        Label modeLbl    = lblW(safe(p.getModePaiement(),"—"), 140, "-fx-font-size:11px;-fx-text-fill:#556677;");
        Label stLbl      = lblW(isPaye?"✅ Payé":"⏳ À venir", 130,
                "-fx-background-color:"+(isPaye?"#e8fff4":"#fff8e8")+";-fx-text-fill:"+(isPaye?"#27ae60":"#e67e22")+
                        ";-fx-font-size:11px;-fx-font-weight:bold;-fx-padding:4 12;-fx-background-radius:20;");

        Button btnE=actionBtn("✏️","#e8eeff"); btnE.setOnAction(e->{e.consume();ouvrirModifier(p);});
        Button btnD=actionBtn("🗑","#fff0f0");  btnD.setStyle(btnD.getStyle()+"-fx-text-fill:#e74c3c;");
        btnD.setOnAction(e->{e.consume();supprimerPaiement(p);});
        Label fleche=lbl("›","-fx-font-size:20px;-fx-text-fill:#1a3a7a;-fx-font-weight:bold;-fx-cursor:hand;-fx-padding:0 0 0 5;");

        HBox actions=new HBox(8); HBox.setHgrow(actions,Priority.ALWAYS); actions.setAlignment(Pos.CENTER_RIGHT);
        actions.getChildren().addAll(btnE,btnD,fleche);
        ligne.getChildren().addAll(svc,dateLbl,montantLbl,modeLbl,stLbl,actions);

        // Panneau détails
        VBox details = new VBox(0); details.setVisible(false); details.setManaged(false);
        details.setStyle("-fx-background-color:#f8faff;-fx-padding:18 25;-fx-background-radius:0 0 12 12;-fx-border-color:#1a3a7a;-fx-border-width:0 1.5 1.5 1.5;-fx-border-radius:0 0 12 12;");
        String titulaire = (safe(p.getNomTitulaire(),"")+' '+safe(p.getPrenomTitulaire(),"")).trim();
        HBox row1=new HBox(30); row1.getChildren().addAll(dBox("SERVICE",nom),dBox("DATE",p.getDatePaiement().toString()),dBox("MONTANT",String.format("%.3f TND",p.getMontant())),dBox("STATUT",p.getStatut()),dBox("MODE",safe(p.getModePaiement(),"—")));
        HBox row2=new HBox(30); row2.setStyle("-fx-padding:10 0 0 0;"); row2.getChildren().addAll(dBox("TITULAIRE",titulaire.isEmpty()?"—":titulaire),dBox("N° CARTE",maskCarte(p.getNumeroCarte())),dBox("EXPIRATION",safe(p.getDateExpiration(),"—")),dBox("CVV",safe(p.getCvv(),"—")));
        details.getChildren().addAll(row1,new Separator(),row2);

        // Toggle
        final boolean[] open={false};
        Runnable toggle=()->{
            open[0]=!open[0]; details.setVisible(open[0]); details.setManaged(open[0]);
            fleche.setText(open[0]?"∨":"›");
            ligne.setStyle(open[0]?S_LIGNE_OPEN:S_LIGNE);
        };
        fleche.setOnMouseClicked(e->{e.consume();toggle.run();});
        ligne.setOnMouseClicked(e->toggle.run());
        ligne.setOnMouseEntered(e->{if(!open[0])ligne.setStyle("-fx-background-color:#f0f4ff;-fx-background-radius:12;-fx-padding:14 20;-fx-cursor:hand;");});
        ligne.setOnMouseExited(e->{if(!open[0])ligne.setStyle(S_LIGNE);});

        VBox container=new VBox(0); container.getChildren().addAll(ligne,details); return container;
    }

    // ===== FORMULAIRE =====
    @FXML private void ouvrirFormulaire() {
        enEdition=null; lblTitreForm.setText("Ajouter un Paiement"); viderForm(); chargerServices(); statutSel="À venir"; majToggle();
        overlayForm.setVisible(true); overlayForm.setManaged(true);
    }
    private void ouvrirModifier(Paiement p) {
        enEdition=p; lblTitreForm.setText("Modifier le Paiement"); chargerServices();
        comboService.setValue(getNom(p.getAbonnementId())); txtMontant.setText(String.valueOf(p.getMontant()));
        txtNomTitulaire.setText(safe(p.getNomTitulaire(),"")); txtPrenomTitulaire.setText(safe(p.getPrenomTitulaire(),""));
        comboMode.setValue(p.getModePaiement()); txtNumeroCarte.setText(safe(p.getNumeroCarte(),""));
        txtDateExpiration.setText(safe(p.getDateExpiration(),"")); txtCvv.setText(safe(p.getCvv(),""));
        datePickerPaie.setValue(p.getDatePaiement().toLocalDate()); statutSel=p.getStatut(); majToggle();
        overlayForm.setVisible(true); overlayForm.setManaged(true);
    }
    @FXML private void fermerFormulaire()  { overlayForm.setVisible(false); overlayForm.setManaged(false); enEdition=null; }
    @FXML private void setStatutPaye()     { statutSel="Payé";    majToggle(); }
    @FXML private void setStatutAttente()  { statutSel="À venir"; majToggle(); }

    private void majToggle() {
        boolean p="Payé".equals(statutSel);
        btnStatutPaye.setStyle("-fx-font-size:13px;-fx-font-weight:bold;-fx-padding:11 30;-fx-background-radius:9 0 0 9;-fx-cursor:hand;-fx-border-radius:9 0 0 9;-fx-border-width:1.5;"+(p?"-fx-background-color:#e8fff4;-fx-text-fill:#27ae60;-fx-border-color:#27ae60;":"-fx-background-color:#f5f5f5;-fx-text-fill:#8899aa;-fx-border-color:#dde3ea;"));
        btnStatutAttente.setStyle("-fx-font-size:13px;-fx-padding:11 30;-fx-background-radius:0 9 9 0;-fx-cursor:hand;-fx-border-radius:0 9 9 0;-fx-border-width:1.5;"+(!p?"-fx-background-color:#fff8e8;-fx-text-fill:#e67e22;-fx-border-color:#e67e22;":"-fx-background-color:#f5f5f5;-fx-text-fill:#8899aa;-fx-border-color:#dde3ea;"));
    }

    @FXML private void sauvegarder() {
        if (comboService.getValue()==null||txtMontant.getText().isEmpty()||datePickerPaie.getValue()==null) {
            alerte(Alert.AlertType.WARNING,"⚠️ Service, montant et date sont obligatoires !"); return;
        }
        try {
            double montant=Double.parseDouble(txtMontant.getText());
            Abonnement abo=aboService.afficher().stream().filter(a->a.getNom().equals(comboService.getValue())).findFirst().orElse(null);
            if(abo==null){alerte(Alert.AlertType.ERROR,"❌ Service introuvable !");return;}
            String mode=comboMode.getValue()!=null?comboMode.getValue():"";
            if(enEdition==null) {
                paieService.ajouter(new Paiement(montant,Date.valueOf(datePickerPaie.getValue()),statutSel,abo.getId(),
                        txtNomTitulaire.getText(),txtPrenomTitulaire.getText(),mode,txtNumeroCarte.getText(),txtDateExpiration.getText(),txtCvv.getText()));
                alerte(Alert.AlertType.INFORMATION,"✅ Paiement ajouté !");
            } else {
                enEdition.setAbonnementId(abo.getId()); enEdition.setMontant(montant);
                enEdition.setDatePaiement(Date.valueOf(datePickerPaie.getValue())); enEdition.setStatut(statutSel);
                enEdition.setNomTitulaire(txtNomTitulaire.getText()); enEdition.setPrenomTitulaire(txtPrenomTitulaire.getText());
                enEdition.setModePaiement(mode); enEdition.setNumeroCarte(txtNumeroCarte.getText());
                enEdition.setDateExpiration(txtDateExpiration.getText()); enEdition.setCvv(txtCvv.getText());
                paieService.modifier(enEdition);
                alerte(Alert.AlertType.INFORMATION,"✅ Paiement modifié !");
            }
            fermerFormulaire(); charger();
        } catch(NumberFormatException e){alerte(Alert.AlertType.ERROR,"❌ Montant invalide !");}
    }

    private void supprimerPaiement(Paiement p) {
        Alert c=new Alert(Alert.AlertType.CONFIRMATION); c.setHeaderText(null);
        c.setContentText("Supprimer ce paiement de "+String.format("%.3f TND",p.getMontant())+" ?");
        c.showAndWait().ifPresent(btn->{if(btn==ButtonType.OK){paieService.supprimer(p.getId());charger();}});
    }

    // ===== HELPERS =====
    private void chargerServices() { comboService.setItems(FXCollections.observableArrayList(aboService.afficher().stream().map(Abonnement::getNom).distinct().sorted().toList())); }
    private void viderForm()       { comboService.setValue(null);txtMontant.clear();txtNomTitulaire.clear();txtPrenomTitulaire.clear();comboMode.setValue(null);txtNumeroCarte.clear();txtDateExpiration.clear();txtCvv.clear();datePickerPaie.setValue(null); }
    private String getNom(int id)  { return aboService.afficher().stream().filter(a->a.getId()==id).map(Abonnement::getNom).findFirst().orElse("Service #"+id); }
    private String safe(String s, String d) { return (s==null||s.isBlank())?d:s; }
    private String maskCarte(String n)      { if(n==null||n.isBlank())return "—"; return n.length()>=4?"**** **** **** "+n.substring(n.length()-4):n; }
    private void alerte(Alert.AlertType t, String m) { Alert a=new Alert(t);a.setHeaderText(null);a.setContentText(m);a.showAndWait(); }
    private Label lbl(String t, String s)             { Label l=new Label(t);l.setStyle(s);return l; }
    private Label lblW(String t, double w, String s)  { Label l=lbl(t,s);l.setPrefWidth(w);return l; }
    private Button actionBtn(String t, String bg)     { Button b=new Button(t);b.setStyle("-fx-background-color:"+bg+";-fx-font-size:12px;-fx-padding:6 10;-fx-background-radius:7;-fx-cursor:hand;");b.setTooltip(new Tooltip(t.equals("✏️")?"Modifier":"Supprimer"));return b; }
    private VBox dBox(String label, String val)       { VBox v=new VBox(5);v.getChildren().addAll(lbl(label,"-fx-font-size:9px;-fx-font-weight:bold;-fx-text-fill:#8899aa;"),lbl(val,"-fx-font-size:12px;-fx-font-weight:bold;-fx-text-fill:#0f1f3d;"));return v; }
    @FXML private void retourAbonnements() { if(mainController!=null)mainController.switchAbonnements(); }
}