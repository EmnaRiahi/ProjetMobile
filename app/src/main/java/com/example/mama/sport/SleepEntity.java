package com.example.mama.sport;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "sleep")
public class SleepEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;
    
    public String date; // Format: dd/MM/yyyy
    public float hours;

    public SleepEntity(String date, float hours) {
        this.date = date;
        this.hours = hours;
    }
}
