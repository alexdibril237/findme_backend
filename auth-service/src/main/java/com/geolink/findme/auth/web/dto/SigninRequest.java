package com.geolink.findme.auth.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record SigninRequest(
        @NotBlank @Email String email,
        @NotBlank String motDePasse
) {
}
