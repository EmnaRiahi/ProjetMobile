package com.example.mama.bot;

import java.util.ArrayList;
import java.util.List;

public class GeminiRequest {
    public List<Content> contents;

    public GeminiRequest(String userText) {
        this.contents = new ArrayList<>();

        Part part = new Part(userText);

        ArrayList<Part> partsList = new ArrayList<>();
        partsList.add(part);

        Content content = new Content(partsList);
        this.contents.add(content);
    }

    public static class Content {
        public List<Part> parts;
        public Content(List<Part> parts) { this.parts = parts; }
    }

    public static class Part {
        public String text;
        public Part(String text) { this.text = text; }
    }
}