package com.geolink.findme.address.domain.model;

public record GeoPoint(Double latitude, Double longitude) {

    public GeoPoint {
        if (latitude != null && (latitude < -90 || latitude > 90)) {
            throw new IllegalArgumentException("Latitude invalide : " + latitude);
        }
        if (longitude != null && (longitude < -180 || longitude > 180)) {
            throw new IllegalArgumentException("Longitude invalide : " + longitude);
        }
    }
}
