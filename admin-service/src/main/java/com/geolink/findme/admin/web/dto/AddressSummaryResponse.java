package com.geolink.findme.admin.web.dto;

import java.time.Instant;
import java.util.UUID;

public record AddressSummaryResponse(
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
