package com.geolink.findme.integration.data.repository;

import com.geolink.findme.business.model.AccountStatus;
import com.geolink.findme.business.model.Role;
import com.geolink.findme.data.entity.User;
import com.geolink.findme.data.entity.UserMessage;
import com.geolink.findme.data.repository.UserMessageRepository;
import com.geolink.findme.data.repository.UserRepository;
import com.geolink.findme.integration.AbstractRepositoryIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Vérifie {@link UserMessageRepository} : tri par date décroissante, et suppression en cascade
 * des messages quand leur destinataire est supprimé (V11, {@code ON DELETE CASCADE}).
 */
class UserMessageRepositoryIT extends AbstractRepositoryIntegrationTest {

    @Autowired
    private UserMessageRepository userMessageRepository;
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

    private UserMessage newMessage(UUID recipientId, String subject, Instant createdAt) {
        UserMessage message = new UserMessage();
        message.setId(UUID.randomUUID());
        message.setRecipientId(recipientId);
        message.setSubject(subject);
        message.setBody("Corps du message");
        message.setRead(false);
        message.setCreatedAt(createdAt);
        return message;
    }

    @Test
    void findByRecipientIdOrderByCreatedAtDesc_trie_du_plus_recent_au_plus_ancien() {
        User user = persistedUser("destinataire1@example.com");
        Instant now = Instant.now();
        userMessageRepository.saveAndFlush(newMessage(user.getId(), "Ancien message", now.minus(2, ChronoUnit.DAYS)));
        userMessageRepository.saveAndFlush(newMessage(user.getId(), "Message recent", now));

        var page = userMessageRepository.findByRecipientIdOrderByCreatedAtDesc(user.getId(), PageRequest.of(0, 10));

        assertThat(page.getContent()).extracting(UserMessage::getSubject)
                .containsExactly("Message recent", "Ancien message");
    }

    @Test
    void findByRecipientIdOrderByCreatedAtDesc_ne_retourne_que_les_messages_du_destinataire() {
        User destinataire = persistedUser("destinataire2@example.com");
        User autre = persistedUser("autre-destinataire@example.com");
        userMessageRepository.saveAndFlush(newMessage(destinataire.getId(), "Pour moi", Instant.now()));
        userMessageRepository.saveAndFlush(newMessage(autre.getId(), "Pas pour moi", Instant.now()));

        var page = userMessageRepository.findByRecipientIdOrderByCreatedAtDesc(destinataire.getId(), PageRequest.of(0, 10));

        assertThat(page.getContent()).extracting(UserMessage::getSubject).containsExactly("Pour moi");
    }

    @Test
    void les_messages_sont_supprimes_en_cascade_si_le_destinataire_est_supprime() {
        User user = persistedUser("destinataire3@example.com");
        UserMessage message = userMessageRepository.saveAndFlush(newMessage(user.getId(), "Sujet", Instant.now()));

        userRepository.delete(user);
        userRepository.flush();
        // Le ON DELETE CASCADE agit au niveau SQL, pas via Hibernate : sans ce clear(), le message
        // déjà chargé resterait visible dans le contexte de persistance malgré sa suppression réelle.
        entityManager.clear();

        assertThat(userMessageRepository.findById(message.getId())).isEmpty();
    }
}
