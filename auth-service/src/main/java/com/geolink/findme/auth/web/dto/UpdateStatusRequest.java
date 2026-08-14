package com.geolink.findme.auth.web.dto;

import com.geolink.findme.auth.domain.model.AccountStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateStatusRequest(@NotNull AccountStatus statut) {
}
