package com.geolink.findme.integration.data.repository;

import com.geolink.findme.business.model.AccountStatus;
import com.geolink.findme.business.model.Role;
import com.geolink.findme.data.entity.RefreshToken;
import com.geolink.findme.data.entity.User;
import com.geolink.findme.data.repository.RefreshTokenRepository;
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
 * Vérifie {@link RefreshTokenRepository} : requête de révocation en masse ({@code @Modifying}),
 * contrainte d'unicité du hash, et suppression en cascade quand l'utilisateur est supprimé.
 */
class RefreshTokenRepositoryIT extends AbstractRepositoryIntegrationTest {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;
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

    private RefreshToken newToken(UUID userId, String tokenHash) {
        RefreshToken token = new RefreshToken();
        token.setId(UUID.randomUUID());
        token.setUserId(userId);
        token.setTokenHash(tokenHash);
        token.setExpiresAt(Instant.now().plusSeconds(3600));
        token.setRevoked(false);
        token.setCreatedAt(Instant.now());
        return token;
    }

    @Test
    void findByTokenHash_retrouve_le_token_sauvegarde() {
        User user = persistedUser("refresh1@example.com");
        refreshTokenRepository.saveAndFlush(newToken(user.getId(), "hash-abc"));

        assertThat(refreshTokenRepository.findByTokenHash("hash-abc"))
                .isPresent()
                .get()
                .extracting(RefreshToken::getUserId)
                .isEqualTo(user.getId());
    }

    @Test
    void findByTokenHash_est_vide_pour_un_hash_inconnu() {
        assertThat(refreshTokenRepository.findByTokenHash("hash-inconnu")).isEmpty();
    }

    @Test
    void revokeAllForUser_ne_revoque_que_les_tokens_actifs_du_bon_utilisateur() {
        User userA = persistedUser("refresh-a@example.com");
        User userB = persistedUser("refresh-b@example.com");
        RefreshToken tokenA1 = refreshTokenRepository.saveAndFlush(newToken(userA.getId(), "hash-a1"));
        RefreshToken tokenA2 = refreshTokenRepository.saveAndFlush(newToken(userA.getId(), "hash-a2"));
        RefreshToken tokenB = refreshTokenRepository.saveAndFlush(newToken(userB.getId(), "hash-b1"));

        refreshTokenRepository.revokeAllForUser(userA.getId());
        refreshTokenRepository.flush();
        // La requête @Modifying est une UPDATE JPQL en masse : elle ne rafraîchit pas les entités
        // déjà chargées dans le contexte de persistance (tokenA1/tokenA2 resteraient "revoked=false"
        // en mémoire). On vide le cache de 1er niveau pour forcer une relecture depuis la base.
        entityManager.clear();

        assertThat(refreshTokenRepository.findById(tokenA1.getId())).get().extracting(RefreshToken::isRevoked).isEqualTo(true);
        assertThat(refreshTokenRepository.findById(tokenA2.getId())).get().extracting(RefreshToken::isRevoked).isEqualTo(true);
        assertThat(refreshTokenRepository.findById(tokenB.getId())).get().extracting(RefreshToken::isRevoked).isEqualTo(false);
    }

    @Test
    void la_contrainte_unique_sur_le_hash_du_token_est_appliquee() {
        User user = persistedUser("refresh2@example.com");
        refreshTokenRepository.saveAndFlush(newToken(user.getId(), "hash-duplique"));

        assertThatThrownBy(() -> refreshTokenRepository.saveAndFlush(newToken(user.getId(), "hash-duplique")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void les_tokens_sont_supprimes_en_cascade_si_l_utilisateur_est_supprime() {
        User user = persistedUser("refresh3@example.com");
        RefreshToken token = refreshTokenRepository.saveAndFlush(newToken(user.getId(), "hash-cascade"));

        userRepository.delete(user);
        userRepository.flush();
        // Le ON DELETE CASCADE agit au niveau SQL, pas via Hibernate : sans ce clear(), le token
        // déjà chargé resterait visible dans le contexte de persistance malgré sa suppression réelle.
        entityManager.clear();

        assertThat(refreshTokenRepository.findById(token.getId())).isEmpty();
    }
}
