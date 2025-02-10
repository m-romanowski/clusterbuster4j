package dev.marcinromanowski.clusterbuster4j.sql.spatial;

public record LatLng(double lat, double lng) {

    private static final int EARTH_RADIUS_KM = 6_371;
    private static final int MIN_LATITUDE = -90;
    private static final int MAX_LATITUDE = 90;
    private static final int MIN_LONGITUDE = -180;
    private static final int MAX_LONGITUDE = 180;

    public LatLng {
        if (lat < MIN_LATITUDE || lat > MAX_LATITUDE) {
            throw new IllegalArgumentException("Latitude must be in range [-90, 90]");
        }

        if (lng < MIN_LONGITUDE || lng > MAX_LONGITUDE) {
            throw new IllegalArgumentException("Longitude must be in range [-180, 180]");
        }
    }

    private static double haversine(double x) {
        final double sinHalf = Math.sin(x * 0.5);
        return sinHalf * sinHalf;
    }

    private static double arcHaversine(double x) {
        return 2 * Math.asin(Math.sqrt(x));
    }

    private static double haversineDistance(double lat1, double lat2, double dLng) {
        return haversine(lat1 - lat2) + haversine(dLng) * Math.cos(lat1) * Math.cos(lat2);
    }

    private static double distanceRadians(double lat1, double lng1, double lat2, double lng2) {
        return arcHaversine(haversineDistance(lat1, lat2, lng1 - lng2));
    }

    private static double computeAngleBetween(LatLng from, LatLng to) {
        return distanceRadians(Math.toRadians(from.lat()), Math.toRadians(from.lng()), Math.toRadians(to.lat()), Math.toRadians(to.lng()));
    }

    public double computeDistanceKm(LatLng to) {
        return computeAngleBetween(this, to) * EARTH_RADIUS_KM;
    }

}
