package com.geolink.findme.business.service;

import com.geolink.findme.business.exception.SupportTicketNotFoundException;
import com.geolink.findme.business.model.TicketStatus;
import com.geolink.findme.data.entity.SupportTicket;
import com.geolink.findme.data.repository.SupportTicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SupportServiceTest {

    @Mock
    private SupportTicketRepository supportTicketRepository;
    @Mock
    private UserMessageService userMessageService;

    private SupportService supportService;

    @BeforeEach
    void setUp() {
        supportService = new SupportServiceImpl(supportTicketRepository, userMessageService);
    }

    private SupportTicket existing(TicketStatus status, UUID userId) {
        SupportTicket ticket = new SupportTicket();
        ticket.setId(UUID.randomUUID());
        ticket.setName("Awa Ndiaye");
        ticket.setEmail("awa@example.com");
        ticket.setMessage("Mon adresse ne se vérifie pas.");
        ticket.setUserId(userId);
        ticket.setStatus(status);
        ticket.setCreatedAt(Instant.now());
        ticket.setUpdatedAt(Instant.now());
        return ticket;
    }

    // --- createTicket ---

    @Test
    void cree_un_ticket_non_traite_relie_a_l_utilisateur_connecte() {
        UUID userId = UUID.randomUUID();
        when(supportTicketRepository.save(any(SupportTicket.class))).thenAnswer(inv -> inv.getArgument(0));

        SupportTicket created = supportService.createTicket("Awa Ndiaye", "awa@example.com", "Question.", userId);

        assertThat(created.getStatus()).isEqualTo(TicketStatus.NON_TRAITE);
        assertThat(created.getUserId()).isEqualTo(userId);
    }

    @Test
    void cree_un_ticket_anonyme_sans_userId() {
        when(supportTicketRepository.save(any(SupportTicket.class))).thenAnswer(inv -> inv.getArgument(0));

        SupportTicket created = supportService.createTicket("Anonyme", "anon@example.com", "Question.", null);

        assertThat(created.getUserId()).isNull();
    }

    // --- updateTicketStatus ---

    @Test
    void resoudre_un_ticket_notifie_son_auteur_connu() {
        UUID userId = UUID.randomUUID();
        SupportTicket ticket = existing(TicketStatus.NON_TRAITE, userId);
        when(supportTicketRepository.findById(ticket.getId())).thenReturn(Optional.of(ticket));
        when(supportTicketRepository.save(any(SupportTicket.class))).thenAnswer(inv -> inv.getArgument(0));

        SupportTicket result = supportService.updateTicketStatus(ticket.getId(), TicketStatus.TRAITE);

        assertThat(result.getStatus()).isEqualTo(TicketStatus.TRAITE);
        verify(userMessageService).send(eq(userId), anyString(), anyString());
    }

    @Test
    void resoudre_un_ticket_anonyme_ne_notifie_personne() {
        SupportTicket ticket = existing(TicketStatus.NON_TRAITE, null);
        when(supportTicketRepository.findById(ticket.getId())).thenReturn(Optional.of(ticket));
        when(supportTicketRepository.save(any(SupportTicket.class))).thenAnswer(inv -> inv.getArgument(0));

        supportService.updateTicketStatus(ticket.getId(), TicketStatus.TRAITE);

        verify(userMessageService, never()).send(any(), anyString(), anyString());
    }

    @Test
    void reenregistrer_le_statut_traite_ne_notifie_pas_une_seconde_fois() {
        UUID userId = UUID.randomUUID();
        SupportTicket ticket = existing(TicketStatus.TRAITE, userId);
        when(supportTicketRepository.findById(ticket.getId())).thenReturn(Optional.of(ticket));
        when(supportTicketRepository.save(any(SupportTicket.class))).thenAnswer(inv -> inv.getArgument(0));

        supportService.updateTicketStatus(ticket.getId(), TicketStatus.TRAITE);

        verify(userMessageService, never()).send(any(), anyString(), anyString());
    }

    @Test
    void rouvrir_un_ticket_ne_notifie_pas() {
        UUID userId = UUID.randomUUID();
        SupportTicket ticket = existing(TicketStatus.TRAITE, userId);
        when(supportTicketRepository.findById(ticket.getId())).thenReturn(Optional.of(ticket));
        when(supportTicketRepository.save(any(SupportTicket.class))).thenAnswer(inv -> inv.getArgument(0));

        SupportTicket result = supportService.updateTicketStatus(ticket.getId(), TicketStatus.NON_TRAITE);

        assertThat(result.getStatus()).isEqualTo(TicketStatus.NON_TRAITE);
        verify(userMessageService, never()).send(any(), anyString(), anyString());
    }

    @Test
    void update_leve_not_found_quand_ticket_inexistant() {
        UUID id = UUID.randomUUID();
        when(supportTicketRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> supportService.updateTicketStatus(id, TicketStatus.TRAITE))
                .isInstanceOf(SupportTicketNotFoundException.class);

        verify(userMessageService, never()).send(any(), anyString(), anyString());
    }
}
