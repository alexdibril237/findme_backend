package com.geolink.findme.presentation.mapper;

import com.geolink.findme.data.entity.SupportTicket;
import com.geolink.findme.presentation.dto.SupportTicketResponse;
import org.springframework.stereotype.Component;

@Component
public class SupportWebMapper {

    public SupportTicketResponse toResponse(SupportTicket ticket) {
        return new SupportTicketResponse(ticket.getId(), ticket.getName(), ticket.getEmail(), ticket.getMessage(),
                ticket.getStatus().name(), ticket.getCreatedAt(), ticket.getUpdatedAt());
    }
}
