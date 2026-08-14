package com.geolink.findme.auth.web.dto;

import com.geolink.findme.auth.domain.model.Role;
import jakarta.validation.constraints.NotNull;

public record UpdateRoleRequest(@NotNull Role role) {
}
