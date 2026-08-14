package com.geolink.findme.admin.domain.model;

import java.time.Instant;
import java.util.UUID;

/** Vue agrégée en lecture seule, reconstruite à la demande depuis auth-service (pas de persistance). */
public record UserSummary(
        UUID id,
        String email,
        String prenom,
        String nom,
        Role role,
        String statutCompte,
        Instant dateCreation,
        Instant dateDerniereConnexion
) {
}
