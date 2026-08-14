package com.geolink.findme.auth.application.usecase;

import com.geolink.findme.auth.domain.exception.AccountDisabledException;
import com.geolink.findme.auth.domain.exception.InvalidCredentialsException;
import com.geolink.findme.auth.domain.model.Email;
import com.geolink.findme.auth.domain.model.RefreshToken;
import com.geolink.findme.auth.domain.model.User;
import com.geolink.findme.auth.domain.port.JwtIssuerPort;
import com.geolink.findme.auth.domain.port.PasswordHasherPort;
import com.geolink.findme.auth.domain.port.RefreshTokenRepositoryPort;
import com.geolink.findme.auth.domain.port.SecureTokenPort;
import com.geolink.findme.auth.domain.port.UserRepositoryPort;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;

public class SigninUseCase {

    private final UserRepositoryPort userRepository;
    private final RefreshTokenRepositoryPort refreshTokenRepository;
    private final PasswordHasherPort passwordHasher;
    private final JwtIssuerPort jwtIssuer;
    private final SecureTokenPort secureToken;
    private final Duration refreshTokenTtl;

    public SigninUseCase(UserRepositoryPort userRepository, RefreshTokenRepositoryPort refreshTokenRepository,
                          PasswordHasherPort passwordHasher, JwtIssuerPort jwtIssuer, SecureTokenPort secureToken,
                          Duration refreshTokenTtl) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordHasher = passwordHasher;
        this.jwtIssuer = jwtIssuer;
        this.secureToken = secureToken;
        this.refreshTokenTtl = refreshTokenTtl;
    }

    public record Command(String email, String rawPassword) {
    }

    @Transactional
    public AuthResult execute(Command command) {
        User user = userRepository.findByEmail(new Email(command.email()))
                .filter(u -> passwordHasher.matches(command.rawPassword(), u.getPasswordHash()))
                .orElseThrow(InvalidCredentialsException::new);

        if (!user.isActive()) {
            throw new AccountDisabledException();
        }

        user.recordLogin();
        userRepository.save(user);

        String accessToken = jwtIssuer.issueAccessToken(user);
        String rawRefreshToken = secureToken.generateOpaqueToken();
        refreshTokenRepository.save(RefreshToken.issue(user.getId(), secureToken.hash(rawRefreshToken),
                Instant.now().plus(refreshTokenTtl)));

        return new AuthResult(user, accessToken, rawRefreshToken);
    }
}