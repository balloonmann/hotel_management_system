package com.hotel.models;

import java.io.Serializable;

/**
 * Represents an additional charge added to a booking (laundry, room service, spa, etc.).
 * Demonstrates encapsulation and value-object pattern.
 */
public class BillItem implements Serializable {
    private static final long serialVersionUID = 1L;
    private String description;
    private double amount;

    public BillItem(String description, double amount) {
        this.description = description;
        this.amount = amount;
    }

    public String getDescription() { return description; }
    public double getAmount() { return amount; }

    @Override
    public String toString() {
        return description + ": \u20B9" + String.format("%.2f", amount);
    }
}
