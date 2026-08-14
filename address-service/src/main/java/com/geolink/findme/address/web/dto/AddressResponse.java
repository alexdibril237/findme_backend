package com.geolink.findme.address.web.dto;

import java.time.Instant;
import java.util.UUID;

public record AddressResponse(
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
