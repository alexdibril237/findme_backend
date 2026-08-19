package com.geolink.findme.business.validation;

public final class GeoPointPolicy {

    private GeoPointPolicy() {
    }

    public static void validate(Double latitude, Double longitude) {
        if (latitude != null && (latitude < -90 || latitude > 90)) {
            throw new IllegalArgumentException("Latitude invalide : " + latitude);
        }
        if (longitude != null && (longitude < -180 || longitude > 180)) {
            throw new IllegalArgumentException("Longitude invalide : " + longitude);
        }
    }
}
