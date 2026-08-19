package com.geolink.findme.presentation.dto;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        UserProfileResponse user
) {
}
