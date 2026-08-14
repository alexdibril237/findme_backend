package com.geolink.findme.auth.web.dto;

import java.time.Instant;
import java.util.UUID;

public record UserProfileResponse(
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
