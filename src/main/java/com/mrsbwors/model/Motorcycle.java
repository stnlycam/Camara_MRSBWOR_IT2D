package com.mrsbwors.model;

public record Motorcycle(int id, int customerId, String brand, String model, String plateNumber) {
    @Override
    public String toString() {
        return String.format("[%d] %s %s (%s) - Customer ID: %d", id, brand, model, plateNumber, customerId);
    }
}