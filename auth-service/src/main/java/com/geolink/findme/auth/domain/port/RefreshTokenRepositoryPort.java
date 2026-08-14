package com.geolink.findme.auth.domain.port;

import com.geolink.findme.auth.domain.model.RefreshToken;

import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepositoryPort {

    RefreshToken save(RefreshToken refreshToken);

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    void revokeAllForUser(UUID userId);
}