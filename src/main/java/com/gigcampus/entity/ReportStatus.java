package com.gigcampus.entity;

public enum ReportStatus {
    PENDING("Pending Review"),
    RESOLVED("Resolved"),
    DISMISSED("Dismissed");

    private final String displayName;

    ReportStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
