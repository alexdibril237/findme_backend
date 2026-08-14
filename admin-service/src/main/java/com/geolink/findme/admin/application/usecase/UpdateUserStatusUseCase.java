package com.geolink.findme.admin.application.usecase;

import com.geolink.findme.admin.domain.model.AccountStatus;
import com.geolink.findme.admin.domain.port.AuthServiceClientPort;

import java.util.UUID;

public class UpdateUserStatusUseCase {

    private final AuthServiceClientPort authServiceClient;

    public UpdateUserStatusUseCase(AuthServiceClientPort authServiceClient) {
        this.authServiceClient = authServiceClient;
    }

    public record Command(UUID userId, AccountStatus newStatus) {
    }

    public void execute(Command command) {
        authServiceClient.updateUserStatus(command.userId(), command.newStatus());
    }
}
