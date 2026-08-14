package com.geolink.findme.auth.application.usecase;

import com.geolink.findme.auth.domain.exception.EmailAlreadyUsedException;
import com.geolink.findme.auth.domain.exception.WeakPasswordException;
import com.geolink.findme.auth.domain.model.Email;
import com.geolink.findme.auth.domain.model.RefreshToken;
import com.geolink.findme.auth.domain.model.User;
import com.geolink.findme.auth.domain.port.JwtIssuerPort;
import com.geolink.findme.auth.domain.port.PasswordHasherPort;
import com.geolink.findme.auth.domain.port.RefreshTokenRepositoryPort;
import com.geolink.findme.auth.domain.port.SecureTokenPort;
import com.geolink.findme.auth.domain.port.UserRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SignupUseCaseTest {

    @Mock
    private UserRepositoryPort userRepository;
    @Mock
    private RefreshTokenRepositoryPort refreshTokenRepository;
    @Mock
    private PasswordHasherPort passwordHasher;
    @Mock
    private JwtIssuerPort jwtIssuer;
    @Mock
    private SecureTokenPort secureToken;

    private SignupUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new SignupUseCase(userRepository, refreshTokenRepository, passwordHasher, jwtIssuer,
                secureToken, Duration.ofDays(7));
    }

    @Test
    void cree_un_compte_et_emet_les_tokens_quand_email_disponible() {
        when(userRepository.existsByEmail(new Email("jane@doe.io"))).thenReturn(false);
        when(passwordHasher.hash("Password1")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jwtIssuer.issueAccessToken(any(User.class))).thenReturn("access.jwt");
        when(secureToken.generateOpaqueToken()).thenReturn("raw-refresh");
        when(secureToken.hash("raw-refresh")).thenReturn("hashed-refresh");

        AuthResult result = useCase.execute(new SignupUseCase.Command("jane@doe.io", "Password1", "Jane", "Doe"));

        assertThat(result.accessToken()).isEqualTo("access.jwt");
        assertThat(result.refreshToken()).isEqualTo("raw-refresh");
        assertThat(result.user().getEmail().value()).isEqualTo("jane@doe.io");
        assertThat(result.user().getFirstName()).isEqualTo("Jane");

        ArgumentCaptor<RefreshToken> tokenCaptor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(tokenCaptor.capture());
        assertThat(tokenCaptor.getValue().getTokenHash()).isEqualTo("hashed-refresh");
        assertThat(tokenCaptor.getValue().isRevoked()).isFalse();
    }

    @Test
    void refuse_un_email_deja_utilise() {
        when(userRepository.existsByEmail(new Email("jane@doe.io"))).thenReturn(true);

        assertThatThrownBy(() -> useCase.execute(
                new SignupUseCase.Command("jane@doe.io", "Password1", "Jane", "Doe")))
                .isInstanceOf(EmailAlreadyUsedException.class);

        verify(userRepository, never()).save(any());
        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void refuse_un_mot_de_passe_trop_faible() {
        when(userRepository.existsByEmail(new Email("jane@doe.io"))).thenReturn(false);

        assertThatThrownBy(() -> useCase.execute(
                new SignupUseCase.Command("jane@doe.io", "weak", "Jane", "Doe")))
                .isInstanceOf(WeakPasswordException.class);

        verify(userRepository, never()).save(any());
    }
}