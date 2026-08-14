package com.geolink.findme.auth.domain.model;

import java.time.Instant;
import java.util.UUID;

public class RefreshToken {

    private final UUID id;
    private final UUID userId;
    private final String tokenHash;
    private final Instant expiresAt;
    private boolean revoked;
    private final Instant createdAt;

    private RefreshToken(UUID id, UUID userId, String tokenHash, Instant expiresAt, boolean revoked, Instant createdAt) {
        this.id = id;
        this.userId = userId;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
        this.revoked = revoked;
        this.createdAt = createdAt;
    }

    public static RefreshToken issue(UUID userId, String tokenHash, Instant expiresAt) {
        return new RefreshToken(UUID.randomUUID(), userId, tokenHash, expiresAt, false, Instant.now());
    }

    public static RefreshToken rehydrate(UUID id, UUID userId, String tokenHash, Instant expiresAt,
                                          boolean revoked, Instant createdAt) {
        return new RefreshToken(id, userId, tokenHash, expiresAt, revoked, createdAt);
    }

    public boolean isValid(Instant now) {
        return !revoked && now.isBefore(expiresAt);
    }

    public void revoke() {
        this.revoked = true;
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public boolean isRevoked() {
        return revoked;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}