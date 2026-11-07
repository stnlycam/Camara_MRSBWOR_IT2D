package com.mrsbwors.model;

public record RepairWorkOrder(int id, int bookingId, Integer mechanicId, String repairDetails, String status, String completionDate) {
    @Override
    public String toString() {
        return String.format("[%d] Booking: %d | Mechanic: %s | %s (%s)", id, bookingId, mechanicId == null ? "UNASSIGNED" : mechanicId, repairDetails, status);
    }
}