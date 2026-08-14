package com.geolink.findme.admin.application.usecase;

import com.geolink.findme.admin.domain.model.Role;
import com.geolink.findme.admin.domain.port.AuthServiceClientPort;

import java.util.UUID;

public class UpdateUserRoleUseCase {

    private final AuthServiceClientPort authServiceClient;

    public UpdateUserRoleUseCase(AuthServiceClientPort authServiceClient) {
        this.authServiceClient = authServiceClient;
    }

    public record Command(UUID userId, Role newRole) {
    }

    public void execute(Command command) {
        authServiceClient.updateUserRole(command.userId(), command.newRole());
    }
}
