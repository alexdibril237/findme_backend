package com.geolink.findme.presentation.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AddressRequest(
        @NotBlank @Size(max = 100) String label,
        @NotBlank String pays,
        @NotBlank String ville,
        @NotBlank String quartier,
        @NotBlank String rue,
        @NotBlank String numero,
        String codePostal,
        @DecimalMin(value = "-90", inclusive = true) @DecimalMax(value = "90", inclusive = true) Double latitude,
        @DecimalMin(value = "-180", inclusive = true) @DecimalMax(value = "180", inclusive = true) Double longitude,
        @Size(max = 5) String countryCode
) {
}
