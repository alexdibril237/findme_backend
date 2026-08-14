package com.geolink.findme.auth.application.usecase;

import com.geolink.findme.auth.domain.exception.AccountDisabledException;
import com.geolink.findme.auth.domain.exception.InvalidOrExpiredTokenException;
import com.geolink.findme.auth.domain.model.RefreshToken;
import com.geolink.findme.auth.domain.model.User;
import com.geolink.findme.auth.domain.port.JwtIssuerPort;
import com.geolink.findme.auth.domain.port.RefreshTokenRepositoryPort;
import com.geolink.findme.auth.domain.port.SecureTokenPort;
import com.geolink.findme.auth.domain.port.UserRepositoryPort;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;

public class RefreshTokenUseCase {

    private final UserRepositoryPort userRepository;
    private final RefreshTokenRepositoryPort refreshTokenRepository;
    private final JwtIssuerPort jwtIssuer;
    private final SecureTokenPort secureToken;
    private final Duration refreshTokenTtl;

    public RefreshTokenUseCase(UserRepositoryPort userRepository, RefreshTokenRepositoryPort refreshTokenRepository,
                                JwtIssuerPort jwtIssuer, SecureTokenPort secureToken, Duration refreshTokenTtl) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtIssuer = jwtIssuer;
        this.secureToken = secureToken;
        this.refreshTokenTtl = refreshTokenTtl;
    }

    public record Command(String refreshToken) {
    }

    @Transactional
    public AuthResult execute(Command command) {
        String presentedHash = secureToken.hash(command.refreshToken());
        RefreshToken stored = refreshTokenRepository.findByTokenHash(presentedHash)
                .orElseThrow(() -> new InvalidOrExpiredTokenException("Refresh token invalide"));

        if (stored.isRevoked()) {
            // Rejeu d'un refresh token déjà utilisé : signe probable de vol -> on révoque tout par précaution.
            refreshTokenRepository.revokeAllForUser(stored.getUserId());
            throw new InvalidOrExpiredTokenException("Refresh token déjà utilisé, toutes les sessions ont été révoquées");
        }
        if (!stored.isValid(Instant.now())) {
            throw new InvalidOrExpiredTokenException("Refresh token expiré");
        }

        User user = userRepository.findById(stored.getUserId())
                .orElseThrow(() -> new InvalidOrExpiredTokenException("Refresh token invalide"));
        if (!user.isActive()) {
            throw new AccountDisabledException();
        }

        stored.revoke();
        refreshTokenRepository.save(stored);

        String newAccessToken = jwtIssuer.issueAccessToken(user);
        String newRawRefreshToken = secureToken.generateOpaqueToken();
        refreshTokenRepository.save(RefreshToken.issue(user.getId(), secureToken.hash(newRawRefreshToken),
                Instant.now().plus(refreshTokenTtl)));

        return new AuthResult(user, newAccessToken, newRawRefreshToken);
    }
}