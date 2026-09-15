package com.geolink.findme.integration.data.repository;

import com.geolink.findme.business.model.AccountStatus;
import com.geolink.findme.business.model.Role;
import com.geolink.findme.business.model.TicketStatus;
import com.geolink.findme.data.entity.SupportTicket;
import com.geolink.findme.data.entity.User;
import com.geolink.findme.data.repository.SupportTicketRepository;
import com.geolink.findme.data.repository.UserRepository;
import com.geolink.findme.integration.AbstractRepositoryIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Vérifie {@link SupportTicketRepository} : filtrage par statut, et le comportement
 * {@code ON DELETE SET NULL} de la clé étrangère vers l'auteur du ticket (V12) — un ticket ne
 * doit jamais être supprimé quand son auteur l'est, seul le lien est rompu.
 */
class SupportTicketRepositoryIT extends AbstractRepositoryIntegrationTest {

    @Autowired
    private SupportTicketRepository supportTicketRepository;
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

    private SupportTicket newTicket(String name, String email, TicketStatus status, UUID userId) {
        Instant now = Instant.now();
        SupportTicket ticket = new SupportTicket();
        ticket.setId(UUID.randomUUID());
        ticket.setName(name);
        ticket.setEmail(email);
        ticket.setMessage("Message de test");
        ticket.setUserId(userId);
        ticket.setStatus(status);
        ticket.setCreatedAt(now);
        ticket.setUpdatedAt(now);
        return ticket;
    }

    @Test
    void findByStatus_ne_retourne_que_les_tickets_du_statut_demande() {
        supportTicketRepository.saveAndFlush(newTicket("Awa", "awa@example.com", TicketStatus.NON_TRAITE, null));
        supportTicketRepository.saveAndFlush(newTicket("Moussa", "moussa@example.com", TicketStatus.TRAITE, null));

        var page = supportTicketRepository.findByStatus(TicketStatus.TRAITE, PageRequest.of(0, 50));

        assertThat(page.getContent()).extracting(SupportTicket::getName).contains("Moussa").doesNotContain("Awa");
    }

    @Test
    void le_ticket_survit_a_la_suppression_de_son_auteur_mais_perd_le_lien() {
        User user = persistedUser("auteur-ticket@example.com");
        SupportTicket ticket = supportTicketRepository.saveAndFlush(
                newTicket("Auteur", "auteur-ticket@example.com", TicketStatus.NON_TRAITE, user.getId()));

        userRepository.delete(user);
        userRepository.flush();
        // Le ON DELETE SET NULL agit au niveau SQL, pas via Hibernate : sans ce clear(), le ticket
        // déjà chargé garderait son ancien userId en mémoire malgré la mise à jour réelle en base.
        entityManager.clear();

        assertThat(supportTicketRepository.findById(ticket.getId()))
                .isPresent()
                .get()
                .extracting(SupportTicket::getUserId)
                .isNull();
    }

    @Test
    void un_ticket_anonyme_n_a_pas_de_userId() {
        SupportTicket ticket = supportTicketRepository.saveAndFlush(
                newTicket("Anonyme", "anonyme@example.com", TicketStatus.NON_TRAITE, null));

        assertThat(supportTicketRepository.findById(ticket.getId())).get()
                .extracting(SupportTicket::getUserId)
                .isNull();
    }
}
