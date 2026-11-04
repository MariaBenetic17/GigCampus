package com.gigcampus.entity;

public enum VerificationStatus {
    UNVERIFIED("Unverified"),
    PENDING_REVIEW("Pending Review"),
    VERIFIED("Verified"),
    REJECTED("Rejected");

    private final String displayName;

    VerificationStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
