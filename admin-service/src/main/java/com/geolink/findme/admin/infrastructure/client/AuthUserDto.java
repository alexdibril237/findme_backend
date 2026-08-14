package com.geolink.findme.admin.infrastructure.client;

import java.time.Instant;
import java.util.UUID;

/** Miroir de {@code UserProfileResponse} côté auth-service. */
public record AuthUserDto(
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
