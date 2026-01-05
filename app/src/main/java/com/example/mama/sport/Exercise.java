package com.example.mama.sport;

public class Exercise {
    private String name;
    private String description;
    private String videoUrl;
    private int imageResId; 

    public Exercise(String name, String description, int imageResId, String videoUrl) {
        this.name = name;
        this.description = description;
        this.imageResId = imageResId;
        this.videoUrl = videoUrl;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getImageResId() {
        return imageResId;
    }

    public String getVideoUrl() {
        return videoUrl;
    }
}
