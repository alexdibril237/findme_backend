package com.geolink.findme.auth.application.usecase;

import com.geolink.findme.auth.domain.model.User;

public record AuthResult(User user, String accessToken, String refreshToken) {
}