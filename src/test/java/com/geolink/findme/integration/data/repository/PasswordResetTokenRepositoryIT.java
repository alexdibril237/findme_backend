package com.geolink.findme.integration.data.repository;

import com.geolink.findme.business.model.AccountStatus;
import com.geolink.findme.business.model.Role;
import com.geolink.findme.data.entity.PasswordResetToken;
import com.geolink.findme.data.entity.User;
import com.geolink.findme.data.repository.PasswordResetTokenRepository;
import com.geolink.findme.data.repository.UserRepository;
import com.geolink.findme.integration.AbstractRepositoryIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Vérifie {@link PasswordResetTokenRepository} : contrainte d'unicité du hash et suppression en
 * cascade des tokens quand l'utilisateur est supprimé.
 */
class PasswordResetTokenRepositoryIT extends AbstractRepositoryIntegrationTest {

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TestEntityManager entityManager;

    private User persistedUser(String email) {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail(email);
        user.setPasswordHash("hashed");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setRole(Role.USER);
        user.setStatus(AccountStatus.ACTIVE);
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());
        return userRepository.saveAndFlush(user);
    }

    private PasswordResetToken newToken(UUID userId, String tokenHash) {
        PasswordResetToken token = new PasswordResetToken();
        token.setId(UUID.randomUUID());
        token.setUserId(userId);
        token.setTokenHash(tokenHash);
        token.setExpiresAt(Instant.now().plusSeconds(1800));
        token.setUsed(false);
        token.setCreatedAt(Instant.now());
        return token;
    }

    @Test
    void findByTokenHash_retrouve_le_token_sauvegarde() {
        User user = persistedUser("reset1@example.com");
        passwordResetTokenRepository.saveAndFlush(newToken(user.getId(), "reset-hash-abc"));

        assertThat(passwordResetTokenRepository.findByTokenHash("reset-hash-abc"))
                .isPresent()
                .get()
                .extracting(PasswordResetToken::getUserId)
                .isEqualTo(user.getId());
    }

    @Test
    void findByTokenHash_est_vide_pour_un_hash_inconnu() {
        assertThat(passwordResetTokenRepository.findByTokenHash("reset-hash-inconnu")).isEmpty();
    }

    @Test
    void la_contrainte_unique_sur_le_hash_du_token_est_appliquee() {
        User user = persistedUser("reset2@example.com");
        passwordResetTokenRepository.saveAndFlush(newToken(user.getId(), "reset-hash-duplique"));

        assertThatThrownBy(() -> passwordResetTokenRepository.saveAndFlush(newToken(user.getId(), "reset-hash-duplique")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void les_tokens_sont_supprimes_en_cascade_si_l_utilisateur_est_supprime() {
        User user = persistedUser("reset3@example.com");
        PasswordResetToken token = passwordResetTokenRepository.saveAndFlush(newToken(user.getId(), "reset-hash-cascade"));

        userRepository.delete(user);
        userRepository.flush();
        // Le ON DELETE CASCADE agit au niveau SQL, pas via Hibernate : sans ce clear(), le token
        // déjà chargé resterait visible dans le contexte de persistance malgré sa suppression réelle.
        entityManager.clear();

        assertThat(passwordResetTokenRepository.findById(token.getId())).isEmpty();
    }
}
