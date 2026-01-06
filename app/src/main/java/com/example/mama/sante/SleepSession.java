package com.example.mama.sante;

// POJO pour représenter une session de sommeil.
public class SleepSession {
    private final String date;
    private final int deepSleepMinutes;
    private final int lightSleepMinutes;
    private final int soundEvents;

    public SleepSession(String date, int deepSleepMinutes, int lightSleepMinutes, int soundEvents) {
        this.date = date;
        this.deepSleepMinutes = deepSleepMinutes;
        this.lightSleepMinutes = lightSleepMinutes;
        this.soundEvents = soundEvents;
    }

    // Getters
    public String getDate() {
        return date;
    }

    public int getDeepSleepMinutes() {
        return deepSleepMinutes;
    }

    public int getLightSleepMinutes() {
        return lightSleepMinutes;
    }
    
    public int getTotalMinutes() {
        return deepSleepMinutes + lightSleepMinutes;
    }

    public int getSoundEvents() {
        return soundEvents;
    }
}
