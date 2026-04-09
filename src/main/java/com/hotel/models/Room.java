package com.hotel.models;

import java.io.Serializable;
import java.util.List;

public abstract class Room implements Serializable {
    private static final long serialVersionUID = 1L;
    private int roomNumber;
    private double pricePerNight;
    private boolean isAvailable;
    private boolean underMaintenance;
    private HousekeepingStatus housekeepingStatus;

    public Room(int roomNumber, double pricePerNight) {
        this.roomNumber = roomNumber;
        this.pricePerNight = pricePerNight;
        this.isAvailable = true;
        this.underMaintenance = false;
        this.housekeepingStatus = HousekeepingStatus.CLEAN;
    }

    public int getRoomNumber() {
        return roomNumber;
    }

    public double getPricePerNight() {
        return pricePerNight;
    }

    public void setPricePerNight(double pricePerNight) {
        this.pricePerNight = pricePerNight;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }

    public boolean isUnderMaintenance() {
        return underMaintenance;
    }

    public void setUnderMaintenance(boolean underMaintenance) {
        this.underMaintenance = underMaintenance;
    }

    public String getStatus() {
        if (underMaintenance) return "Maintenance";
        return isAvailable ? "Available" : "Occupied";
    }

    /** Null-safe getter — handles old serialized data that won't have this field */
    public HousekeepingStatus getHousekeepingStatus() {
        return housekeepingStatus != null ? housekeepingStatus : HousekeepingStatus.CLEAN;
    }

    public void setHousekeepingStatus(HousekeepingStatus housekeepingStatus) {
        this.housekeepingStatus = housekeepingStatus;
    }

    // Abstraction & Polymorphism: Force subclasses to define their own pricing logic
    public abstract double calculateTotalStayCost(int days);

    // Abstraction & Polymorphism: Subclasses will provide their specific list of amenities
    public abstract List<String> getAmenitiesList();

    // To be overridden in subclasses to return their specific type string
    public abstract String getRoomType();

    @Override
    public String toString() {
        return String.format("%s [No:%d, Price:%.2f, Status:%s, HK:%s]",
            getRoomType(), roomNumber, pricePerNight, getStatus(),
            getHousekeepingStatus().getDisplayName());
    }
}