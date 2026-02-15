package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
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
import java.util.*;
import java.util.stream.Collectors;

public class GestionabonnementsController {

    // ===== FXML =====
    @FXML private VBox pageListe, pageDetailService, pageFormulaire;
    @FXML private Label lblTotalMensuel, lblServiceNom, lblServiceSousTitre;
    @FXML private TextField txtRecherche, txtNom, txtPrix;
    @FXML private ComboBox<String> comboFiltreCategorie, comboTri, comboCategorie, comboFrequence;
    @FXML private VBox listeServices, listeAbonnementsService;
    @FXML private HBox statsService;
    @FXML private DatePicker datePicker;

    // ===== ÉTAT =====
    private MainController mainController;
    private final AbonnementService aboService = new AbonnementService();
    private final PaiementService paieService  = new PaiementService();
    private List<Abonnement> tous;
    private String nomServiceSelectionne;

    public void setMainController(MainController mc) { this.mainController = mc; }

    // ===== STYLES CONSTANTS =====
    private static final String S_CARD =
            "-fx-background-color: white; -fx-padding: 18 25; -fx-background-radius: 14; " +
                    "-fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 8, 0, 0, 2);";
    private static final String S_CARD_HOVER =
            "-fx-background-color: #f0f4ff; -fx-padding: 18 25; -fx-background-radius: 14; " +
                    "-fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(26,58,122,0.12), 14, 0, 0, 4); " +
                    "-fx-border-color: #1a3a7a; -fx-border-width: 1.5; -fx-border-radius: 14;";
    private static final String S_FIELD =
            "-fx-background-radius: 9; -fx-border-radius: 9; -fx-border-color: #e0e8f0; " +
                    "-fx-border-width: 1.5; -fx-padding: 11 14; -fx-font-size: 13px; -fx-background-color: #fafbfc;";

    // ===== INIT =====
    @FXML public void initialize() {
        comboFiltreCategorie.setItems(FXCollections.observableArrayList(
                "Toutes","🎬 Streaming","🎵 Musique","🏋️ Sport","📚 Éducation","🎮 Jeux","☁️ Cloud","🔧 Autre"));
        comboCategorie.setItems(FXCollections.observableArrayList(
                "🎬 Streaming","🎵 Musique","🏋️ Sport","📚 Éducation","🎮 Jeux","☁️ Cloud","🔧 Autre"));
        comboTri.setItems(FXCollections.observableArrayList("Prix croissant ↑","Prix décroissant ↓"));
        comboFrequence.setItems(FXCollections.observableArrayList("Mensuel","Annuel"));
        chargerTout();
    }

    // ===== NAVIGATION =====
    private void showPage(VBox p) {
        for (VBox v : new VBox[]{pageListe, pageDetailService, pageFormulaire}) { v.setVisible(false); v.setManaged(false); }
        p.setVisible(true); p.setManaged(true);
    }
    @FXML private void ouvrirFormulaire()  { vider(); showPage(pageFormulaire); }
    @FXML private void retourListe()       { chargerTout(); showPage(pageListe); }
    @FXML private void ouvrirPaiements()   { if (mainController != null) mainController.switchPaiements(); }
    @FXML private void rafraichir()        { txtRecherche.clear(); comboFiltreCategorie.setValue(null); comboTri.setValue(null); chargerTout(); }

    // ===== DONNÉES =====
    private void chargerTout() { tous = aboService.afficher(); afficherListe(tous); majTotal(tous); }

    private void majTotal(List<Abonnement> liste) {
        lblTotalMensuel.setText(String.format("%.3f TND",
                liste.stream().filter(a -> "Mensuel".equals(a.getFrequence())).mapToDouble(Abonnement::getPrix).sum()));
    }

    @FXML private void appliquerFiltres() {
        String r = txtRecherche.getText().toLowerCase().trim();
        String c = comboFiltreCategorie.getValue();
        List<Abonnement> f = tous.stream()
                .filter(a -> r.isEmpty() || a.getNom().toLowerCase().contains(r) || a.getCategorie().toLowerCase().contains(r))
                .filter(a -> c == null || "Toutes".equals(c) || a.getCategorie().equals(c))
                .collect(Collectors.toList());
        afficherListe(f); majTotal(f);
    }

    private void afficherListe(List<Abonnement> liste) {
        listeServices.getChildren().clear();
        Map<String, List<Abonnement>> map = liste.stream()
                .collect(Collectors.groupingBy(a -> a.getNom().trim().toLowerCase()));
        if (map.isEmpty()) {
            Label v = lbl("Aucun abonnement. Cliquez sur + Ajouter !", "-fx-text-fill:#8899aa;-fx-font-size:14px;-fx-padding:30;");
            listeServices.getChildren().add(v); return;
        }
        List<Map.Entry<String, List<Abonnement>>> entries = new ArrayList<>(map.entrySet());
        String tri = comboTri.getValue();
        if ("Prix croissant ↑".equals(tri))  entries.sort(Comparator.comparingDouble(e -> e.getValue().stream().mapToDouble(Abonnement::getPrix).sum()));
        if ("Prix décroissant ↓".equals(tri)) entries.sort((e1,e2) -> Double.compare(e2.getValue().stream().mapToDouble(Abonnement::getPrix).sum(), e1.getValue().stream().mapToDouble(Abonnement::getPrix).sum()));
        entries.forEach(e -> listeServices.getChildren().add(creerLigne(e.getValue().get(0), e.getValue())));
    }

    // ===== LIGNE SERVICE =====
    private HBox creerLigne(Abonnement rep, List<Abonnement> abos) {
        HBox ligne = new HBox(18); ligne.setAlignment(Pos.CENTER_LEFT); ligne.setStyle(S_CARD);

        VBox infos = new VBox(4); HBox.setHgrow(infos, Priority.ALWAYS);
        HBox top = new HBox(10); top.setAlignment(Pos.CENTER_LEFT);
        Label nom = lbl(rep.getNom(), "-fx-font-size:15px;-fx-font-weight:bold;-fx-text-fill:#0f1f3d;");
        String cat = rep.getCategorie(); String catNom = cat.contains(" ") ? cat.substring(cat.indexOf(" ")+1) : cat;
        Label badge = lbl(catNom, "-fx-background-color:#e8eeff;-fx-text-fill:#1a3a7a;-fx-font-size:10px;-fx-font-weight:bold;-fx-padding:3 10;-fx-background-radius:20;");
        top.getChildren().addAll(nom, badge);
        long nbP = abos.stream().flatMap(a -> paieService.afficher().stream().filter(p -> p.getAbonnementId()==a.getId() && "Payé".equals(p.getStatut()))).count();
        infos.getChildren().addAll(top, lbl(abos.size()+" abonnement(s)  •  "+nbP+" paiements réglés", "-fx-font-size:11px;-fx-text-fill:#8899aa;"));

        boolean actif = abos.stream().anyMatch(Abonnement::isActif);
        Label statut = lbl(actif?"● Actif":"● Inactif", "-fx-font-size:11px;-fx-font-weight:bold;-fx-text-fill:"+(actif?"#27ae60":"#e67e22")+";");

        double prixM = abos.stream().filter(a->"Mensuel".equals(a.getFrequence())).mapToDouble(Abonnement::getPrix).sum()
                + abos.stream().filter(a->"Annuel".equals(a.getFrequence())).mapToDouble(a->a.getPrix()/12.0).sum();
        VBox prixBox = new VBox(2); prixBox.setAlignment(Pos.CENTER_RIGHT);
        prixBox.getChildren().addAll(lbl(String.format("%.3f",prixM), "-fx-font-size:20px;-fx-font-weight:bold;-fx-text-fill:#1a3a7a;"),
                lbl("TND/mois", "-fx-font-size:10px;-fx-text-fill:#8899aa;"));

        Label fleche = lbl("›", "-fx-font-size:24px;-fx-text-fill:#1a3a7a;-fx-font-weight:bold;");
        ligne.getChildren().addAll(creerIcone(rep.getNom()), infos, statut, prixBox, fleche);
        ligne.setOnMouseEntered(e -> ligne.setStyle(S_CARD_HOVER));
        ligne.setOnMouseExited(e  -> ligne.setStyle(S_CARD));
        ligne.setOnMouseClicked(e -> ouvrirDetail(rep.getNom(), abos));
        return ligne;
    }

    // ===== DÉTAIL SERVICE =====
    private void ouvrirDetail(String nom, List<Abonnement> abos) {
        nomServiceSelectionne = nom;
        lblServiceNom.setText(nom); lblServiceSousTitre.setText(abos.size()+" abonnement(s)");
        statsService.getChildren().clear();
        double total = abos.stream().mapToDouble(a -> "Mensuel".equals(a.getFrequence()) ? a.getPrix() : a.getPrix()/12.0).sum();
        long nbPaie  = abos.stream().mapToLong(a -> paieService.afficher().stream().filter(p->p.getAbonnementId()==a.getId()).count()).sum();
        statsService.getChildren().addAll(
                statCard("💰 Total mensuel", String.format("%.3f TND",total), "#1a3a7a"),
                statCard("📋 Abonnements",   String.valueOf(abos.size()),      "#0f1f3d"),
                statCard("🧾 Paiements",     String.valueOf(nbPaie),           "#27ae60"));
        listeAbonnementsService.getChildren().clear();
        aboService.afficher().stream().filter(a -> a.getNom().trim().equalsIgnoreCase(nom.trim()))
                .forEach(a -> listeAbonnementsService.getChildren().add(creerCarteAbo(a)));
        showPage(pageDetailService);
    }

    private VBox creerCarteAbo(Abonnement a) {
        VBox carte = new VBox(12);
        carte.setStyle("-fx-background-color:white;-fx-padding:20 25;-fx-background-radius:14;-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.07),10,0,0,3);");

        HBox top = new HBox(12); top.setAlignment(Pos.CENTER_LEFT);
        Label nom = lbl(a.getNom()+"  —  "+a.getFrequence(), "-fx-font-size:15px;-fx-font-weight:bold;-fx-text-fill:#0f1f3d;");
        HBox.setHgrow(nom, Priority.ALWAYS);
        Label st = lbl(a.isActif()?"✅ Actif":"❌ Inactif",
                "-fx-background-color:"+(a.isActif()?"#e8fff4":"#fff0f0")+";-fx-text-fill:"+(a.isActif()?"#27ae60":"#e74c3c")+
                        ";-fx-font-size:11px;-fx-font-weight:bold;-fx-padding:5 14;-fx-background-radius:20;");
        top.getChildren().addAll(nom, st);

        HBox infos = new HBox(40);
        infos.getChildren().addAll(infoBox("PRIX", String.format("%.3f TND",a.getPrix())),
                infoBox("DATE DÉBUT", a.getDateDebut().toString()),
                infoBox("COÛT ANNUEL", String.format("%.3f TND",a.getPrix()*12)));

        List<Paiement> paies = paieService.afficher().stream().filter(p->p.getAbonnementId()==a.getId()).toList();
        long nbPaye = paies.stream().filter(p->"Payé".equals(p.getStatut())).count();
        long nbAv   = paies.stream().filter(p->"À venir".equals(p.getStatut())).count();
        HBox resume = new HBox(10);
        resume.setStyle("-fx-background-color:#f0f4f8;-fx-padding:10 18;-fx-background-radius:8;");
        resume.getChildren().add(lbl("💳  "+paies.size()+" paiements  •  ✅ "+nbPaye+" payés  •  ⏳ "+nbAv+" à venir","-fx-font-size:12px;-fx-text-fill:#334455;"));

        Button btnE = btn("✏️", "-fx-background-color:#e8eeff;-fx-font-size:14px;-fx-padding:8 14;-fx-background-radius:8;-fx-cursor:hand;", "Modifier");
        Button btnD = btn("🗑",  "-fx-background-color:#fff0f0;-fx-font-size:14px;-fx-padding:8 14;-fx-background-radius:8;-fx-cursor:hand;", "Supprimer");
        btnE.setOnAction(e -> afficherDialogModifier(a));
        btnD.setOnAction(e -> supprimerAbo(a));
        HBox boutons = new HBox(10); boutons.getChildren().addAll(btnE, btnD);
        carte.getChildren().addAll(top, infos, resume, boutons);
        return carte;
    }

    // ===== DIALOG MODIFIER =====
    private void afficherDialogModifier(Abonnement a) {
        Dialog<ButtonType> dlg = new Dialog<>();
        dlg.setTitle(null); dlg.setHeaderText(null);
        VBox root = new VBox(0); root.setPrefWidth(480);

        // Header
        VBox header = new VBox(5);
        header.setStyle("-fx-background-color:#0f1f3d;-fx-padding:22 28 18 28;-fx-background-radius:12 12 0 0;");
        HBox hdr = new HBox(15); hdr.setAlignment(Pos.CENTER_LEFT);
        VBox hdrTxt = new VBox(3);
        hdrTxt.getChildren().addAll(
                lbl("Modifier l'abonnement", "-fx-font-size:17px;-fx-font-weight:bold;-fx-text-fill:white;"),
                lbl(a.getNom()+"  —  "+a.getCategorie(), "-fx-font-size:11px;-fx-text-fill:#6a8aaa;"));
        hdr.getChildren().addAll(creerIcone(a.getNom()), hdrTxt);
        header.getChildren().add(hdr);

        // Corps
        VBox corps = new VBox(16);
        corps.setStyle("-fx-background-color:white;-fx-padding:22 28;-fx-background-radius:0 0 12 12;");

        TextField fNom  = field(a.getNom());
        TextField fPrix = field(String.valueOf(a.getPrix()));
        HBox row1 = hboxGrow(labeledF("Nom du service", fNom), labeledF("Prix (TND)", fPrix));

        ComboBox<String> fFreq = new ComboBox<>(FXCollections.observableArrayList("Mensuel","Annuel"));
        fFreq.setValue(a.getFrequence()); fFreq.setMaxWidth(Double.MAX_VALUE);
        fFreq.setStyle("-fx-background-radius:9;-fx-font-size:13px;");

        ToggleGroup tg = new ToggleGroup();
        RadioButton rbA = rb("✅ Actif", tg, a.isActif(),  "8 0 0 8", "#e8fff4", "#f5f5f5");
        RadioButton rbI = rb("❌ Inactif", tg, !a.isActif(), "0 8 8 0", "#fff0f0", "#f5f5f5");
        tg.selectedToggleProperty().addListener((o,v,n) -> {
            rbA.setStyle(rbStyle(rbA.isSelected(),"#e8fff4","8 0 0 8"));
            rbI.setStyle(rbStyle(rbI.isSelected(),"#fff0f0","0 8 8 0"));
        });
        HBox toggles = new HBox(0); toggles.getChildren().addAll(rbA, rbI);
        HBox row2 = hboxGrow(labeledF("Fréquence", fFreq), labeledF("Statut", toggles));

        Button btnC = btn("Annuler",   "-fx-background-color:white;-fx-text-fill:#556677;-fx-padding:11 24;-fx-background-radius:8;-fx-border-color:#dde3ea;-fx-border-radius:8;-fx-border-width:1.5;-fx-font-size:13px;-fx-cursor:hand;", null);
        Button btnS = btn("💾  Enregistrer", "-fx-background-color:#1a3a7a;-fx-text-fill:white;-fx-padding:11 28;-fx-background-radius:8;-fx-font-size:13px;-fx-font-weight:bold;-fx-cursor:hand;-fx-effect:dropshadow(gaussian,rgba(26,58,122,0.4),8,0,0,3);", null);
        HBox btnBox = new HBox(12); btnBox.setAlignment(Pos.CENTER_RIGHT); btnBox.getChildren().addAll(btnC, btnS);

        corps.getChildren().addAll(row1, row2, new Separator(), btnBox);
        root.getChildren().addAll(header, corps);
        dlg.getDialogPane().setContent(root);
        dlg.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dlg.getDialogPane().lookupButton(ButtonType.CLOSE).setVisible(false);
        dlg.getDialogPane().setStyle("-fx-padding:0;");

        btnC.setOnAction(e -> dlg.close());
        btnS.setOnAction(e -> {
            try {
                a.setNom(fNom.getText()); a.setPrix(Double.parseDouble(fPrix.getText()));
                a.setFrequence(fFreq.getValue()); a.setActif(rbA.isSelected());
                aboService.modifier(a); dlg.close();
                alerte(Alert.AlertType.INFORMATION, "✅ Modifié !");
                List<Abonnement> maj = aboService.afficher().stream().filter(ab->ab.getNom().trim().equalsIgnoreCase(a.getNom().trim())).toList();
                ouvrirDetail(a.getNom(), maj);
            } catch (NumberFormatException ex) { alerte(Alert.AlertType.ERROR, "❌ Prix invalide !"); }
        });
        dlg.showAndWait();
    }

    // ===== AJOUT =====
    @FXML private void ajouter() {
        if (txtNom.getText().isEmpty() || txtPrix.getText().isEmpty()
                || comboCategorie.getValue()==null || comboFrequence.getValue()==null || datePicker.getValue()==null) {
            alerte(Alert.AlertType.WARNING, "⚠️ Remplissez tous les champs !"); return;
        }
        try {
            aboService.ajouter(new Abonnement(txtNom.getText(), Double.parseDouble(txtPrix.getText()),
                    Date.valueOf(datePicker.getValue()), comboFrequence.getValue(), comboCategorie.getValue(), true));
            alerte(Alert.AlertType.INFORMATION, "✅ Ajouté !"); retourListe();
        } catch (NumberFormatException e) { alerte(Alert.AlertType.ERROR, "❌ Prix invalide !"); }
    }

    private void supprimerAbo(Abonnement a) {
        Alert c = new Alert(Alert.AlertType.CONFIRMATION); c.setHeaderText(null);
        c.setContentText("Supprimer « "+a.getNom()+" » ?");
        c.showAndWait().ifPresent(btn -> {
            if (btn==ButtonType.OK) {
                aboService.supprimer(a.getId());
                List<Abonnement> reste = aboService.afficher().stream().filter(ab->ab.getNom().trim().equalsIgnoreCase(nomServiceSelectionne.trim())).toList();
                if (reste.isEmpty()) retourListe(); else ouvrirDetail(nomServiceSelectionne, reste);
            }
        });
    }

    // ===== HELPERS UI =====
    private void vider() { txtNom.clear(); txtPrix.clear(); comboCategorie.setValue(null); comboFrequence.setValue(null); datePicker.setValue(null); }
    private void alerte(Alert.AlertType t, String msg) { Alert a=new Alert(t); a.setHeaderText(null); a.setContentText(msg); a.showAndWait(); }
    private Label lbl(String t, String s)              { Label l=new Label(t); l.setStyle(s); return l; }
    private TextField field(String v)                  { TextField tf=new TextField(v); tf.setStyle(S_FIELD); return tf; }
    private Button btn(String t, String s, String tip) { Button b=new Button(t); b.setStyle(s); if(tip!=null) b.setTooltip(new Tooltip(tip)); return b; }

    private VBox labeledF(String label, javafx.scene.Node n) {
        VBox v=new VBox(7); v.getChildren().addAll(lbl(label,"-fx-font-size:11px;-fx-font-weight:bold;-fx-text-fill:#445566;"),n); return v;
    }
    private HBox hboxGrow(VBox a, VBox b) {
        HBox h=new HBox(15); HBox.setHgrow(a,Priority.ALWAYS); HBox.setHgrow(b,Priority.ALWAYS); h.getChildren().addAll(a,b); return h;
    }
    private RadioButton rb(String t, ToggleGroup g, boolean sel, String r, String on, String off) {
        RadioButton rb=new RadioButton(t); rb.setToggleGroup(g); rb.setSelected(sel); rb.setStyle(rbStyle(sel,on,r)); return rb;
    }
    private String rbStyle(boolean sel, String onCol, String r) {
        return "-fx-padding:10 20;-fx-cursor:hand;-fx-font-size:12px;-fx-background-color:"+(sel?onCol:"#f5f5f5")+";-fx-background-radius:"+r+";";
    }
    private VBox statCard(String label, String val, String color) {
        VBox c=new VBox(5); c.setPrefWidth(190);
        c.setStyle("-fx-background-color:white;-fx-padding:16;-fx-background-radius:12;-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.06),8,0,0,2);");
        c.getChildren().addAll(lbl(label,"-fx-font-size:11px;-fx-text-fill:#8899aa;"),lbl(val,"-fx-font-size:20px;-fx-font-weight:bold;-fx-text-fill:"+color+";")); return c;
    }
    private VBox infoBox(String label, String val) {
        VBox v=new VBox(4);
        v.getChildren().addAll(lbl(label,"-fx-text-fill:#8899aa;-fx-font-size:10px;-fx-font-weight:bold;"),lbl(val,"-fx-text-fill:#0f1f3d;-fx-font-size:14px;-fx-font-weight:bold;")); return v;
    }
    private StackPane creerIcone(String nom) {
        Circle c=new Circle(24); c.setFill(Color.web(couleur(nom)));
        Label l=lbl(logo(nom),"-fx-font-size:13px;-fx-text-fill:white;-fx-font-weight:bold;");
        return new StackPane(c,l);
    }
    private String couleur(String n) {
        n=n.toLowerCase();
        if(n.contains("netflix"))   return "#E50914"; if(n.contains("spotify"))   return "#1DB954";
        if(n.contains("amazon"))    return "#FF9900"; if(n.contains("disney"))    return "#113CCF";
        if(n.contains("youtube"))   return "#FF0000"; if(n.contains("deezer"))    return "#A238FF";
        if(n.contains("microsoft")) return "#0078D4"; if(n.contains("google"))    return "#4285F4";
        if(n.contains("adobe"))     return "#FF4444"; if(n.contains("apple"))     return "#555555";
        return "#1a3a7a";
    }
    private String logo(String n) {
        n=n.toLowerCase();
        if(n.contains("netflix"))   return "N";  if(n.contains("spotify"))   return "S";
        if(n.contains("amazon"))    return "a";  if(n.contains("disney"))    return "D+";
        if(n.contains("youtube"))   return "▶"; if(n.contains("deezer"))    return "Dz";
        if(n.contains("microsoft")) return "M";  if(n.contains("google"))    return "G";
        if(n.contains("adobe"))     return "Ai";
        return n.substring(0, Math.min(2,n.length())).toUpperCase();
    }
}