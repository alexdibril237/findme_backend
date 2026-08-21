package com.geolink.findme.business.service;

import com.geolink.findme.business.exception.SupportTicketNotFoundException;
import com.geolink.findme.business.model.TicketStatus;
import com.geolink.findme.data.entity.SupportTicket;
import com.geolink.findme.data.repository.SupportTicketRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class SupportServiceImpl implements SupportService {

    private final SupportTicketRepository supportTicketRepository;
    private final UserMessageService userMessageService;

    public SupportServiceImpl(SupportTicketRepository supportTicketRepository, UserMessageService userMessageService) {
        this.supportTicketRepository = supportTicketRepository;
        this.userMessageService = userMessageService;
    }

    @Override
    @Transactional
    public SupportTicket createTicket(String name, String email, String message, UUID userId) {
        Instant now = Instant.now();
        SupportTicket ticket = new SupportTicket();
        ticket.setId(UUID.randomUUID());
        ticket.setName(name);
        ticket.setEmail(email);
        ticket.setMessage(message);
        ticket.setUserId(userId);
        ticket.setStatus(TicketStatus.NON_TRAITE);
        ticket.setCreatedAt(now);
        ticket.setUpdatedAt(now);
        return supportTicketRepository.save(ticket);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SupportTicket> listTickets(Pageable pageable, TicketStatus statusFilter) {
        if (statusFilter == null) {
            return supportTicketRepository.findAll(pageable);
        }
        return supportTicketRepository.findByStatus(statusFilter, pageable);
    }

    @Override
    @Transactional
    public SupportTicket updateTicketStatus(UUID ticketId, TicketStatus newStatus) {
        SupportTicket ticket = supportTicketRepository.findById(ticketId)
                .orElseThrow(() -> new SupportTicketNotFoundException(ticketId));

        boolean justResolved = newStatus == TicketStatus.TRAITE && ticket.getStatus() != TicketStatus.TRAITE;

        ticket.setStatus(newStatus);
        ticket.setUpdatedAt(Instant.now());
        SupportTicket saved = supportTicketRepository.save(ticket);

        // Notifie automatiquement l'auteur du ticket quand il est resolu, s'il a soumis
        // sa demande en etant connecte (cf. V12 : les tickets anonymes n'ont pas de userId).
        if (justResolved && ticket.getUserId() != null) {
            userMessageService.send(ticket.getUserId(), "Votre demande a été traitée",
                    "Bonjour,\n\nVotre demande de support a été traitée par notre équipe.\n\n"
                            + "Message d'origine :\n" + ticket.getMessage()
                            + "\n\nCordialement,\nL'équipe findMe");
        }

        return saved;
    }
}
