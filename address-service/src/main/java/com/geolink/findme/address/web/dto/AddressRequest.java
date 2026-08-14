package com.geolink.findme.address.web.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;

public record AddressRequest(
        @NotBlank String pays,
        @NotBlank String ville,
        @NotBlank String quartier,
        @NotBlank String rue,
        @NotBlank String numero,
        String codePostal,
        @DecimalMin(value = "-90", inclusive = true) @DecimalMax(value = "90", inclusive = true) Double latitude,
        @DecimalMin(value = "-180", inclusive = true) @DecimalMax(value = "180", inclusive = true) Double longitude
) {
}
