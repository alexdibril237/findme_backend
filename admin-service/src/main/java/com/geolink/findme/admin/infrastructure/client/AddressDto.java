package com.geolink.findme.admin.infrastructure.client;

import java.time.Instant;
import java.util.UUID;

/** Miroir de {@code AddressResponse} côté address-service. */
public record AddressDto(
        UUID id,
        UUID userId,
        String pays,
        String ville,
        String quartier,
        String rue,
        String numero,
        String codePostal,
        Double latitude,
        Double longitude,
        String urlPhoto,
        Instant dateCreation,
        Instant dateModification
) {
}
