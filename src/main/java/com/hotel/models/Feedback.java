package com.hotel.models;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents guest feedback / review for a completed stay.
 * Stores a 1-5 star rating, comment, and timestamp.
 */
public class Feedback implements Serializable {
    private static final long serialVersionUID = 1L;
    private String bookingId;
    private String guestName;
    private int rating; // 1 to 5
    private String comment;
    private LocalDateTime timestamp;

    public Feedback(String bookingId, String guestName, int rating, String comment) {
        this.bookingId = bookingId;
        this.guestName = guestName;
        this.rating = Math.max(1, Math.min(5, rating)); // clamp 1-5
        this.comment = comment;
        this.timestamp = LocalDateTime.now();
    }

    public String getBookingId() { return bookingId; }
    public String getGuestName() { return guestName; }
    public int getRating() { return rating; }
    public String getComment() { return comment; }
    public LocalDateTime getTimestamp() { return timestamp; }

    /** Returns visual star representation like ★★★★☆ */
    public String getStars() {
        return "\u2605".repeat(rating) + "\u2606".repeat(5 - rating);
    }

    public String getFormattedDate() {
        return timestamp.format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm"));
    }
}
