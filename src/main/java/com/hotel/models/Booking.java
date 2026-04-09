package com.hotel.models;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Booking implements Serializable {
    private static final long serialVersionUID = 1L;

    private String bookingId;
    private Guest guest;
    private Room room;
    private int daysStayed;
    private double totalAmount;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private boolean cancelled;
    private List<String> specialRequests;
    private List<BillItem> extraCharges;

    public Booking(String bookingId, Guest guest, Room room, int daysStayed,
                   LocalDate checkIn, LocalDate checkOut, double seasonMultiplier) {
        this.bookingId = bookingId;
        this.guest = guest;
        this.room = room;
        this.daysStayed = daysStayed;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.cancelled = false;
        this.specialRequests = new ArrayList<>();
        this.extraCharges = new ArrayList<>();
        this.totalAmount = calculateTotal(room, daysStayed) * seasonMultiplier;
    }

    /** Backward-compatible constructor (no dates, no seasonal pricing) */
    public Booking(String bookingId, Guest guest, Room room, int daysStayed) {
        this(bookingId, guest, room, daysStayed, null, null, 1.0);
    }

    private double calculateTotal(Room room, int days) {
        if (room != null && days > 0) {
            return room.calculateTotalStayCost(days);
        }
        return 0.0;
    }

    // --- Core Accessors ---
    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }

    public Guest getGuest() { return guest; }
    public void setGuest(Guest guest) { this.guest = guest; }

    public Room getRoom() { return room; }
    public void setRoom(Room room) {
        this.room = room;
        this.totalAmount = calculateTotal(this.room, this.daysStayed);
    }

    public int getDaysStayed() { return daysStayed; }
    public void setDaysStayed(int daysStayed) {
        this.daysStayed = daysStayed;
        this.totalAmount = calculateTotal(this.room, this.daysStayed);
    }

    public double getTotalAmount() { return totalAmount; }

    public LocalDate getCheckIn() { return checkIn; }
    public LocalDate getCheckOut() { return checkOut; }

    public boolean isCancelled() { return cancelled; }
    public void setCancelled(boolean cancelled) { this.cancelled = cancelled; }

    // --- Special Requests ---
    public List<String> getSpecialRequests() {
        return specialRequests != null ? specialRequests : new ArrayList<>();
    }

    public void addSpecialRequest(String request) {
        if (specialRequests == null) specialRequests = new ArrayList<>();
        if (request != null && !request.trim().isEmpty())
            specialRequests.add(request.trim());
    }

    // --- Itemized Billing ---
    public List<BillItem> getExtraCharges() {
        return extraCharges != null ? extraCharges : new ArrayList<>();
    }

    public void addExtraCharge(BillItem item) {
        if (extraCharges == null) extraCharges = new ArrayList<>();
        extraCharges.add(item);
    }

    /** Room cost + all extra charges combined */
    public double getTotalWithExtras() {
        double extras = getExtraCharges().stream().mapToDouble(BillItem::getAmount).sum();
        return totalAmount + extras;
    }
}