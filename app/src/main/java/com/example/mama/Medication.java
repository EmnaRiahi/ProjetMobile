package com.example.mama;

public class Medication {
    String id;
    String name;
    String generic;
    String time;
    String doses; // Nouveau

    public Medication(String id, String name, String generic, String time, String doses) {
        this.id = id;
        this.name = name;
        this.generic = generic;
        this.time = time;
        this.doses = doses;
    }
}