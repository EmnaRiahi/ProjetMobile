package com.example.mama.Nutrition;
public class Meal {
    private int id;
    private String name;
    private String date;
    private String time;
    private String notes;

    public Meal() {}

    public Meal(String name, String date, String time, String notes) {
        this.name = name;
        this.date = date;
        this.time = time;
        this.notes = notes;
    }

    public Meal(int id, String name, String date, String time, String notes) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.time = time;
        this.notes = notes;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public String getNotes() {
        return notes;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}