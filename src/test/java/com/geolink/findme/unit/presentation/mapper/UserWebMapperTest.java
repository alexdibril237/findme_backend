package com.geolink.findme.unit.presentation.mapper;
import com.geolink.findme.presentation.mapper.UserWebMapper;

import com.geolink.findme.business.model.AccountStatus;
import com.geolink.findme.business.model.Role;
import com.geolink.findme.business.service.AuthResult;
import com.geolink.findme.data.entity.User;
import com.geolink.findme.presentation.dto.AuthResponse;
import com.geolink.findme.presentation.dto.UserProfileResponse;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UserWebMapperTest {

    private final UserWebMapper mapper = new UserWebMapper();

    private User sample() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("jane@doe.io");
        user.setFirstName("Jane");
        user.setLastName("Doe");
        user.setRole(Role.ADMIN);
        user.setStatus(AccountStatus.ACTIVE);
        user.setCreatedAt(Instant.now());
        user.setLastLoginAt(Instant.now());
        return user;
    }

    @Test
    void toProfileResponse_transporte_tous_les_champs() {
        User user = sample();

        UserProfileResponse response = mapper.toProfileResponse(user);

        assertThat(response.id()).isEqualTo(user.getId());
        assertThat(response.email()).isEqualTo("jane@doe.io");
        assertThat(response.prenom()).isEqualTo("Jane");
        assertThat(response.nom()).isEqualTo("Doe");
        assertThat(response.role()).isEqualTo("ADMIN");
        assertThat(response.statutCompte()).isEqualTo("ACTIVE");
        assertThat(response.dateDerniereConnexion()).isEqualTo(user.getLastLoginAt());
    }

    @Test
    void toAuthResponse_enveloppe_les_tokens_et_le_profil() {
        User user = sample();
        AuthResult result = new AuthResult(user, "access.jwt", "raw-refresh");

        AuthResponse response = mapper.toAuthResponse(result);

        assertThat(response.accessToken()).isEqualTo("access.jwt");
        assertThat(response.refreshToken()).isEqualTo("raw-refresh");
        assertThat(response.user().email()).isEqualTo("jane@doe.io");
    }
}
