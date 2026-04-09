package com.hotel.utils;

import com.hotel.models.Booking;
import com.hotel.models.Feedback;
import com.hotel.models.Room;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FileHandler {
    private static final String ROOMS_FILE = "rooms_data.dat";
    private static final String BOOKINGS_FILE = "bookings_data.dat";
    private static final String FEEDBACK_FILE = "feedback_data.dat";

    // --- Rooms Persistence ---
    public static void saveRooms(List<Room> rooms) {
        saveToFile(ROOMS_FILE, rooms);
    }

    public static List<Room> loadRooms() {
        return loadFromFile(ROOMS_FILE);
    }

    // --- Bookings Persistence ---
    public static void saveBookings(List<Booking> bookings) {
        saveToFile(BOOKINGS_FILE, bookings);
    }

    public static List<Booking> loadBookings() {
        return loadFromFile(BOOKINGS_FILE);
    }

    // --- Feedback Persistence ---
    public static void saveFeedback(List<Feedback> feedback) {
        saveToFile(FEEDBACK_FILE, feedback);
    }

    public static List<Feedback> loadFeedback() {
        return loadFromFile(FEEDBACK_FILE);
    }

    // --- Generic Helper Methods ---
    private static <T> void saveToFile(String fileName, List<T> data) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(fileName))) {
            oos.writeObject(data);
        } catch (IOException e) {
            System.err.println("Error saving to " + fileName + ": " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> List<T> loadFromFile(String fileName) {
        File file = new File(fileName);
        if (!file.exists()) {
            return new ArrayList<>();
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fileName))) {
            return (List<T>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading from " + fileName + ": " + e.getMessage());
            return new ArrayList<>();
        }
    }
}