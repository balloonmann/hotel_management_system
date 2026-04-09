package com.hotel.utils;

import com.hotel.models.BillItem;
import com.hotel.models.Booking;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Service to handle bill generation and invoice formatting.
 * Demonstrates clean separation of concerns and formatting logic.
 */
public class BillingService {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static String generateInvoice(Booking booking) {
        StringBuilder sb = new StringBuilder();
        sb.append("========================================\n");
        sb.append("          HOTEL MANAGEMENT SYSTEM       \n");
        sb.append("               OFFICIAL BILL            \n");
        sb.append("========================================\n");
        sb.append("Invoice Date: ").append(LocalDateTime.now().format(formatter)).append("\n");
        sb.append("Booking ID:   ").append(booking.getBookingId()).append("\n");
        sb.append("----------------------------------------\n");
        sb.append("GUEST DETAILS:\n");
        sb.append("Name:         ").append(booking.getGuest().getName()).append("\n");
        sb.append("Contact:      ").append(booking.getGuest().getContactNumber()).append("\n");
        sb.append("----------------------------------------\n");
        sb.append("ROOM DETAILS:\n");
        sb.append("Room No:      ").append(booking.getRoom().getRoomNumber()).append("\n");
        sb.append("Room Type:    ").append(booking.getRoom().getRoomType()).append("\n");
        sb.append("Base Price:   \u20B9").append(String.format("%.2f", booking.getRoom().getPricePerNight())).append("/night\n");
        sb.append("Duration:     ").append(booking.getDaysStayed()).append(" nights\n");

        if (booking.getCheckIn() != null && booking.getCheckOut() != null) {
            sb.append("Check-in:     ").append(booking.getCheckIn()).append("\n");
            sb.append("Check-out:    ").append(booking.getCheckOut()).append("\n");
        }

        sb.append("----------------------------------------\n");
        sb.append("AMENITIES:\n");
        for (String amenity : booking.getRoom().getAmenitiesList()) {
            sb.append(" - ").append(amenity).append("\n");
        }

        // Special Requests
        if (!booking.getSpecialRequests().isEmpty()) {
            sb.append("----------------------------------------\n");
            sb.append("SPECIAL REQUESTS:\n");
            for (String req : booking.getSpecialRequests()) {
                sb.append(" \u2022 ").append(req).append("\n");
            }
        }

        // Extra Charges (Itemized)
        if (!booking.getExtraCharges().isEmpty()) {
            sb.append("----------------------------------------\n");
            sb.append("ADDITIONAL CHARGES:\n");
            for (BillItem item : booking.getExtraCharges()) {
                sb.append(String.format(" - %-22s \u20B9%.2f\n", item.getDescription(), item.getAmount()));
            }
            double extrasTotal = booking.getExtraCharges().stream().mapToDouble(BillItem::getAmount).sum();
            sb.append("                         ----------\n");
            sb.append(String.format(" Extras Subtotal:        \u20B9%.2f\n", extrasTotal));
        }

        sb.append("----------------------------------------\n");
        sb.append("ROOM CHARGES:  \u20B9").append(String.format("%.2f", booking.getTotalAmount())).append("\n");
        if (!booking.getExtraCharges().isEmpty()) {
            sb.append("GRAND TOTAL:   \u20B9").append(String.format("%.2f", booking.getTotalWithExtras())).append("\n");
        } else {
            sb.append("TOTAL AMOUNT:  \u20B9").append(String.format("%.2f", booking.getTotalAmount())).append("\n");
        }
        sb.append("========================================\n");
        sb.append("      Thank you for staying with us!    \n");
        sb.append("========================================\n");
        return sb.toString();
    }
}
