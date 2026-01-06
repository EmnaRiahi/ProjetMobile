package com.example.mama.sante;

// POJO pour la désérialisation du JSON avec Gson.
// Les noms des variables doivent correspondre aux clés dans le fichier JSON.
public class WeeklyInfo {
    private int week;
    private String title;
    private String info;

    // Getters
    public int getWeek() {
        return week;
    }

    public String getTitle() {
        return title;
    }

    public String getInfo() {
        return info;
    }
}
