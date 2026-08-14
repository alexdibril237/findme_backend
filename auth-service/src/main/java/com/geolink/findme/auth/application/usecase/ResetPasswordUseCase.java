package com.geolink.findme.auth.application.usecase;

import com.geolink.findme.auth.domain.exception.InvalidOrExpiredTokenException;
import com.geolink.findme.auth.domain.model.PasswordPolicy;
import com.geolink.findme.auth.domain.model.PasswordResetToken;
import com.geolink.findme.auth.domain.model.User;
import com.geolink.findme.auth.domain.port.PasswordHasherPort;
import com.geolink.findme.auth.domain.port.PasswordResetTokenRepositoryPort;
import com.geolink.findme.auth.domain.port.RefreshTokenRepositoryPort;
import com.geolink.findme.auth.domain.port.SecureTokenPort;
import com.geolink.findme.auth.domain.port.UserRepositoryPort;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

public class ResetPasswordUseCase {

    private final UserRepositoryPort userRepository;
    private final PasswordResetTokenRepositoryPort resetTokenRepository;
    private final RefreshTokenRepositoryPort refreshTokenRepository;
    private final PasswordHasherPort passwordHasher;
    private final SecureTokenPort secureToken;

    public ResetPasswordUseCase(UserRepositoryPort userRepository,
                                 PasswordResetTokenRepositoryPort resetTokenRepository,
                                 RefreshTokenRepositoryPort refreshTokenRepository,
                                 PasswordHasherPort passwordHasher, SecureTokenPort secureToken) {
        this.userRepository = userRepository;
        this.resetTokenRepository = resetTokenRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordHasher = passwordHasher;
        this.secureToken = secureToken;
    }

    public record Command(String token, String newRawPassword) {
    }

    @Transactional
    public void execute(Command command) {
        PasswordResetToken resetToken = resetTokenRepository.findByTokenHash(secureToken.hash(command.token()))
                .orElseThrow(() -> new InvalidOrExpiredTokenException("Lien de réinitialisation invalide"));

        Instant now = Instant.now();
        if (resetToken.isUsed()) {
            throw new InvalidOrExpiredTokenException("Lien de réinitialisation déjà utilisé");
        }
        if (resetToken.isExpired(now)) {
            throw new InvalidOrExpiredTokenException("Lien de réinitialisation expiré");
        }

        PasswordPolicy.validate(command.newRawPassword());

        User user = userRepository.findById(resetToken.getUserId())
                .orElseThrow(() -> new InvalidOrExpiredTokenException("Lien de réinitialisation invalide"));

        user.changePassword(passwordHasher.hash(command.newRawPassword()));
        userRepository.save(user);

        resetToken.markUsed();
        resetTokenRepository.save(resetToken);

        // Un changement de mot de passe invalide toutes les sessions existantes.
        refreshTokenRepository.revokeAllForUser(user.getId());
    }
}