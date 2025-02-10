package dev.marcinromanowski.clusterbuster4j.sql.spatial;

public record BoundingBox(LatLng ne, LatLng sw) {

    public LatLng center() {
        final double latNorth = ne.lat();
        final double latSouth = sw.lat();
        final double lngEast = ne.lng();
        final double lngWest = sw.lng();
        return new LatLng((latNorth + latSouth) / 2, (lngEast + lngWest) / 2);
    }

}
