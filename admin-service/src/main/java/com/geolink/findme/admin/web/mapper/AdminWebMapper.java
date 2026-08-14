package com.geolink.findme.admin.web.mapper;

import com.geolink.findme.admin.domain.model.AddressSummary;
import com.geolink.findme.admin.domain.model.SupportTicket;
import com.geolink.findme.admin.domain.model.UserSummary;
import com.geolink.findme.admin.web.dto.AddressSummaryResponse;
import com.geolink.findme.admin.web.dto.SupportTicketResponse;
import com.geolink.findme.admin.web.dto.UserSummaryResponse;
import org.springframework.stereotype.Component;

@Component
public class AdminWebMapper {

    public SupportTicketResponse toResponse(SupportTicket ticket) {
        return new SupportTicketResponse(ticket.getId(), ticket.getName(), ticket.getEmail(), ticket.getMessage(),
                ticket.getStatus().name(), ticket.getCreatedAt(), ticket.getUpdatedAt());
    }

    public UserSummaryResponse toResponse(UserSummary user) {
        return new UserSummaryResponse(user.id(), user.email(), user.prenom(), user.nom(), user.role().name(),
                user.statutCompte(), user.dateCreation(), user.dateDerniereConnexion());
    }

    public AddressSummaryResponse toResponse(AddressSummary address) {
        return new AddressSummaryResponse(address.id(), address.userId(), address.pays(), address.ville(),
                address.quartier(), address.rue(), address.numero(), address.dateCreation());
    }
}
