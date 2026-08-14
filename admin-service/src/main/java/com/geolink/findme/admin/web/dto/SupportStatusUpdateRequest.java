package com.geolink.findme.admin.web.dto;

import com.geolink.findme.admin.domain.model.TicketStatus;
import jakarta.validation.constraints.NotNull;

public record SupportStatusUpdateRequest(@NotNull TicketStatus statut) {
}
