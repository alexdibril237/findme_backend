package com.geolink.findme.presentation.dto;

import com.geolink.findme.business.model.TicketStatus;
import jakarta.validation.constraints.NotNull;

public record SupportStatusUpdateRequest(@NotNull TicketStatus statut) {
}
