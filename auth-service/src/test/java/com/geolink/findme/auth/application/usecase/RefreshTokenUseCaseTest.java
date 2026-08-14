package com.geolink.findme.auth.application.usecase;

import com.geolink.findme.auth.domain.exception.AccountDisabledException;
import com.geolink.findme.auth.domain.exception.InvalidOrExpiredTokenException;
import com.geolink.findme.auth.domain.model.Email;
import com.geolink.findme.auth.domain.model.RefreshToken;
import com.geolink.findme.auth.domain.model.User;
import com.geolink.findme.auth.domain.port.JwtIssuerPort;
import com.geolink.findme.auth.domain.port.RefreshTokenRepositoryPort;
import com.geolink.findme.auth.domain.port.SecureTokenPort;
import com.geolink.findme.auth.domain.port.UserRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTokenUseCaseTest {

    @Mock
    private UserRepositoryPort userRepository;
    @Mock
    private RefreshTokenRepositoryPort refreshTokenRepository;
    @Mock
    private JwtIssuerPort jwtIssuer;
    @Mock
    private SecureTokenPort secureToken;

    private RefreshTokenUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new RefreshTokenUseCase(userRepository, refreshTokenRepository, jwtIssuer, secureToken,
                Duration.ofDays(7));
    }

    @Test
    void tourne_le_refresh_token_et_emet_de_nouveaux_tokens() {
        User user = User.createNew(new Email("jane@doe.io"), "hashed", "Jane", "Doe");
        RefreshToken stored = RefreshToken.issue(user.getId(), "hashed-old", Instant.now().plusSeconds(3600));
        when(secureToken.hash("raw-old")).thenReturn("hashed-old");
        when(refreshTokenRepository.findByTokenHash("hashed-old")).thenReturn(Optional.of(stored));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(jwtIssuer.issueAccessToken(user)).thenReturn("new.access");
        when(secureToken.generateOpaqueToken()).thenReturn("raw-new");
        when(secureToken.hash("raw-new")).thenReturn("hashed-new");

        AuthResult result = useCase.execute(new RefreshTokenUseCase.Command("raw-old"));

        assertThat(result.accessToken()).isEqualTo("new.access");
        assertThat(result.refreshToken()).isEqualTo("raw-new");
        assertThat(stored.isRevoked()).as("l'ancien token doit être révoqué (rotation)").isTrue();
        // l'ancien révoqué + le nouveau émis = deux save
        verify(refreshTokenRepository, org.mockito.Mockito.times(2)).save(any(RefreshToken.class));
    }

    @Test
    void refuse_un_token_inconnu() {
        when(secureToken.hash("raw-unknown")).thenReturn("hashed-unknown");
        when(refreshTokenRepository.findByTokenHash("hashed-unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(new RefreshTokenUseCase.Command("raw-unknown")))
                .isInstanceOf(InvalidOrExpiredTokenException.class);
    }

    @Test
    void revoque_toutes_les_sessions_si_token_deja_utilise_rejeu() {
        UUID userId = UUID.randomUUID();
        RefreshToken revoked = RefreshToken.issue(userId, "hashed-old", Instant.now().plusSeconds(3600));
        revoked.revoke();
        when(secureToken.hash("raw-old")).thenReturn("hashed-old");
        when(refreshTokenRepository.findByTokenHash("hashed-old")).thenReturn(Optional.of(revoked));

        assertThatThrownBy(() -> useCase.execute(new RefreshTokenUseCase.Command("raw-old")))
                .isInstanceOf(InvalidOrExpiredTokenException.class);

        verify(refreshTokenRepository).revokeAllForUser(userId);
    }

    @Test
    void refuse_un_token_expire() {
        UUID userId = UUID.randomUUID();
        RefreshToken expired = RefreshToken.issue(userId, "hashed-old", Instant.now().minusSeconds(1));
        when(secureToken.hash("raw-old")).thenReturn("hashed-old");
        when(refreshTokenRepository.findByTokenHash("hashed-old")).thenReturn(Optional.of(expired));

        assertThatThrownBy(() -> useCase.execute(new RefreshTokenUseCase.Command("raw-old")))
                .isInstanceOf(InvalidOrExpiredTokenException.class);

        verify(jwtIssuer, never()).issueAccessToken(any());
    }

    @Test
    void refuse_si_compte_desactive() {
        User user = User.createNew(new Email("jane@doe.io"), "hashed", "Jane", "Doe");
        user.disable();
        RefreshToken stored = RefreshToken.issue(user.getId(), "hashed-old", Instant.now().plusSeconds(3600));
        when(secureToken.hash("raw-old")).thenReturn("hashed-old");
        when(refreshTokenRepository.findByTokenHash("hashed-old")).thenReturn(Optional.of(stored));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> useCase.execute(new RefreshTokenUseCase.Command("raw-old")))
                .isInstanceOf(AccountDisabledException.class);
    }
}