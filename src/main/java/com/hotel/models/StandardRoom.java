package com.hotel.models;

import java.util.Arrays;
import java.util.List;

public class StandardRoom extends Room {
    public StandardRoom(int roomNumber, double basePrice) {
        super(roomNumber, basePrice);
    }

    @Override
    public double calculateTotalStayCost(int days) {
        // StandardRoom stays at the base price
        return getPricePerNight() * days;
    }

    @Override
    public List<String> getAmenitiesList() {
        return Arrays.asList("WiFi", "Television", "Air Conditioning");
    }

    @Override
    public String getRoomType() {
        return "Standard";
    }
}
