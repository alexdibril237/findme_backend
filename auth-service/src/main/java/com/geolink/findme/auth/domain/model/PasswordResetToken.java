package com.geolink.findme.auth.domain.model;

import java.time.Instant;
import java.util.UUID;

public class PasswordResetToken {

    private final UUID id;
    private final UUID userId;
    private final String tokenHash;
    private final Instant expiresAt;
    private boolean used;
    private final Instant createdAt;

    private PasswordResetToken(UUID id, UUID userId, String tokenHash, Instant expiresAt, boolean used, Instant createdAt) {
        this.id = id;
        this.userId = userId;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
        this.used = used;
        this.createdAt = createdAt;
    }

    public static PasswordResetToken issue(UUID userId, String tokenHash, Instant expiresAt) {
        return new PasswordResetToken(UUID.randomUUID(), userId, tokenHash, expiresAt, false, Instant.now());
    }

    public static PasswordResetToken rehydrate(UUID id, UUID userId, String tokenHash, Instant expiresAt,
                                                boolean used, Instant createdAt) {
        return new PasswordResetToken(id, userId, tokenHash, expiresAt, used, createdAt);
    }

    public boolean isUsable(Instant now) {
        return !used && now.isBefore(expiresAt);
    }

    public boolean isExpired(Instant now) {
        return !now.isBefore(expiresAt);
    }

    public void markUsed() {
        this.used = true;
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

    public boolean isUsed() {
        return used;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
