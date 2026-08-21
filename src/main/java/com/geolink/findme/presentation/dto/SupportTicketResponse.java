package com.geolink.findme.presentation.dto;

import java.time.Instant;
import java.util.UUID;

public record SupportTicketResponse(
        UUID id,
        String nom,
        String email,
        String message,
        UUID userId,
        String statut,
        Instant dateCreation,
        Instant dateModification
) {
}
