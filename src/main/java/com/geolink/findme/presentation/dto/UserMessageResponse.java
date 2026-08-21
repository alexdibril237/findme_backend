package com.geolink.findme.presentation.dto;

import java.time.Instant;
import java.util.UUID;

public record UserMessageResponse(
        UUID id,
        String sujet,
        String message,
        boolean lu,
        Instant dateCreation
) {
}
