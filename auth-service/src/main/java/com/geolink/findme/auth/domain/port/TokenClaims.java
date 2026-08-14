package com.geolink.findme.auth.domain.port;

import com.geolink.findme.auth.domain.model.Role;

import java.util.UUID;

public record TokenClaims(UUID userId, String email, Role role) {
}