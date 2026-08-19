package com.geolink.findme.presentation.dto;

import com.geolink.findme.business.model.Role;
import jakarta.validation.constraints.NotNull;

public record UpdateRoleRequest(@NotNull Role role) {
}
