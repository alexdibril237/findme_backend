package com.geolink.findme.business.service;

import com.geolink.findme.data.entity.User;

public record AuthResult(User user, String accessToken, String refreshToken) {
}
