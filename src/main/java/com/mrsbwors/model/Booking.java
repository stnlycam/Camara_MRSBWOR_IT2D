package com.mrsbwors.model;

public record Booking(int id, int customerId, int motorcycleId, String bookingDate, String serviceType, String status) {
    @Override
    public String toString() {
        return String.format("[%d] Customer: %d | Moto: %d | Date: %s | %s (%s)", id, customerId, motorcycleId, bookingDate, serviceType, status);
    }
}