package com.geolink.findme.admin.domain.port;

import com.geolink.findme.admin.domain.model.Role;

import java.util.UUID;

public record TokenClaims(UUID userId, String email, Role role) {
}
