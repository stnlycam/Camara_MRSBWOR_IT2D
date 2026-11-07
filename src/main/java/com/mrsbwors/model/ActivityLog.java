package com.mrsbwors.model;

public record ActivityLog(int id, int userId, String activity, String description, String timestamp) {
    @Override
    public String toString() {
        return String.format("[%d] User ID: %d | %s - %s (%s)", id, userId, activity, description, timestamp);
    }
}