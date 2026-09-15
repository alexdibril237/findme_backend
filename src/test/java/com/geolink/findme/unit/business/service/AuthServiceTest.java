package com.geolink.findme.unit.business.service;
import com.geolink.findme.business.service.AuthResult;
import com.geolink.findme.business.service.AuthServiceImpl;
import com.geolink.findme.business.service.AuthService;

import com.geolink.findme.business.exception.AccountDisabledException;
import com.geolink.findme.business.exception.EmailAlreadyUsedException;
import com.geolink.findme.business.exception.InvalidCredentialsException;
import com.geolink.findme.business.exception.InvalidOrExpiredTokenException;
import com.geolink.findme.business.exception.WeakPasswordException;
import com.geolink.findme.business.model.AccountStatus;
import com.geolink.findme.data.entity.RefreshToken;
import com.geolink.findme.data.entity.User;
import com.geolink.findme.data.repository.PasswordResetTokenRepository;
import com.geolink.findme.data.repository.RefreshTokenRepository;
import com.geolink.findme.data.repository.UserRepository;
import com.geolink.findme.security.EmailService;
import com.geolink.findme.security.JwtService;
import com.geolink.findme.security.SecureTokenGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private SecureTokenGenerator secureTokenGenerator;
    @Mock
    private EmailService emailService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthServiceImpl(userRepository, refreshTokenRepository, passwordResetTokenRepository,
                passwordEncoder, jwtService, secureTokenGenerator, emailService, 7L, 30L);
    }

    private User activeUser() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("jane@doe.io");
        user.setPasswordHash("hashed");
        user.setFirstName("Jane");
        user.setLastName("Doe");
        user.setStatus(AccountStatus.ACTIVE);
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());
        return user;
    }

    // --- signup ---

    @Test
    void signup_cree_un_compte_et_emet_les_tokens_quand_email_disponible() {
        when(userRepository.existsByEmail("jane@doe.io")).thenReturn(false);
        when(passwordEncoder.encode("Password1")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jwtService.issueAccessToken(any(User.class))).thenReturn("access.jwt");
        when(secureTokenGenerator.generateOpaqueToken()).thenReturn("raw-refresh");
        when(secureTokenGenerator.hash("raw-refresh")).thenReturn("hashed-refresh");

        AuthResult result = authService.signup("jane@doe.io", "Password1", "Jane", "Doe");

        assertThat(result.accessToken()).isEqualTo("access.jwt");
        assertThat(result.refreshToken()).isEqualTo("raw-refresh");
        assertThat(result.user().getEmail()).isEqualTo("jane@doe.io");
        assertThat(result.user().getFirstName()).isEqualTo("Jane");

        ArgumentCaptor<RefreshToken> tokenCaptor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(tokenCaptor.capture());
        assertThat(tokenCaptor.getValue().getTokenHash()).isEqualTo("hashed-refresh");
        assertThat(tokenCaptor.getValue().isRevoked()).isFalse();
    }

    @Test
    void signup_refuse_un_email_deja_utilise() {
        when(userRepository.existsByEmail("jane@doe.io")).thenReturn(true);

        assertThatThrownBy(() -> authService.signup("jane@doe.io", "Password1", "Jane", "Doe"))
                .isInstanceOf(EmailAlreadyUsedException.class);

        verify(userRepository, never()).save(any());
        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void signup_refuse_un_mot_de_passe_trop_faible() {
        when(userRepository.existsByEmail("jane@doe.io")).thenReturn(false);

        assertThatThrownBy(() -> authService.signup("jane@doe.io", "weak", "Jane", "Doe"))
                .isInstanceOf(WeakPasswordException.class);

        verify(userRepository, never()).save(any());
    }

    // --- signin ---

    @Test
    void signin_authentifie_et_emet_les_tokens_quand_identifiants_valides() {
        User user = activeUser();
        when(userRepository.findByEmail("jane@doe.io")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Password1", "hashed")).thenReturn(true);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jwtService.issueAccessToken(user)).thenReturn("access.jwt");
        when(secureTokenGenerator.generateOpaqueToken()).thenReturn("raw-refresh");
        when(secureTokenGenerator.hash("raw-refresh")).thenReturn("hashed-refresh");

        AuthResult result = authService.signin("jane@doe.io", "Password1");

        assertThat(result.accessToken()).isEqualTo("access.jwt");
        assertThat(result.refreshToken()).isEqualTo("raw-refresh");
        assertThat(user.getLastLoginAt()).isNotNull();
        verify(refreshTokenRepository).save(any());
    }

    @Test
    void signin_refuse_quand_email_inconnu() {
        when(userRepository.findByEmail("ghost@doe.io")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.signin("ghost@doe.io", "Password1"))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void signin_refuse_quand_mot_de_passe_incorrect() {
        User user = activeUser();
        when(userRepository.findByEmail("jane@doe.io")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        assertThatThrownBy(() -> authService.signin("jane@doe.io", "wrong"))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void signin_refuse_quand_compte_desactive() {
        User user = activeUser();
        user.setStatus(AccountStatus.DISABLED);
        when(userRepository.findByEmail("jane@doe.io")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Password1", "hashed")).thenReturn(true);

        assertThatThrownBy(() -> authService.signin("jane@doe.io", "Password1"))
                .isInstanceOf(AccountDisabledException.class);

        verify(refreshTokenRepository, never()).save(any());
    }

    // --- refresh ---

    @Test
    void refresh_tourne_le_refresh_token_et_emet_de_nouveaux_tokens() {
        User user = activeUser();
        RefreshToken stored = new RefreshToken();
        stored.setId(UUID.randomUUID());
        stored.setUserId(user.getId());
        stored.setTokenHash("hashed-old");
        stored.setExpiresAt(Instant.now().plusSeconds(3600));
        stored.setRevoked(false);
        stored.setCreatedAt(Instant.now());

        when(secureTokenGenerator.hash("raw-old")).thenReturn("hashed-old");
        when(refreshTokenRepository.findByTokenHash("hashed-old")).thenReturn(Optional.of(stored));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(jwtService.issueAccessToken(user)).thenReturn("new.access");
        when(secureTokenGenerator.generateOpaqueToken()).thenReturn("raw-new");
        when(secureTokenGenerator.hash("raw-new")).thenReturn("hashed-new");

        AuthResult result = authService.refresh("raw-old");

        assertThat(result.accessToken()).isEqualTo("new.access");
        assertThat(result.refreshToken()).isEqualTo("raw-new");
        assertThat(stored.isRevoked()).as("l'ancien token doit être révoqué (rotation)").isTrue();
        // l'ancien révoqué + le nouveau émis = deux save
        verify(refreshTokenRepository, times(2)).save(any(RefreshToken.class));
    }

    @Test
    void refresh_refuse_un_token_inconnu() {
        when(secureTokenGenerator.hash("raw-unknown")).thenReturn("hashed-unknown");
        when(refreshTokenRepository.findByTokenHash("hashed-unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.refresh("raw-unknown"))
                .isInstanceOf(InvalidOrExpiredTokenException.class);
    }

    @Test
    void refresh_revoque_toutes_les_sessions_si_token_deja_utilise_rejeu() {
        UUID userId = UUID.randomUUID();
        RefreshToken revoked = new RefreshToken();
        revoked.setId(UUID.randomUUID());
        revoked.setUserId(userId);
        revoked.setTokenHash("hashed-old");
        revoked.setExpiresAt(Instant.now().plusSeconds(3600));
        revoked.setRevoked(true);
        revoked.setCreatedAt(Instant.now());

        when(secureTokenGenerator.hash("raw-old")).thenReturn("hashed-old");
        when(refreshTokenRepository.findByTokenHash("hashed-old")).thenReturn(Optional.of(revoked));

        assertThatThrownBy(() -> authService.refresh("raw-old"))
                .isInstanceOf(InvalidOrExpiredTokenException.class);

        verify(refreshTokenRepository).revokeAllForUser(userId);
    }

    @Test
    void refresh_refuse_un_token_expire() {
        UUID userId = UUID.randomUUID();
        RefreshToken expired = new RefreshToken();
        expired.setId(UUID.randomUUID());
        expired.setUserId(userId);
        expired.setTokenHash("hashed-old");
        expired.setExpiresAt(Instant.now().minusSeconds(1));
        expired.setRevoked(false);
        expired.setCreatedAt(Instant.now());

        when(secureTokenGenerator.hash("raw-old")).thenReturn("hashed-old");
        when(refreshTokenRepository.findByTokenHash("hashed-old")).thenReturn(Optional.of(expired));

        assertThatThrownBy(() -> authService.refresh("raw-old"))
                .isInstanceOf(InvalidOrExpiredTokenException.class);

        verify(jwtService, never()).issueAccessToken(any());
    }

    @Test
    void refresh_refuse_si_compte_desactive() {
        User user = activeUser();
        user.setStatus(AccountStatus.DISABLED);
        RefreshToken stored = new RefreshToken();
        stored.setId(UUID.randomUUID());
        stored.setUserId(user.getId());
        stored.setTokenHash("hashed-old");
        stored.setExpiresAt(Instant.now().plusSeconds(3600));
        stored.setRevoked(false);
        stored.setCreatedAt(Instant.now());

        when(secureTokenGenerator.hash("raw-old")).thenReturn("hashed-old");
        when(refreshTokenRepository.findByTokenHash("hashed-old")).thenReturn(Optional.of(stored));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.refresh("raw-old"))
                .isInstanceOf(AccountDisabledException.class);
    }

    // --- resetPassword ---

    @Test
    void resetPassword_reinitialise_le_mot_de_passe_et_revoque_les_sessions() {
        User user = activeUser();
        var token = new com.geolink.findme.data.entity.PasswordResetToken();
        token.setId(UUID.randomUUID());
        token.setUserId(user.getId());
        token.setTokenHash("hashed-token");
        token.setExpiresAt(Instant.now().plusSeconds(600));
        token.setUsed(false);
        token.setCreatedAt(Instant.now());

        when(secureTokenGenerator.hash("raw-token")).thenReturn("hashed-token");
        when(passwordResetTokenRepository.findByTokenHash("hashed-token")).thenReturn(Optional.of(token));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("NewPass1")).thenReturn("new-hash");
        lenient().when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        authService.resetPassword("raw-token", "NewPass1");

        assertThat(user.getPasswordHash()).isEqualTo("new-hash");
        assertThat(token.isUsed()).isTrue();
        verify(userRepository).save(user);
        verify(passwordResetTokenRepository).save(token);
        verify(refreshTokenRepository).revokeAllForUser(user.getId());
    }

    @Test
    void resetPassword_refuse_un_token_inconnu() {
        when(secureTokenGenerator.hash("raw-token")).thenReturn("hashed-token");
        when(passwordResetTokenRepository.findByTokenHash("hashed-token")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.resetPassword("raw-token", "NewPass1"))
                .isInstanceOf(InvalidOrExpiredTokenException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void resetPassword_refuse_un_token_deja_utilise() {
        var used = new com.geolink.findme.data.entity.PasswordResetToken();
        used.setId(UUID.randomUUID());
        used.setUserId(UUID.randomUUID());
        used.setTokenHash("hashed-token");
        used.setExpiresAt(Instant.now().plusSeconds(600));
        used.setUsed(true);
        used.setCreatedAt(Instant.now());

        when(secureTokenGenerator.hash("raw-token")).thenReturn("hashed-token");
        when(passwordResetTokenRepository.findByTokenHash("hashed-token")).thenReturn(Optional.of(used));

        assertThatThrownBy(() -> authService.resetPassword("raw-token", "NewPass1"))
                .isInstanceOf(InvalidOrExpiredTokenException.class);

        verify(refreshTokenRepository, never()).revokeAllForUser(any());
    }

    @Test
    void resetPassword_refuse_un_token_expire() {
        var expired = new com.geolink.findme.data.entity.PasswordResetToken();
        expired.setId(UUID.randomUUID());
        expired.setUserId(UUID.randomUUID());
        expired.setTokenHash("hashed-token");
        expired.setExpiresAt(Instant.now().minusSeconds(1));
        expired.setUsed(false);
        expired.setCreatedAt(Instant.now());

        when(secureTokenGenerator.hash("raw-token")).thenReturn("hashed-token");
        when(passwordResetTokenRepository.findByTokenHash("hashed-token")).thenReturn(Optional.of(expired));

        assertThatThrownBy(() -> authService.resetPassword("raw-token", "NewPass1"))
                .isInstanceOf(InvalidOrExpiredTokenException.class);
    }

    // --- logout ---

    @Test
    void logout_revoque_le_refresh_token_connu() {
        RefreshToken stored = new RefreshToken();
        stored.setId(UUID.randomUUID());
        stored.setUserId(UUID.randomUUID());
        stored.setTokenHash("hashed-old");
        stored.setRevoked(false);

        when(secureTokenGenerator.hash("raw-old")).thenReturn("hashed-old");
        when(refreshTokenRepository.findByTokenHash("hashed-old")).thenReturn(Optional.of(stored));

        authService.logout("raw-old");

        assertThat(stored.isRevoked()).isTrue();
        verify(refreshTokenRepository).save(stored);
    }

    @Test
    void logout_est_un_no_op_silencieux_pour_un_token_inconnu() {
        when(secureTokenGenerator.hash("raw-unknown")).thenReturn("hashed-unknown");
        when(refreshTokenRepository.findByTokenHash("hashed-unknown")).thenReturn(Optional.empty());

        assertThatCode(() -> authService.logout("raw-unknown")).doesNotThrowAnyException();

        verify(refreshTokenRepository, never()).save(any());
    }

    // --- forgotPassword ---

    @Test
    void forgotPassword_emet_un_code_et_l_envoie_si_le_compte_existe() {
        User user = activeUser();
        when(userRepository.findByEmail("jane@doe.io")).thenReturn(Optional.of(user));
        when(secureTokenGenerator.generateNumericCode()).thenReturn("123456");
        when(secureTokenGenerator.hash("123456")).thenReturn("hashed-code");

        authService.forgotPassword("jane@doe.io");

        ArgumentCaptor<com.geolink.findme.data.entity.PasswordResetToken> captor =
                ArgumentCaptor.forClass(com.geolink.findme.data.entity.PasswordResetToken.class);
        verify(passwordResetTokenRepository).save(captor.capture());
        assertThat(captor.getValue().getTokenHash()).isEqualTo("hashed-code");
        assertThat(captor.getValue().isUsed()).isFalse();
        verify(emailService).sendPasswordResetCode("jane@doe.io", "123456");
    }

    @Test
    void forgotPassword_est_silencieux_si_le_compte_n_existe_pas() {
        when(userRepository.findByEmail("ghost@doe.io")).thenReturn(Optional.empty());

        assertThatCode(() -> authService.forgotPassword("ghost@doe.io")).doesNotThrowAnyException();

        verify(passwordResetTokenRepository, never()).save(any());
        verify(emailService, never()).sendPasswordResetCode(any(), any());
    }

    @Test
    void forgotPassword_normalise_l_email_avant_la_recherche() {
        User user = activeUser();
        when(userRepository.findByEmail("jane@doe.io")).thenReturn(Optional.of(user));
        when(secureTokenGenerator.generateNumericCode()).thenReturn("654321");
        when(secureTokenGenerator.hash("654321")).thenReturn("hashed-code");

        authService.forgotPassword("  JANE@Doe.IO ");

        verify(userRepository).findByEmail("jane@doe.io");
    }

    // --- normalisation de l'email ---

    @Test
    void signup_normalise_l_email_avant_de_verifier_son_existence() {
        when(userRepository.existsByEmail("jane@doe.io")).thenReturn(false);
        when(passwordEncoder.encode("Password1")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jwtService.issueAccessToken(any(User.class))).thenReturn("access.jwt");
        when(secureTokenGenerator.generateOpaqueToken()).thenReturn("raw-refresh");
        when(secureTokenGenerator.hash("raw-refresh")).thenReturn("hashed-refresh");

        AuthResult result = authService.signup("  JANE@Doe.IO ", "Password1", "Jane", "Doe");

        assertThat(result.user().getEmail()).isEqualTo("jane@doe.io");
        verify(userRepository).existsByEmail("jane@doe.io");
    }

    @Test
    void signin_normalise_l_email_avant_la_recherche() {
        User user = activeUser();
        when(userRepository.findByEmail("jane@doe.io")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Password1", "hashed")).thenReturn(true);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jwtService.issueAccessToken(user)).thenReturn("access.jwt");
        when(secureTokenGenerator.generateOpaqueToken()).thenReturn("raw-refresh");
        when(secureTokenGenerator.hash("raw-refresh")).thenReturn("hashed-refresh");

        authService.signin("  JANE@Doe.IO ", "Password1");

        verify(userRepository).findByEmail("jane@doe.io");
    }

    @Test
    void resetPassword_refuse_un_nouveau_mot_de_passe_trop_faible() {
        var token = new com.geolink.findme.data.entity.PasswordResetToken();
        token.setId(UUID.randomUUID());
        token.setUserId(UUID.randomUUID());
        token.setTokenHash("hashed-token");
        token.setExpiresAt(Instant.now().plusSeconds(600));
        token.setUsed(false);
        token.setCreatedAt(Instant.now());

        when(secureTokenGenerator.hash("raw-token")).thenReturn("hashed-token");
        when(passwordResetTokenRepository.findByTokenHash("hashed-token")).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> authService.resetPassword("raw-token", "weak"))
                .isInstanceOf(WeakPasswordException.class);

        verify(userRepository, never()).save(any());
    }
}
