package com.geolink.findme.auth.application.usecase;

import com.geolink.findme.auth.domain.exception.InvalidOrExpiredTokenException;
import com.geolink.findme.auth.domain.exception.WeakPasswordException;
import com.geolink.findme.auth.domain.model.Email;
import com.geolink.findme.auth.domain.model.PasswordResetToken;
import com.geolink.findme.auth.domain.model.User;
import com.geolink.findme.auth.domain.port.PasswordHasherPort;
import com.geolink.findme.auth.domain.port.PasswordResetTokenRepositoryPort;
import com.geolink.findme.auth.domain.port.RefreshTokenRepositoryPort;
import com.geolink.findme.auth.domain.port.SecureTokenPort;
import com.geolink.findme.auth.domain.port.UserRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
class ResetPasswordUseCaseTest {

    @Mock
    private UserRepositoryPort userRepository;
    @Mock
    private PasswordResetTokenRepositoryPort resetTokenRepository;
    @Mock
    private RefreshTokenRepositoryPort refreshTokenRepository;
    @Mock
    private PasswordHasherPort passwordHasher;
    @Mock
    private SecureTokenPort secureToken;

    private ResetPasswordUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ResetPasswordUseCase(userRepository, resetTokenRepository, refreshTokenRepository,
                passwordHasher, secureToken);
    }

    @Test
    void reinitialise_le_mot_de_passe_et_revoque_les_sessions() {
        User user = User.createNew(new Email("jane@doe.io"), "old-hash", "Jane", "Doe");
        PasswordResetToken token = PasswordResetToken.issue(user.getId(), "hashed-token",
                Instant.now().plusSeconds(600));
        when(secureToken.hash("raw-token")).thenReturn("hashed-token");
        when(resetTokenRepository.findByTokenHash("hashed-token")).thenReturn(Optional.of(token));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(passwordHasher.hash("NewPass1")).thenReturn("new-hash");

        useCase.execute(new ResetPasswordUseCase.Command("raw-token", "NewPass1"));

        assertThat(user.getPasswordHash()).isEqualTo("new-hash");
        assertThat(token.isUsed()).isTrue();
        verify(userRepository).save(user);
        verify(resetTokenRepository).save(token);
        verify(refreshTokenRepository).revokeAllForUser(user.getId());
    }

    @Test
    void refuse_un_token_inconnu() {
        when(secureToken.hash("raw-token")).thenReturn("hashed-token");
        when(resetTokenRepository.findByTokenHash("hashed-token")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(new ResetPasswordUseCase.Command("raw-token", "NewPass1")))
                .isInstanceOf(InvalidOrExpiredTokenException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void refuse_un_token_deja_utilise() {
        PasswordResetToken used = PasswordResetToken.issue(UUID.randomUUID(), "hashed-token",
                Instant.now().plusSeconds(600));
        used.markUsed();
        when(secureToken.hash("raw-token")).thenReturn("hashed-token");
        when(resetTokenRepository.findByTokenHash("hashed-token")).thenReturn(Optional.of(used));

        assertThatThrownBy(() -> useCase.execute(new ResetPasswordUseCase.Command("raw-token", "NewPass1")))
                .isInstanceOf(InvalidOrExpiredTokenException.class);

        verify(refreshTokenRepository, never()).revokeAllForUser(any());
    }

    @Test
    void refuse_un_token_expire() {
        PasswordResetToken expired = PasswordResetToken.issue(UUID.randomUUID(), "hashed-token",
                Instant.now().minusSeconds(1));
        when(secureToken.hash("raw-token")).thenReturn("hashed-token");
        when(resetTokenRepository.findByTokenHash("hashed-token")).thenReturn(Optional.of(expired));

        assertThatThrownBy(() -> useCase.execute(new ResetPasswordUseCase.Command("raw-token", "NewPass1")))
                .isInstanceOf(InvalidOrExpiredTokenException.class);
    }

    @Test
    void refuse_un_nouveau_mot_de_passe_trop_faible() {
        PasswordResetToken token = PasswordResetToken.issue(UUID.randomUUID(), "hashed-token",
                Instant.now().plusSeconds(600));
        when(secureToken.hash("raw-token")).thenReturn("hashed-token");
        when(resetTokenRepository.findByTokenHash("hashed-token")).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> useCase.execute(new ResetPasswordUseCase.Command("raw-token", "weak")))
                .isInstanceOf(WeakPasswordException.class);

        verify(userRepository, never()).save(any());
    }
}