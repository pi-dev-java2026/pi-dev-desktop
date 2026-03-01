package Fintech.tests;

import Fintech.entities.Reclamation;
import Fintech.entities.User;
import Fintech.servicies.ServiceReclamation;
import Fintech.servicies.ServiceUser;

import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {

        /*ServiceUser su = new ServiceUser();
        ServiceReclamation ru = new ServiceReclamation();

        try {
            su.ajouter(new User(
                    "Aziz",
                    "Aziizzz@gmail.com",
                    "2355654411",
                    "12345556",
                    "CLIENT"));
            ru.ajouter(new Reclamation(
                    1, // id du user (doit exister dans la table user)
                    "test@example.com", // email
                    "Problème de paiement",
                    "Le paiement a échoué mais le montant a été débité",
                    "En attente")); // statut
            System.out.println("Reclamation ajouté");

            // su.modifier(new User(1, "Ayoub", "ayoub@gmail.com", "99887766", "pass123",
            // "ADMIN"));
            // System.out.println("User modifié");

            // su.supprimer(1);
            System.out.println(ru.afficher());

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    */}
}
