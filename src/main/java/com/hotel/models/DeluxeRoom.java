package com.hotel.models;

import java.util.Arrays;
import java.util.List;

public class DeluxeRoom extends Room {
    private static final double DELUXE_TAX_RATE = 0.10; // 10% tax for deluxe rooms

    public DeluxeRoom(int roomNumber, double basePrice) {
        super(roomNumber, basePrice);
    }

    @Override
    public double calculateTotalStayCost(int days) {
        // DeluxeRoom includes a 10% tax over the base price
        double baseCost = getPricePerNight() * days;
        return baseCost + (baseCost * DELUXE_TAX_RATE);
    }

    @Override
    public List<String> getAmenitiesList() {
        return Arrays.asList("WiFi", "Television", "Complimentary Breakfast", "Room Service");
    }

    @Override
    public String getRoomType() {
        return "Deluxe";
    }
}
