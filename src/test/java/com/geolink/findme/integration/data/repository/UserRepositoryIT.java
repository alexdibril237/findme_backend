package com.geolink.findme.integration.data.repository;

import com.geolink.findme.business.model.AccountStatus;
import com.geolink.findme.business.model.Role;
import com.geolink.findme.data.entity.User;
import com.geolink.findme.data.repository.UserRepository;
import com.geolink.findme.integration.AbstractRepositoryIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Vérifie {@link UserRepository} face à une vraie base PostgreSQL (contraintes SQL, requête
 * {@code @Query} de recherche) — les données de démo (V7/V13) sont présentes.
 */
class UserRepositoryIT extends AbstractRepositoryIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    private User newUser(String email, String firstName, String lastName) {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail(email);
        user.setPasswordHash("hashed");
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setRole(Role.USER);
        user.setStatus(AccountStatus.ACTIVE);
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());
        return user;
    }

    @Test
    void existsByEmail_vrai_pour_un_compte_de_demonstration() {
        assertThat(userRepository.existsByEmail("aya@example.com")).isTrue();
    }

    @Test
    void existsByEmail_faux_pour_un_email_inconnu() {
        assertThat(userRepository.existsByEmail("inconnu@example.com")).isFalse();
    }

    @Test
    void findByEmail_retrouve_un_utilisateur_nouvellement_sauvegarde() {
        userRepository.save(newUser("nouvel.utilisateur@example.com", "Nouvel", "Utilisateur"));

        assertThat(userRepository.findByEmail("nouvel.utilisateur@example.com"))
                .isPresent()
                .get()
                .extracting(User::getFirstName)
                .isEqualTo("Nouvel");
    }

    @Test
    void la_contrainte_unique_sur_l_email_est_appliquee_par_la_base() {
        userRepository.saveAndFlush(newUser("duplicata@example.com", "Premier", "Compte"));

        assertThatThrownBy(() -> userRepository.saveAndFlush(newUser("duplicata@example.com", "Second", "Compte")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void search_sans_filtre_retourne_les_utilisateurs_de_demonstration() {
        var page = userRepository.search(null, PageRequest.of(0, 50));

        assertThat(page.getContent())
                .extracting(User::getEmail)
                .contains("admin@geolink.africa", "aya@example.com", "jean@example.com");
    }

    @Test
    void search_filtre_par_fragment_d_email() {
        var page = userRepository.search("aya@", PageRequest.of(0, 10));

        assertThat(page.getContent()).extracting(User::getEmail).containsExactly("aya@example.com");
    }

    @Test
    void search_filtre_par_prenom_insensible_a_la_casse() {
        var page = userRepository.search("JEAN", PageRequest.of(0, 10));

        assertThat(page.getContent()).extracting(User::getFirstName).containsExactly("Jean");
    }

    @Test
    void search_ne_retourne_rien_pour_un_fragment_absent() {
        var page = userRepository.search("zzz-introuvable-zzz", PageRequest.of(0, 10));

        assertThat(page.getContent()).isEmpty();
    }
}
