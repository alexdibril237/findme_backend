package com.geolink.findme.admin.domain.port;

import com.geolink.findme.admin.domain.model.SupportTicket;
import com.geolink.findme.admin.domain.model.TicketStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface SupportTicketRepositoryPort {

    SupportTicket save(SupportTicket ticket);

    Optional<SupportTicket> findById(UUID id);

    Page<SupportTicket> findPage(Pageable pageable, TicketStatus statusFilter);
}
