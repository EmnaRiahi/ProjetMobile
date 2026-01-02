package com.example.mama.sport;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "activite")
public class ActiviteEntity {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String date;
    public String time;
    public int duration; // minutes
    public int steps;
    public String type;
    // FIX: Add this empty constructor
    @Ignore // Add this so Room ignores this constructor
    public ActiviteEntity(String date, String time, int duration, int steps, String type) {
        this.date = date;
        this.time = time;
        this.duration = duration;
        this.steps = steps;
        this.type = type;
    }

    public ActiviteEntity() {
        // Room will use this one
    }
}
