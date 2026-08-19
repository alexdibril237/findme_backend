package com.geolink.findme.presentation.dto;

import com.geolink.findme.business.model.AccountStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateStatusRequest(@NotNull AccountStatus statut) {
}
