package com.geolink.findme.unit.presentation.mapper;
import com.geolink.findme.presentation.mapper.SupportWebMapper;

import com.geolink.findme.business.model.TicketStatus;
import com.geolink.findme.data.entity.SupportTicket;
import com.geolink.findme.presentation.dto.SupportTicketResponse;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SupportWebMapperTest {

    private final SupportWebMapper mapper = new SupportWebMapper();

    private SupportTicket sample(UUID userId) {
        SupportTicket ticket = new SupportTicket();
        ticket.setId(UUID.randomUUID());
        ticket.setName("Awa Ndiaye");
        ticket.setEmail("awa@example.com");
        ticket.setMessage("Mon adresse ne se vérifie pas.");
        ticket.setUserId(userId);
        ticket.setStatus(TicketStatus.NON_TRAITE);
        ticket.setCreatedAt(Instant.now());
        ticket.setUpdatedAt(Instant.now());
        return ticket;
    }

    @Test
    void toResponse_transporte_tous_les_champs() {
        SupportTicket ticket = sample(UUID.randomUUID());

        SupportTicketResponse response = mapper.toResponse(ticket);

        assertThat(response.id()).isEqualTo(ticket.getId());
        assertThat(response.nom()).isEqualTo("Awa Ndiaye");
        assertThat(response.email()).isEqualTo("awa@example.com");
        assertThat(response.message()).isEqualTo(ticket.getMessage());
        assertThat(response.userId()).isEqualTo(ticket.getUserId());
        assertThat(response.statut()).isEqualTo("NON_TRAITE");
    }

    @Test
    void toResponse_gere_un_ticket_anonyme_sans_userId() {
        SupportTicket ticket = sample(null);

        SupportTicketResponse response = mapper.toResponse(ticket);

        assertThat(response.userId()).isNull();
    }
}
