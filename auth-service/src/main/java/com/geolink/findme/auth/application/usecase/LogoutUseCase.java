package com.geolink.findme.auth.application.usecase;

import com.geolink.findme.auth.domain.model.RefreshToken;
import com.geolink.findme.auth.domain.port.RefreshTokenRepositoryPort;
import com.geolink.findme.auth.domain.port.SecureTokenPort;
import org.springframework.transaction.annotation.Transactional;

public class LogoutUseCase {

    private final RefreshTokenRepositoryPort refreshTokenRepository;
    private final SecureTokenPort secureToken;

    public LogoutUseCase(RefreshTokenRepositoryPort refreshTokenRepository, SecureTokenPort secureToken) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.secureToken = secureToken;
    }

    public record Command(String refreshToken) {
    }

    @Transactional
    public void execute(Command command) {
        refreshTokenRepository.findByTokenHash(secureToken.hash(command.refreshToken()))
                .ifPresent(token -> {
                    token.revoke();
                    refreshTokenRepository.save(token);
                });
        // Idempotent : un token déjà révoqué/inconnu ne doit pas faire échouer le logout côté client.
    }
}