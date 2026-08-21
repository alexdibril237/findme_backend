package com.geolink.findme.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record SendMessageRequest(
        @NotNull UUID destinataireId,
        @NotBlank @Size(max = 150) String sujet,
        @NotBlank String message
) {
}
