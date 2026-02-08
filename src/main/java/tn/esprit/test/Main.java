package tn.esprit.test;

import tn.esprit.entities.Abonnement;
import tn.esprit.entities.Paiement;
import tn.esprit.services.AbonnementService;
import tn.esprit.services.PaiementService;

import java.sql.Date;
import java.util.List;

public class Main {
    public static void main(String[] args) {

        AbonnementService abonnementService = new AbonnementService();
        PaiementService paiementService = new PaiementService();

        System.out.println("===== TEST ABONNEMENT AVEC PAIEMENTS AUTOMATIQUES =====\n");

        // AJOUTER DES ABONNEMENTS (les paiements sont générés automatiquement)
        System.out.println("1. Ajout d'abonnements :");
        abonnementService.ajouter(new Abonnement("Netflix", 14.99, Date.valueOf("2024-01-15"), "Mensuel", "Streaming", true));
        System.out.println();
        abonnementService.ajouter(new Abonnement("Spotify", 9.99, Date.valueOf("2024-02-01"), "Mensuel", "Musique", true));
        System.out.println();
        abonnementService.ajouter(new Abonnement("Salle de sport", 49.99, Date.valueOf("2024-01-10"), "Mensuel", "Sport", true));

        // AFFICHER LES ABONNEMENTS
        System.out.println("\n\n2. Liste des abonnements :");
        List<Abonnement> abonnements = abonnementService.afficher();
        for (Abonnement a : abonnements) {
            System.out.println(a);
        }

        // AFFICHER LES PAIEMENTS (générés automatiquement)
        System.out.println("\n\n3. Liste des paiements (générés automatiquement) :");
        List<Paiement> paiements = paiementService.afficher();
        for (Paiement p : paiements) {
            System.out.println(p);
        }

        // MODIFIER UN ABONNEMENT
        if (!abonnements.isEmpty()) {
            System.out.println("\n\n4. Modification d'un abonnement :");
            Abonnement aModifier = abonnements.get(0);
            System.out.println("Avant : " + aModifier.getNom() + " - " + aModifier.getPrix() + "€");
            aModifier.setPrix(16.99);
            abonnementService.modifier(aModifier);
            System.out.println("Après : " + aModifier.getNom() + " - " + aModifier.getPrix() + "€");
        }

        // SUPPRIMER UN ABONNEMENT (CASCADE supprime aussi les paiements)
        if (abonnements.size() > 1) {
            System.out.println("\n\n5. Suppression d'un abonnement (CASCADE) :");
            int idAbonnement = abonnements.get(abonnements.size() - 1).getId();
            System.out.println("Suppression de l'abonnement ID : " + idAbonnement);
            abonnementService.supprimer(idAbonnement);
        }

        // RÉSULTAT FINAL
        System.out.println("\n\n===== RÉSULTAT FINAL =====");
        System.out.println("Abonnements restants : " + abonnementService.afficher().size());
        System.out.println("Paiements restants : " + paiementService.afficher().size());
        System.out.println("\n===== FIN DES TESTS =====");
    }
}
