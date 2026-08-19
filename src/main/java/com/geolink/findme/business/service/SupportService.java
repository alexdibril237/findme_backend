package com.geolink.findme.business.service;

import com.geolink.findme.business.model.TicketStatus;
import com.geolink.findme.data.entity.SupportTicket;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface SupportService {

    SupportTicket createTicket(String name, String email, String message);

    Page<SupportTicket> listTickets(Pageable pageable, TicketStatus statusFilter);

    SupportTicket updateTicketStatus(UUID ticketId, TicketStatus newStatus);
}
