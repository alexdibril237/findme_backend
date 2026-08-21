package com.geolink.findme.presentation.dto;

import com.geolink.findme.business.model.AddressStatus;

import java.time.Instant;
import java.util.UUID;

public record AddressResponse(
        UUID id,
        UUID userId,
        String label,
        String pays,
        String ville,
        String quartier,
        String rue,
        String numero,
        String codePostal,
        Double latitude,
        Double longitude,
        String urlPhoto,
        AddressStatus status,
        String addressCode,
        String countryCode,
        Instant dateCreation,
        Instant dateModification
) {
}
