package com.example.mama.bot;

public class ChatMessage {
    public String message;
    public String sender; // "USER" ou "BOT"

    public ChatMessage(String message, String sender) {
        this.message = message;
        this.sender = sender;
    }
}