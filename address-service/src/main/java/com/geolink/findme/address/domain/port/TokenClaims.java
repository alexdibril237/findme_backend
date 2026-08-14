package com.geolink.findme.address.domain.port;

import com.geolink.findme.address.domain.model.Role;

import java.util.UUID;

public record TokenClaims(UUID userId, String email, Role role) {
}
