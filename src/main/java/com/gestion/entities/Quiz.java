package com.gestion.entities;

import java.time.LocalDate;
import java.util.List;

public class Quiz {

    private int idQuiz;
    private int idCours;
    private String titre;
    private List<String> listeDeReponse;
    private String reponseCorrect;
    private int scoreDeQuiz;
    private LocalDate dateCreation;

    public Quiz() {}


    public Quiz(int idCours, String titre, List<String> listeDeReponse,
                String reponseCorrect, int scoreDeQuiz, LocalDate dateCreation) {
        this.idCours = idCours;
        this.titre = titre;
        this.listeDeReponse = listeDeReponse;
        this.reponseCorrect = reponseCorrect;
        this.scoreDeQuiz = scoreDeQuiz;
        this.dateCreation = dateCreation;
    }

    public Quiz(int idQuiz, int idCours, String titre, List<String> listeDeReponse,
                String reponseCorrect, int scoreDeQuiz, LocalDate dateCreation) {
        this.idQuiz = idQuiz;
        this.idCours = idCours;
        this.titre = titre;
        this.listeDeReponse = listeDeReponse;
        this.reponseCorrect = reponseCorrect;
        this.scoreDeQuiz = scoreDeQuiz;
        this.dateCreation = dateCreation;
    }

    public int getIdQuiz() {
        return idQuiz;
    }

    public void setIdQuiz(int idQuiz) {
        this.idQuiz = idQuiz;
    }

    public int getIdCours() {
        return idCours;
    }

    public void setIdCours(int idCours) {
        this.idCours = idCours;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public List<String> getListeDeReponse() {
        return listeDeReponse;
    }

    public void setListeDeReponse(List<String> listeDeReponse) {
        this.listeDeReponse = listeDeReponse;
    }

    public String getReponseCorrect() {
        return reponseCorrect;
    }

    public void setReponseCorrect(String reponseCorrect) {
        this.reponseCorrect = reponseCorrect;
    }

    public int getScoreDeQuiz() {
        return scoreDeQuiz;
    }

    public void setScoreDeQuiz(int scoreDeQuiz) {
        this.scoreDeQuiz = scoreDeQuiz;
    }

    public LocalDate getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDate dateCreation) {
        this.dateCreation = dateCreation;
    }
}