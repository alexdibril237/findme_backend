package com.geolink.findme.business.service;

import com.geolink.findme.business.exception.AccountDisabledException;
import com.geolink.findme.business.exception.EmailAlreadyUsedException;
import com.geolink.findme.business.exception.InvalidCredentialsException;
import com.geolink.findme.business.exception.InvalidOrExpiredTokenException;
import com.geolink.findme.business.model.AccountStatus;
import com.geolink.findme.business.model.Role;
import com.geolink.findme.business.validation.EmailPolicy;
import com.geolink.findme.business.validation.PasswordPolicy;
import com.geolink.findme.data.entity.PasswordResetToken;
import com.geolink.findme.data.entity.RefreshToken;
import com.geolink.findme.data.entity.User;
import com.geolink.findme.data.repository.PasswordResetTokenRepository;
import com.geolink.findme.data.repository.RefreshTokenRepository;
import com.geolink.findme.data.repository.UserRepository;
import com.geolink.findme.security.EmailService;
import com.geolink.findme.security.JwtService;
import com.geolink.findme.security.SecureTokenGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final SecureTokenGenerator secureTokenGenerator;
    private final EmailService emailService;
    private final Duration refreshTokenTtl;
    private final Duration resetTokenTtl;

    public AuthServiceImpl(UserRepository userRepository, RefreshTokenRepository refreshTokenRepository,
                            PasswordResetTokenRepository passwordResetTokenRepository, PasswordEncoder passwordEncoder,
                            JwtService jwtService, SecureTokenGenerator secureTokenGenerator, EmailService emailService,
                            @Value("${jwt.refresh-token-ttl-days:7}") long refreshTokenTtlDays,
                            @Value("${jwt.reset-token-ttl-minutes:30}") long resetTokenTtlMinutes) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.secureTokenGenerator = secureTokenGenerator;
        this.emailService = emailService;
        this.refreshTokenTtl = Duration.ofDays(refreshTokenTtlDays);
        this.resetTokenTtl = Duration.ofMinutes(resetTokenTtlMinutes);
    }

    @Override
    @Transactional
    public AuthResult signup(String rawEmail, String rawPassword, String firstName, String lastName) {
        String email = EmailPolicy.normalize(rawEmail);
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyUsedException(rawEmail);
        }
        PasswordPolicy.validate(rawPassword);

        Instant now = Instant.now();
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setRole(Role.USER);
        user.setStatus(AccountStatus.ACTIVE);
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        user = userRepository.save(user);

        return issueAuthResult(user);
    }

    @Override
    @Transactional
    public AuthResult signin(String rawEmail, String rawPassword) {
        String email = EmailPolicy.normalize(rawEmail);
        User user = userRepository.findByEmail(email)
                .filter(u -> passwordEncoder.matches(rawPassword, u.getPasswordHash()))
                .orElseThrow(InvalidCredentialsException::new);

        if (user.getStatus() != AccountStatus.ACTIVE) {
            throw new AccountDisabledException();
        }

        user.setLastLoginAt(Instant.now());
        userRepository.save(user);

        return issueAuthResult(user);
    }

    @Override
    @Transactional
    public AuthResult refresh(String rawRefreshToken) {
        String presentedHash = secureTokenGenerator.hash(rawRefreshToken);
        RefreshToken stored = refreshTokenRepository.findByTokenHash(presentedHash)
                .orElseThrow(() -> new InvalidOrExpiredTokenException("Refresh token invalide"));

        if (stored.isRevoked()) {
            // Rejeu d'un refresh token déjà utilisé : signe probable de vol -> on révoque tout par précaution.
            refreshTokenRepository.revokeAllForUser(stored.getUserId());
            throw new InvalidOrExpiredTokenException("Refresh token déjà utilisé, toutes les sessions ont été révoquées");
        }
        if (!Instant.now().isBefore(stored.getExpiresAt())) {
            throw new InvalidOrExpiredTokenException("Refresh token expiré");
        }

        User user = userRepository.findById(stored.getUserId())
                .orElseThrow(() -> new InvalidOrExpiredTokenException("Refresh token invalide"));
        if (user.getStatus() != AccountStatus.ACTIVE) {
            throw new AccountDisabledException();
        }

        stored.setRevoked(true);
        refreshTokenRepository.save(stored);

        return issueAuthResult(user);
    }

    @Override
    @Transactional
    public void logout(String rawRefreshToken) {
        // Idempotent : un token déjà révoqué/inconnu ne doit pas faire échouer le logout côté client.
        refreshTokenRepository.findByTokenHash(secureTokenGenerator.hash(rawRefreshToken))
                .ifPresent(token -> {
                    token.setRevoked(true);
                    refreshTokenRepository.save(token);
                });
    }

    /**
     * Ne révèle jamais si l'email existe : ne renvoie aucune information exploitable au contrôleur.
     * Si le compte existe, un token de réinitialisation à usage unique est émis et "envoyé" ; sinon
     * l'appel est un no-op silencieux mais se comporte de façon indiscernable côté HTTP (toujours 200).
     */
    @Override
    @Transactional
    public void forgotPassword(String rawEmail) {
        String email = EmailPolicy.normalize(rawEmail);
        userRepository.findByEmail(email).ifPresent(user -> {
            // Code numérique (plutôt qu'un token opaque) : plus simple à lire/saisir
            // depuis un email, tout en réutilisant le même stockage hashé.
            String rawToken = secureTokenGenerator.generateNumericCode();
            PasswordResetToken resetToken = new PasswordResetToken();
            resetToken.setId(UUID.randomUUID());
            resetToken.setUserId(user.getId());
            resetToken.setTokenHash(secureTokenGenerator.hash(rawToken));
            resetToken.setExpiresAt(Instant.now().plus(resetTokenTtl));
            resetToken.setUsed(false);
            resetToken.setCreatedAt(Instant.now());
            passwordResetTokenRepository.save(resetToken);
            emailService.sendPasswordResetCode(user.getEmail(), rawToken);
        });
    }

    @Override
    @Transactional
    public void resetPassword(String rawToken, String newRawPassword) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByTokenHash(secureTokenGenerator.hash(rawToken))
                .orElseThrow(() -> new InvalidOrExpiredTokenException("Lien de réinitialisation invalide"));

        Instant now = Instant.now();
        if (resetToken.isUsed()) {
            throw new InvalidOrExpiredTokenException("Lien de réinitialisation déjà utilisé");
        }
        if (!now.isBefore(resetToken.getExpiresAt())) {
            throw new InvalidOrExpiredTokenException("Lien de réinitialisation expiré");
        }

        PasswordPolicy.validate(newRawPassword);

        User user = userRepository.findById(resetToken.getUserId())
                .orElseThrow(() -> new InvalidOrExpiredTokenException("Lien de réinitialisation invalide"));

        user.setPasswordHash(passwordEncoder.encode(newRawPassword));
        user.setUpdatedAt(Instant.now());
        userRepository.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);

        // Un changement de mot de passe invalide toutes les sessions existantes.
        refreshTokenRepository.revokeAllForUser(user.getId());
    }

    private AuthResult issueAuthResult(User user) {
        String accessToken = jwtService.issueAccessToken(user);
        String rawRefreshToken = secureTokenGenerator.generateOpaqueToken();

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setId(UUID.randomUUID());
        refreshToken.setUserId(user.getId());
        refreshToken.setTokenHash(secureTokenGenerator.hash(rawRefreshToken));
        refreshToken.setExpiresAt(Instant.now().plus(refreshTokenTtl));
        refreshToken.setRevoked(false);
        refreshToken.setCreatedAt(Instant.now());
        refreshTokenRepository.save(refreshToken);

        return new AuthResult(user, accessToken, rawRefreshToken);
    }
}
