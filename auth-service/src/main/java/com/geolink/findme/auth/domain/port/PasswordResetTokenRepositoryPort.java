package com.geolink.findme.auth.domain.port;

import com.geolink.findme.auth.domain.model.PasswordResetToken;

import java.util.Optional;

public interface PasswordResetTokenRepositoryPort {

    PasswordResetToken save(PasswordResetToken token);

    Optional<PasswordResetToken> findByTokenHash(String tokenHash);
}