package com.geolink.findme.admin.web.dto;

import java.time.Instant;
import java.util.UUID;

public record UserSummaryResponse(
        UUID id,
        String email,
        String prenom,
        String nom,
        String role,
        String statutCompte,
        Instant dateCreation,
        Instant dateDerniereConnexion
) {
}
