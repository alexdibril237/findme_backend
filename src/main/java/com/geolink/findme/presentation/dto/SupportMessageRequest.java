package com.geolink.findme.presentation.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record SupportMessageRequest(
        @NotBlank String nom,
        @NotBlank @Email String email,
        @NotBlank String message
) {
}
