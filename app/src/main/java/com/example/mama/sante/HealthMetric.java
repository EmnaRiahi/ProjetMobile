package com.example.mama.sante;

// Un simple "Plain Old Java Object" (POJO) pour représenter une entrée de données de santé.
// C'est une bonne pratique pour structurer les données et les séparer de la logique UI.
public class HealthMetric {
    private String id;
    private String date;
    private float weight;
    private int systolic;
    private int diastolic;

    public HealthMetric(String id, String date, float weight, int systolic, int diastolic) {
        this.id = id;
        this.date = date;
        this.weight = weight;
        this.systolic = systolic;
        this.diastolic = diastolic;
    }

    // Getters pour accéder aux données
    public String getId() {
        return id;
    }

    public String getDate() {
        return date;
    }

    public float getWeight() {
        return weight;
    }

    public int getSystolic() {
        return systolic;
    }

    public int getDiastolic() {
        return diastolic;
    }
}
