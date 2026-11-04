package com.gigcampus.entity;

public enum WorkMode {
    REMOTE("Remote"),
    ON_SITE("On-Site (Erode)"),
    HYBRID("Hybrid");

    private final String displayName;

    WorkMode(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
