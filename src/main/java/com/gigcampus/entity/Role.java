package com.gigcampus.entity;

public enum Role {
    ROLE_STUDENT("Student"),
    ROLE_BUSINESS("Business"),
    ROLE_ADMIN("Admin");

    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
