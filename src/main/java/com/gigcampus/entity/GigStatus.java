package com.gigcampus.entity;

public enum GigStatus {
    DRAFT("Draft"),
    OPEN("Open"),
    IN_PROGRESS("In Progress"),
    COMPLETED("Completed"),
    CLOSED("Closed");

    private final String displayName;

    GigStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
