package org.wfparser;

import org.cloudsimplus.distributions.ExponentialDistr;

public class DemandInterval {
    private final ExponentialDistr exponentialDistr;
    // Total number of seconds up to the end of each month in a normal year
    private static final double[] SECONDS_UP_TO_END_OF_MONTH = {
            0, // Placeholder for 0 index
            2678400, // End of January
            5097600, // End of February
            7776000, // End of March
            10368000, // End of April
            13046400, // End of May
            15638400, // End of June
            18316800, // End of July
            20995200, // End of August
            23587200, // End of September
            26265600, // End of October
            28857600, // End of November
            31536000  // End of December
    };
    public DemandInterval(long meanInterval) {
        this.exponentialDistr = new ExponentialDistr(meanInterval);
    }
    public static int getMonth(double time) {
        // Iterate through the months to find where the time fits
        for (int month = 1; month <= 12; month++) {
            if (time < SECONDS_UP_TO_END_OF_MONTH[month]) {
                return month;
            }
        }
        // If the time is beyond the end of the year, return an invalid month (e.g., -1)
        return -1;
    }
    public double nextInterval(double currTime) {
        int month = getMonth(currTime);
        double adjustmentFactor = getAdjustmentFactor(month);
        return exponentialDistr.sample() * adjustmentFactor;
    }
    private double getAdjustmentFactor(int month) {
        return switch (month) { // April
            case 4, 11 -> // November
                    0.5; // August
            case 8, 12 -> // December
                    2.0;
            default -> 1.0;
        };
    }
}
