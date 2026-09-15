package com.geolink.findme.unit.business.service;
import com.geolink.findme.business.service.UserServiceImpl;
import com.geolink.findme.business.service.UserService;

import com.geolink.findme.business.exception.UserNotFoundException;
import com.geolink.findme.business.model.AccountStatus;
import com.geolink.findme.business.model.Role;
import com.geolink.findme.data.entity.User;
import com.geolink.findme.data.repository.RefreshTokenRepository;
import com.geolink.findme.data.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userRepository, refreshTokenRepository);
    }

    private User existing() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("jane@doe.io");
        user.setFirstName("Jane");
        user.setLastName("Doe");
        user.setRole(Role.USER);
        user.setStatus(AccountStatus.ACTIVE);
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());
        return user;
    }

    // --- getMyProfile ---

    @Test
    void getMyProfile_retourne_le_profil() {
        User user = existing();
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        assertThat(userService.getMyProfile(user.getId())).isSameAs(user);
    }

    @Test
    void getMyProfile_leve_not_found() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getMyProfile(id)).isInstanceOf(UserNotFoundException.class);
    }

    // --- updateMyProfile ---

    @Test
    void updateMyProfile_met_a_jour_prenom_et_nom() {
        User user = existing();
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = userService.updateMyProfile(user.getId(), "Janet", "Smith");

        assertThat(result.getFirstName()).isEqualTo("Janet");
        assertThat(result.getLastName()).isEqualTo("Smith");
    }

    @Test
    void updateMyProfile_leve_not_found() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateMyProfile(id, "Janet", "Smith"))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository, never()).save(any());
    }

    // --- listUsers ---

    @Test
    void listUsers_normalise_une_recherche_vide_en_null() {
        Pageable pageable = Pageable.ofSize(10);
        Page<User> page = new PageImpl<>(List.of(existing()));
        when(userRepository.search(isNull(), eq(pageable))).thenReturn(page);

        Page<User> result = userService.listUsers("   ", pageable);

        assertThat(result).isSameAs(page);
    }

    @Test
    void listUsers_transmet_une_recherche_non_vide_apres_trim() {
        Pageable pageable = Pageable.ofSize(10);
        Page<User> page = new PageImpl<>(List.of(existing()));
        when(userRepository.search("jane", pageable)).thenReturn(page);

        Page<User> result = userService.listUsers("  jane  ", pageable);

        assertThat(result).isSameAs(page);
    }

    @Test
    void listUsers_traite_une_recherche_null_comme_absente() {
        Pageable pageable = Pageable.ofSize(10);
        Page<User> page = new PageImpl<>(List.of(existing()));
        when(userRepository.search(isNull(), eq(pageable))).thenReturn(page);

        Page<User> result = userService.listUsers(null, pageable);

        assertThat(result).isSameAs(page);
    }

    // --- updateUserRole ---

    @Test
    void updateUserRole_met_a_jour_le_role() {
        User user = existing();
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        userService.updateUserRole(user.getId(), Role.ADMIN);

        assertThat(user.getRole()).isEqualTo(Role.ADMIN);
        verify(userRepository).save(user);
    }

    @Test
    void updateUserRole_leve_not_found() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUserRole(id, Role.ADMIN))
                .isInstanceOf(UserNotFoundException.class);
    }

    // --- updateUserStatus ---

    @Test
    void updateUserStatus_vers_disabled_revoque_les_sessions() {
        User user = existing();
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        userService.updateUserStatus(user.getId(), AccountStatus.DISABLED);

        assertThat(user.getStatus()).isEqualTo(AccountStatus.DISABLED);
        verify(refreshTokenRepository).revokeAllForUser(user.getId());
    }

    @Test
    void updateUserStatus_vers_active_ne_revoque_pas_les_sessions() {
        User user = existing();
        user.setStatus(AccountStatus.DISABLED);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        userService.updateUserStatus(user.getId(), AccountStatus.ACTIVE);

        assertThat(user.getStatus()).isEqualTo(AccountStatus.ACTIVE);
        verify(refreshTokenRepository, never()).revokeAllForUser(any());
    }

    @Test
    void updateUserStatus_leve_not_found() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUserStatus(id, AccountStatus.DISABLED))
                .isInstanceOf(UserNotFoundException.class);

        verify(refreshTokenRepository, never()).revokeAllForUser(any());
    }
}
