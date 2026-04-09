package com.hotel.models;

import java.util.Arrays;
import java.util.List;

public class LuxuryRoom extends Room {
    private static final double LUXURY_TAX_RATE = 0.20; // 20% tax for luxury rooms
    private static final double SERVICE_CHARGE = 50.0; // Fixed per-stay service charge

    public LuxuryRoom(int roomNumber, double basePrice) {
        super(roomNumber, basePrice);
    }

    @Override
    public double calculateTotalStayCost(int days) {
        // LuxuryRoom includes a 20% tax and a fixed service charge
        double baseCost = getPricePerNight() * days;
        return baseCost + (baseCost * LUXURY_TAX_RATE) + SERVICE_CHARGE;
    }

    @Override
    public List<String> getAmenitiesList() {
        return Arrays.asList("WiFi", "Television", "Complimentary Breakfast", "Pool Access", "Gym Access", "Spa Access");
    }

    @Override
    public String getRoomType() {
        return "Luxury";
    }
}
