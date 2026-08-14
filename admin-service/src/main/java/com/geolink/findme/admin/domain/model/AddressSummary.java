package com.geolink.findme.admin.domain.model;

import java.time.Instant;
import java.util.UUID;

/** Vue agrégée en lecture seule, reconstruite à la demande depuis address-service (pas de persistance). */
public record AddressSummary(
        UUID id,
        UUID userId,
        String pays,
        String ville,
        String quartier,
        String rue,
        String numero,
        Instant dateCreation
) {
}
