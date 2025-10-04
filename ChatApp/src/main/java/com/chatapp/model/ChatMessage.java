package com.chatapp.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ChatMessage {
    private String sender;
    private String receiver;
    private String content;
    private MessageType type;
    private LocalDateTime timestamp;
    private String formattedTime;

    public enum MessageType { 
        CHAT, JOIN, LEAVE, USERS 
    }

    public ChatMessage() {
        this.timestamp = LocalDateTime.now();
        this.formattedTime = this.timestamp.format(DateTimeFormatter.ofPattern("hh:mm a"));
    }

    // Getters and Setters
    public String getSender() {
    	return sender;
    	}
    public void setSender(String sender) { this.sender = sender; }

    public String getReceiver() { return receiver; }
    public void setReceiver(String receiver) { this.receiver = receiver; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public MessageType getType() { return type; }
    public void setType(MessageType type) { this.type = type; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { 
        this.timestamp = timestamp; 
        this.formattedTime = timestamp.format(DateTimeFormatter.ofPattern("hh:mm a"));
    }

    public String getFormattedTime() { return formattedTime; }
    public void setFormattedTime(String formattedTime) { this.formattedTime = formattedTime; }

    @Override
    public String toString() {
        return "ChatMessage{" +
                "sender='" + sender + '\'' +
                ", receiver='" + receiver + '\'' +
                ", content='" + content + '\'' +
                ", type=" + type +
                '}';
    }
}