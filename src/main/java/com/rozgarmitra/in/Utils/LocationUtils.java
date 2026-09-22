package com.rozgarmitra.in.Utils;

public class LocationUtils {

    private static final int EARTH_RADIUS_KM = 6371;

    public static double calculateDistance(double startLat, double startLong, double endLat, double endLong) {
        double dLat = Math.toRadians(endLat - startLat);
        double dLong = Math.toRadians(endLong - startLong);

        startLat = Math.toRadians(startLat);
        endLat = Math.toRadians(endLat);

        double a = haversin(dLat) + Math.cos(startLat) * Math.cos(endLat) * haversin(dLong);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_KM * c; // Returns distance in Kilometers
    }

    private static double haversin(double val) {
        return Math.pow(Math.sin(val / 2), 2);
    }
}