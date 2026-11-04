package com.gigcampus.entity;

public enum ApplicationStatus {
    PENDING("Pending Review"),
    ACCEPTED("Accepted / Project Active"),
    REJECTED("Not Selected"),
    WITHDRAWN("Withdrawn by Student"),
    COMPLETED("Completed & Delivered");

    private final String displayName;

    ApplicationStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
