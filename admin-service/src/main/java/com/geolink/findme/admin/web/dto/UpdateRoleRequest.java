package com.geolink.findme.admin.web.dto;

import com.geolink.findme.admin.domain.model.Role;
import jakarta.validation.constraints.NotNull;

public record UpdateRoleRequest(@NotNull Role role) {
}
