package com.geolink.findme.admin.application.usecase;

import com.geolink.findme.admin.domain.exception.SupportTicketNotFoundException;
import com.geolink.findme.admin.domain.model.SupportTicket;
import com.geolink.findme.admin.domain.model.TicketStatus;
import com.geolink.findme.admin.domain.port.SupportTicketRepositoryPort;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public class UpdateSupportTicketStatusUseCase {

    private final SupportTicketRepositoryPort repository;

    public UpdateSupportTicketStatusUseCase(SupportTicketRepositoryPort repository) {
        this.repository = repository;
    }

    public record Command(UUID ticketId, TicketStatus newStatus) {
    }

    @Transactional
    public SupportTicket execute(Command command) {
        SupportTicket ticket = repository.findById(command.ticketId())
                .orElseThrow(() -> new SupportTicketNotFoundException(command.ticketId()));

        if (command.newStatus() == TicketStatus.TRAITE) {
            ticket.markTreated();
        } else {
            ticket.markPending();
        }
        return repository.save(ticket);
    }
}
