package com.geolink.findme.presentation.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateProfileRequest(
        @NotBlank String prenom,
        @NotBlank String nom
) {
}
