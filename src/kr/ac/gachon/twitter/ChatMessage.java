package kr.ac.gachon.twitter;

import java.sql.Timestamp;

public class ChatMessage {
    private long chatId;
    private long senderId;
    private String senderName;
    private long receiverId;
    private String message;
    private Timestamp createdAt;
    private boolean isRead;

    public ChatMessage(long chatId, long senderId, String senderName, long receiverId, 
                      String message, Timestamp createdAt, boolean isRead) {
        this.chatId = chatId;
        this.senderId = senderId;
        this.senderName = senderName;
        this.receiverId = receiverId;
        this.message = message;
        this.createdAt = createdAt;
        this.isRead = isRead;
    }

    // Getters
    public long getChatId() { return chatId; }
    public long getSenderId() { return senderId; }
    public String getSenderName() { return senderName; }
    public long getReceiverId() { return receiverId; }
    public String getMessage() { return message; }
    public Timestamp getCreatedAt() { return createdAt; }
    public boolean isRead() { return isRead; }
} 