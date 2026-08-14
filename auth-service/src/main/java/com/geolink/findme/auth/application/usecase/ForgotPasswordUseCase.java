package com.geolink.findme.auth.application.usecase;

import com.geolink.findme.auth.domain.model.Email;
import com.geolink.findme.auth.domain.model.PasswordResetToken;
import com.geolink.findme.auth.domain.port.EmailSenderPort;
import com.geolink.findme.auth.domain.port.PasswordResetTokenRepositoryPort;
import com.geolink.findme.auth.domain.port.SecureTokenPort;
import com.geolink.findme.auth.domain.port.UserRepositoryPort;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;

public class ForgotPasswordUseCase {

    private final UserRepositoryPort userRepository;
    private final PasswordResetTokenRepositoryPort resetTokenRepository;
    private final SecureTokenPort secureToken;
    private final EmailSenderPort emailSender;
    private final Duration resetTokenTtl;

    public ForgotPasswordUseCase(UserRepositoryPort userRepository,
                                  PasswordResetTokenRepositoryPort resetTokenRepository,
                                  SecureTokenPort secureToken, EmailSenderPort emailSender, Duration resetTokenTtl) {
        this.userRepository = userRepository;
        this.resetTokenRepository = resetTokenRepository;
        this.secureToken = secureToken;
        this.emailSender = emailSender;
        this.resetTokenTtl = resetTokenTtl;
    }

    public record Command(String email) {
    }

    /**
     * Ne révèle jamais si l'email existe : ne renvoie aucune information exploitable au contrôleur.
     * Si le compte existe, un token de réinitialisation à usage unique est émis et "envoyé" ; sinon
     * l'appel est un no-op silencieux mais se comporte de façon indiscernable côté HTTP (toujours 200).
     */
    @Transactional
    public void execute(Command command) {
        userRepository.findByEmail(new Email(command.email()))
                .ifPresent(user -> {
                    String rawToken = secureToken.generateOpaqueToken();
                    resetTokenRepository.save(PasswordResetToken.issue(user.getId(), secureToken.hash(rawToken),
                            Instant.now().plus(resetTokenTtl)));
                    emailSender.sendPasswordResetLink(user.getEmail().value(), rawToken);
                });
    }
}