package com.example.mama.medication;

public class Medication {
    String id;
    String name;
    String generic;
    String time;
    String doses;
    String frequency; // Once a day, etc.
    String schedule;  // Comma-separated: Morning, Afternoon, etc.

    public Medication(String id, String name, String generic, String time, String doses, String frequency, String schedule) {
        this.id = id;
        this.name = name;
        this.generic = generic;
        this.time = time;
        this.doses = doses;
        this.frequency = frequency;
        this.schedule = schedule;
    }
}