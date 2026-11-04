package com.gigcampus.dto;

public class NotificationDto {

    private Long id;
    private String title;
    private String message;
    private String linkUrl;
    private boolean read;
    private String timeAgo;
    private long unreadCount;

    public NotificationDto() {
    }

    public NotificationDto(Long id, String title, String message, String linkUrl, boolean read, String timeAgo, long unreadCount) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.linkUrl = linkUrl;
        this.read = read;
        this.timeAgo = timeAgo;
        this.unreadCount = unreadCount;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getLinkUrl() {
        return linkUrl;
    }

    public void setLinkUrl(String linkUrl) {
        this.linkUrl = linkUrl;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public String getTimeAgo() {
        return timeAgo;
    }

    public void setTimeAgo(String timeAgo) {
        this.timeAgo = timeAgo;
    }

    public long getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(long unreadCount) {
        this.unreadCount = unreadCount;
    }
}
