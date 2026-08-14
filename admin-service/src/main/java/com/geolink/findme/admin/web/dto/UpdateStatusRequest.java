package com.geolink.findme.admin.web.dto;

import com.geolink.findme.admin.domain.model.AccountStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateStatusRequest(@NotNull AccountStatus statut) {
}
