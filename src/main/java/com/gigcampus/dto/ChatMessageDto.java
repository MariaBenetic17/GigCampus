package com.gigcampus.dto;

import java.time.LocalDateTime;

public class ChatMessageDto {

    private Long id;
    private Long conversationId;
    private Long senderId;
    private String senderName;
    private String senderRole;
    private String content;
    private String sentAtFormatted;
    private boolean mine;

    public ChatMessageDto() {
    }

    public ChatMessageDto(Long id, Long conversationId, Long senderId, String senderName, String senderRole, String content, String sentAtFormatted, boolean mine) {
        this.id = id;
        this.conversationId = conversationId;
        this.senderId = senderId;
        this.senderName = senderName;
        this.senderRole = senderRole;
        this.content = content;
        this.sentAtFormatted = sentAtFormatted;
        this.mine = mine;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getConversationId() {
        return conversationId;
    }

    public void setConversationId(Long conversationId) {
        this.conversationId = conversationId;
    }

    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getSenderRole() {
        return senderRole;
    }

    public void setSenderRole(String senderRole) {
        this.senderRole = senderRole;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSentAtFormatted() {
        return sentAtFormatted;
    }

    public void setSentAtFormatted(String sentAtFormatted) {
        this.sentAtFormatted = sentAtFormatted;
    }

    public boolean isMine() {
        return mine;
    }

    public void setMine(boolean mine) {
        this.mine = mine;
    }
}
