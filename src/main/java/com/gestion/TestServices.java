package com.gestion;

import com.gestion.Services.ServiceCours;
import com.gestion.Services.ServiceQuiz;
import com.gestion.entities.Cours;
import com.gestion.entities.Quiz;

public class TestServices {
    public static void main(String[] args) throws Exception {
        ServiceCours coursDAO = new ServiceCours();
        int idCours = coursDAO.add(new Cours("Java", "C:\\docs\\java.pdf", "Intro Java", java.time.LocalDate.now()));

        ServiceQuiz quizDAO = new ServiceQuiz();
        quizDAO.add(new Quiz(idCours, "Q1", java.util.List.of("A","B","C"), "B", 10, java.time.LocalDate.now()));

        System.out.println(coursDAO.getAll().size());
        System.out.println(quizDAO.getAll().size());
    }
}
