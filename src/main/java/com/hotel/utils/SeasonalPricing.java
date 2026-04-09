package com.hotel.utils;

import java.time.LocalDate;

/**
 * Utility class implementing dynamic pricing based on seasonal demand.
 * Demonstrates the Strategy pattern concept — pricing varies by time period.
 */
public class SeasonalPricing {

    public static double getMultiplier(LocalDate date) {
        if (date == null) return 1.0;
        int month = date.getMonthValue();
        // Peak: Dec-Jan-Feb (winter holidays / wedding season)
        if (month == 12 || month == 1 || month == 2) return 1.50;
        // High: May-Jun (summer vacation)
        if (month == 5 || month == 6) return 1.30;
        // Shoulder: Oct-Nov (festive season)
        if (month == 10 || month == 11) return 1.20;
        // Regular: Mar-Apr, Jul-Aug-Sep
        return 1.0;
    }

    public static String getSeasonName(LocalDate date) {
        double m = getMultiplier(date);
        if (m >= 1.5) return "Peak Season";
        if (m >= 1.3) return "High Season";
        if (m >= 1.2) return "Festive Season";
        return "Regular Season";
    }

    public static String getSeasonLabel(LocalDate date) {
        double m = getMultiplier(date);
        if (m == 1.0) return getSeasonName(date);
        return String.format("%s (+%.0f%%)", getSeasonName(date), (m - 1.0) * 100);
    }
}
