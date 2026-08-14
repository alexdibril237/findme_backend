package com.geolink.findme.auth.web.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateProfileRequest(
        @NotBlank String prenom,
        @NotBlank String nom
) {
}
