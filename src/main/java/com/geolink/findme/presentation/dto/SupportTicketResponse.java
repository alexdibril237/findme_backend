package com.geolink.findme.presentation.dto;

import java.time.Instant;
import java.util.UUID;

public record SupportTicketResponse(
        UUID id,
        String nom,
        String email,
        String message,
        String statut,
        Instant dateCreation,
        Instant dateModification
) {
}
