package com.geolink.findme.security;

import com.geolink.findme.business.model.Role;

import java.util.UUID;

public record TokenClaims(UUID userId, String email, Role role) {
}
