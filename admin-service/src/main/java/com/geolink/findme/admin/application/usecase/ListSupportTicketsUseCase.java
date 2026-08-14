package com.geolink.findme.admin.application.usecase;

import com.geolink.findme.admin.domain.model.SupportTicket;
import com.geolink.findme.admin.domain.model.TicketStatus;
import com.geolink.findme.admin.domain.port.SupportTicketRepositoryPort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

public class ListSupportTicketsUseCase {

    private final SupportTicketRepositoryPort repository;

    public ListSupportTicketsUseCase(SupportTicketRepositoryPort repository) {
        this.repository = repository;
    }

    public record Query(Pageable pageable, TicketStatus statusFilter) {
    }

    @Transactional(readOnly = true)
    public Page<SupportTicket> execute(Query query) {
        return repository.findPage(query.pageable(), query.statusFilter());
    }
}
