package com.geolink.findme.auth.web.dto;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        UserProfileResponse user
) {
}
