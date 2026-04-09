package com.hotel.utils;

import com.hotel.models.Booking;
import com.hotel.models.Feedback;
import com.hotel.models.HousekeepingStatus;
import com.hotel.models.Room;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Singleton class to manage global hotel state (Rooms, Bookings, Feedback).
 * Encapsulates management logic and state persistence.
 */
public class HotelManager {
    private static HotelManager instance;
    private List<Room> rooms;
    private List<Booking> bookings;
    private List<Feedback> feedbackList;

    private HotelManager() {
        this.rooms = FileHandler.loadRooms();
        this.bookings = FileHandler.loadBookings();
        this.feedbackList = FileHandler.loadFeedback();
    }

    public static HotelManager getInstance() {
        if (instance == null) {
            instance = new HotelManager();
        }
        return instance;
    }

    // --- Room Management ---
    public void addRoom(Room room) {
        rooms.add(room);
        saveState();
    }

    public List<Room> getRooms() {
        return rooms;
    }

    public List<Room> getAvailableRooms() {
        return rooms.stream()
                .filter(Room::isAvailable)
                .filter(r -> !r.isUnderMaintenance())
                .collect(Collectors.toList());
    }

    public List<Room> filterRooms(String type, double maxPrice) {
        return rooms.stream()
                .filter(Room::isAvailable)
                .filter(r -> !r.isUnderMaintenance())
                .filter(r -> type.equals("All") || r.getRoomType().equalsIgnoreCase(type))
                .filter(r -> maxPrice <= 0 || r.getPricePerNight() <= maxPrice)
                .collect(Collectors.toList());
    }

    // --- Room Editing ---
    public void updateRoomPrice(int roomNumber, double newPrice) {
        rooms.stream()
             .filter(r -> r.getRoomNumber() == roomNumber)
             .findFirst()
             .ifPresent(r -> r.setPricePerNight(newPrice));
        saveState();
    }

    public void toggleMaintenance(Room room) {
        if (room.isUnderMaintenance()) {
            room.setUnderMaintenance(false);
            room.setAvailable(true);
        } else {
            room.setUnderMaintenance(true);
            room.setAvailable(false);
        }
        saveState();
    }

    // --- Housekeeping ---
    public void cycleHousekeeping(Room room) {
        HousekeepingStatus current = room.getHousekeepingStatus();
        room.setHousekeepingStatus(current.next());
        saveState();
    }

    /** Auto-mark a room as dirty after checkout */
    public void markDirtyAfterCheckout(Room room) {
        room.setHousekeepingStatus(HousekeepingStatus.DIRTY);
        saveState();
    }

    // --- Booking Management ---
    public void addBooking(Booking booking) {
        bookings.add(booking);
        booking.getRoom().setAvailable(false);
        saveState();
    }

    public List<Booking> getBookings() {
        return bookings;
    }

    public void cancelBooking(Booking booking) {
        booking.setCancelled(true);
        booking.getRoom().setAvailable(true);
        saveState();
    }

    // --- Checkout Reminders ---
    /** Returns bookings where checkout date is today or overdue, and the room is still occupied */
    public List<Booking> getCheckoutReminders() {
        LocalDate today = LocalDate.now();
        return bookings.stream()
                .filter(b -> !b.isCancelled())
                .filter(b -> !b.getRoom().isAvailable())
                .filter(b -> b.getCheckOut() != null)
                .filter(b -> !b.getCheckOut().isAfter(today))
                .collect(Collectors.toList());
    }

    /** Check if a specific room has an overdue/due-today checkout */
    public boolean isCheckoutDue(int roomNumber) {
        LocalDate today = LocalDate.now();
        return bookings.stream()
                .filter(b -> !b.isCancelled())
                .filter(b -> b.getRoom().getRoomNumber() == roomNumber)
                .filter(b -> !b.getRoom().isAvailable())
                .filter(b -> b.getCheckOut() != null)
                .anyMatch(b -> !b.getCheckOut().isAfter(today));
    }

    // --- Guest Lookup ---
    public List<Booking> searchByGuestName(String name) {
        if (name == null || name.trim().isEmpty()) return new ArrayList<>();
        String lower = name.toLowerCase();
        return bookings.stream()
                .filter(b -> b.getGuest().getName().toLowerCase().contains(lower))
                .collect(Collectors.toList());
    }

    // --- Overlap / Date Validation ---
    public boolean hasOverlappingBooking(int roomNumber, LocalDate checkIn, LocalDate checkOut) {
        return bookings.stream()
                .filter(b -> !b.isCancelled())
                .filter(b -> b.getRoom().getRoomNumber() == roomNumber)
                .filter(b -> b.getCheckIn() != null && b.getCheckOut() != null)
                .anyMatch(b -> checkIn.isBefore(b.getCheckOut()) && checkOut.isAfter(b.getCheckIn()));
    }

    // --- Feedback / Reviews ---
    public void addFeedback(Feedback feedback) {
        feedbackList.add(feedback);
        FileHandler.saveFeedback(feedbackList);
    }

    public List<Feedback> getAllFeedback() {
        return feedbackList;
    }

    public double getAverageRating() {
        if (feedbackList.isEmpty()) return 0;
        return feedbackList.stream().mapToInt(Feedback::getRating).average().orElse(0);
    }

    // --- Analytics ---
    public double getOccupancyRate() {
        if (rooms.isEmpty()) return 0.0;
        long occupied = rooms.stream().filter(r -> !r.isAvailable()).count();
        return ((double) occupied / rooms.size()) * 100;
    }

    public double getTotalRevenue() {
        return bookings.stream()
                .filter(b -> !b.isCancelled())
                .mapToDouble(Booking::getTotalWithExtras).sum();
    }

    public long getActiveBookingsCount() {
        return bookings.stream().filter(b -> !b.isCancelled()).count();
    }

    // --- Persistence ---
    public void saveState() {
        FileHandler.saveRooms(rooms);
        FileHandler.saveBookings(bookings);
    }
}
