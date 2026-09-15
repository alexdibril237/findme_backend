package com.geolink.findme.unit.security;
import com.geolink.findme.security.CurrentUserProvider;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CurrentUserProviderTest {

    private final CurrentUserProvider provider = new CurrentUserProvider();

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    private void authenticateAs(String principal) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, List.of()));
    }

    @Test
    void requireCurrentUserId_retourne_l_id_du_principal_authentifie() {
        UUID userId = UUID.randomUUID();
        authenticateAs(userId.toString());

        assertThat(provider.requireCurrentUserId()).isEqualTo(userId);
    }

    @Test
    void requireCurrentUserId_leve_une_exception_sans_authentification() {
        SecurityContextHolder.clearContext();

        assertThatThrownBy(provider::requireCurrentUserId).isInstanceOf(RuntimeException.class);
    }

    @Test
    void currentUserIdOrNull_retourne_null_sans_authentification() {
        SecurityContextHolder.clearContext();

        assertThat(provider.currentUserIdOrNull()).isNull();
    }

    @Test
    void currentUserIdOrNull_retourne_null_pour_un_utilisateur_anonyme() {
        SecurityContextHolder.getContext().setAuthentication(
                new AnonymousAuthenticationToken("key", "anonymousUser", List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))));

        assertThat(provider.currentUserIdOrNull()).isNull();
    }

    @Test
    void currentUserIdOrNull_retourne_l_id_si_authentifie() {
        UUID userId = UUID.randomUUID();
        authenticateAs(userId.toString());

        assertThat(provider.currentUserIdOrNull()).isEqualTo(userId);
    }

    @Test
    void currentUserIdOrNull_retourne_null_si_le_principal_n_est_pas_un_uuid() {
        authenticateAs("pas-un-uuid");

        assertThat(provider.currentUserIdOrNull()).isNull();
    }
}
