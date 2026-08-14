package com.geolink.findme.address.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class Address {

    private final UUID id;
    private final UUID userId;
    private String country;
    private String city;
    private String district;
    private String street;
    private String houseNumber;
    private String postalCode;
    private GeoPoint location;
    private String photoUrl;
    private final Instant createdAt;
    private Instant updatedAt;

    private Address(UUID id, UUID userId, String country, String city, String district, String street,
                     String houseNumber, String postalCode, GeoPoint location, String photoUrl,
                     Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.userId = userId;
        this.country = country;
        this.city = city;
        this.district = district;
        this.street = street;
        this.houseNumber = houseNumber;
        this.postalCode = postalCode;
        this.location = location;
        this.photoUrl = photoUrl;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Address createNew(UUID userId, String country, String city, String district, String street,
                                     String houseNumber, String postalCode, GeoPoint location) {
        Instant now = Instant.now();
        return new Address(UUID.randomUUID(), userId, country, city, district, street, houseNumber,
                postalCode, location, null, now, now);
    }

    public static Address rehydrate(UUID id, UUID userId, String country, String city, String district,
                                     String street, String houseNumber, String postalCode, GeoPoint location,
                                     String photoUrl, Instant createdAt, Instant updatedAt) {
        return new Address(id, userId, country, city, district, street, houseNumber, postalCode, location,
                photoUrl, createdAt, updatedAt);
    }

    public boolean belongsTo(UUID candidateUserId) {
        return this.userId.equals(candidateUserId);
    }

    public boolean sameIdentityAs(String country, String city, String district, String street, String houseNumber) {
        return this.country.equalsIgnoreCase(country)
                && this.city.equalsIgnoreCase(city)
                && this.district.equalsIgnoreCase(district)
                && this.street.equalsIgnoreCase(street)
                && this.houseNumber.equalsIgnoreCase(houseNumber);
    }

    public void updateDetails(String country, String city, String district, String street, String houseNumber,
                               String postalCode, GeoPoint location) {
        this.country = Objects.requireNonNull(country);
        this.city = Objects.requireNonNull(city);
        this.district = Objects.requireNonNull(district);
        this.street = Objects.requireNonNull(street);
        this.houseNumber = Objects.requireNonNull(houseNumber);
        this.postalCode = postalCode;
        this.location = location;
        this.updatedAt = Instant.now();
    }

    public void attachPhoto(String url) {
        this.photoUrl = Objects.requireNonNull(url);
        this.updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getCountry() {
        return country;
    }

    public String getCity() {
        return city;
    }

    public String getDistrict() {
        return district;
    }

    public String getStreet() {
        return street;
    }

    public String getHouseNumber() {
        return houseNumber;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public GeoPoint getLocation() {
        return location;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
