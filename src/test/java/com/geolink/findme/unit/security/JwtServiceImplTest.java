package com.geolink.findme.unit.security;
import com.geolink.findme.security.TokenClaims;
import com.geolink.findme.security.JwtServiceImpl;

import com.geolink.findme.business.exception.InvalidOrExpiredTokenException;
import com.geolink.findme.business.model.AccountStatus;
import com.geolink.findme.business.model.Role;
import com.geolink.findme.data.entity.User;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceImplTest {

    private static final String SECRET = "test-secret-test-secret-test-secret-test-secret";

    private final JwtServiceImpl jwtService = new JwtServiceImpl(SECRET, 15);

    private User sampleUser() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("jane@doe.io");
        user.setRole(Role.ADMIN);
        user.setStatus(AccountStatus.ACTIVE);
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());
        return user;
    }

    @Test
    void issueAccessToken_puis_parseAndValidate_retrouve_l_utilisateur() {
        User user = sampleUser();

        String token = jwtService.issueAccessToken(user);
        TokenClaims claims = jwtService.parseAndValidate(token);

        assertThat(claims.userId()).isEqualTo(user.getId());
        assertThat(claims.email()).isEqualTo("jane@doe.io");
        assertThat(claims.role()).isEqualTo(Role.ADMIN);
    }

    @Test
    void issueAccessToken_inclut_le_role_du_support_agent() {
        User user = sampleUser();
        user.setRole(Role.SUPPORT_AGENT);

        String token = jwtService.issueAccessToken(user);

        assertThat(jwtService.parseAndValidate(token).role()).isEqualTo(Role.SUPPORT_AGENT);
    }

    @Test
    void parseAndValidate_leve_une_exception_si_le_token_est_expire() {
        JwtServiceImpl expiredIssuer = new JwtServiceImpl(SECRET, -1);
        String expiredToken = expiredIssuer.issueAccessToken(sampleUser());

        assertThatThrownBy(() -> jwtService.parseAndValidate(expiredToken))
                .isInstanceOf(InvalidOrExpiredTokenException.class);
    }

    @Test
    void parseAndValidate_leve_une_exception_si_la_signature_est_invalide() {
        JwtServiceImpl otherSigner = new JwtServiceImpl("autre-secret-autre-secret-autre-secret-32", 15);
        String token = otherSigner.issueAccessToken(sampleUser());

        assertThatThrownBy(() -> jwtService.parseAndValidate(token))
                .isInstanceOf(InvalidOrExpiredTokenException.class);
    }

    @Test
    void parseAndValidate_leve_une_exception_pour_un_token_malformed() {
        assertThatThrownBy(() -> jwtService.parseAndValidate("ceci-n-est-pas-un-jwt"))
                .isInstanceOf(InvalidOrExpiredTokenException.class);
    }

    @Test
    void parseAndValidate_leve_une_exception_pour_une_chaine_vide() {
        assertThatThrownBy(() -> jwtService.parseAndValidate(""))
                .isInstanceOf(InvalidOrExpiredTokenException.class);
    }
}
