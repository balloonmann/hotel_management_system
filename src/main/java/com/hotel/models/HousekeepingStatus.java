package com.hotel.models;

/**
 * Enum representing a room's housekeeping state.
 * Demonstrates a state-machine pattern: CLEAN → DIRTY → CLEANING → CLEAN.
 */
public enum HousekeepingStatus {
    CLEAN("Clean"),
    DIRTY("Needs Cleaning"),
    CLEANING("Being Cleaned");

    private final String displayName;

    HousekeepingStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /** Cycle to the next state in the workflow */
    public HousekeepingStatus next() {
        switch (this) {
            case DIRTY:    return CLEANING;
            case CLEANING: return CLEAN;
            case CLEAN:    return DIRTY; // fallback: manual dirt flag
            default:       return CLEAN;
        }
    }
}
