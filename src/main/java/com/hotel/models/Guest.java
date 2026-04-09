package com.hotel.models;

import java.util.ArrayList;
import java.util.List;

public class Guest extends Person {
    private String customerId;
    private int roomNumberAllocated;
    private List<Booking> bookingHistory;

    public Guest(String customerId, String name, String contactNumber) {
        super(name, contactNumber);
        this.customerId = customerId;
        this.roomNumberAllocated = -1; // -1 signifies no room currently allocated
        this.bookingHistory = new ArrayList<>();
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public int getRoomNumberAllocated() {
        return roomNumberAllocated;
    }

    public void setRoomNumberAllocated(int roomNumberAllocated) {
        this.roomNumberAllocated = roomNumberAllocated;
    }

    public List<Booking> getBookingHistory() {
        return bookingHistory;
    }

    public void addBookingToHistory(Booking booking) {
        this.bookingHistory.add(booking);
    }

    @Override
    public String getRole() {
        return "GUEST";
    }
}