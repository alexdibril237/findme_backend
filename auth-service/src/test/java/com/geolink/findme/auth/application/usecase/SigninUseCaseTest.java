package com.geolink.findme.auth.application.usecase;

import com.geolink.findme.auth.domain.exception.AccountDisabledException;
import com.geolink.findme.auth.domain.exception.InvalidCredentialsException;
import com.geolink.findme.auth.domain.model.Email;
import com.geolink.findme.auth.domain.model.User;
import com.geolink.findme.auth.domain.port.JwtIssuerPort;
import com.geolink.findme.auth.domain.port.PasswordHasherPort;
import com.geolink.findme.auth.domain.port.RefreshTokenRepositoryPort;
import com.geolink.findme.auth.domain.port.SecureTokenPort;
import com.geolink.findme.auth.domain.port.UserRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SigninUseCaseTest {

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

    private SigninUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new SigninUseCase(userRepository, refreshTokenRepository, passwordHasher, jwtIssuer,
                secureToken, Duration.ofDays(7));
    }

    private User activeUser() {
        return User.createNew(new Email("jane@doe.io"), "hashed", "Jane", "Doe");
    }

    @Test
    void authentifie_et_emet_les_tokens_quand_identifiants_valides() {
        User user = activeUser();
        when(userRepository.findByEmail(new Email("jane@doe.io"))).thenReturn(Optional.of(user));
        when(passwordHasher.matches("Password1", "hashed")).thenReturn(true);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jwtIssuer.issueAccessToken(user)).thenReturn("access.jwt");
        when(secureToken.generateOpaqueToken()).thenReturn("raw-refresh");
        when(secureToken.hash("raw-refresh")).thenReturn("hashed-refresh");

        AuthResult result = useCase.execute(new SigninUseCase.Command("jane@doe.io", "Password1"));

        assertThat(result.accessToken()).isEqualTo("access.jwt");
        assertThat(result.refreshToken()).isEqualTo("raw-refresh");
        assertThat(user.getLastLoginAt()).isNotNull();
        verify(refreshTokenRepository).save(any());
    }

    @Test
    void refuse_quand_email_inconnu() {
        when(userRepository.findByEmail(new Email("ghost@doe.io"))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(new SigninUseCase.Command("ghost@doe.io", "Password1")))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void refuse_quand_mot_de_passe_incorrect() {
        User user = activeUser();
        when(userRepository.findByEmail(new Email("jane@doe.io"))).thenReturn(Optional.of(user));
        when(passwordHasher.matches("wrong", "hashed")).thenReturn(false);

        assertThatThrownBy(() -> useCase.execute(new SigninUseCase.Command("jane@doe.io", "wrong")))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void refuse_quand_compte_desactive() {
        User user = activeUser();
        user.disable();
        when(userRepository.findByEmail(new Email("jane@doe.io"))).thenReturn(Optional.of(user));
        when(passwordHasher.matches("Password1", "hashed")).thenReturn(true);
        lenient().when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        assertThatThrownBy(() -> useCase.execute(new SigninUseCase.Command("jane@doe.io", "Password1")))
                .isInstanceOf(AccountDisabledException.class);

        verify(refreshTokenRepository, never()).save(any());
    }
}