package com.geolink.findme.admin.infrastructure.config;

import com.geolink.findme.admin.application.usecase.CreateSupportTicketUseCase;
import com.geolink.findme.admin.application.usecase.ListAddressesUseCase;
import com.geolink.findme.admin.application.usecase.ListSupportTicketsUseCase;
import com.geolink.findme.admin.application.usecase.ListUsersUseCase;
import com.geolink.findme.admin.application.usecase.UpdateSupportTicketStatusUseCase;
import com.geolink.findme.admin.application.usecase.UpdateUserRoleUseCase;
import com.geolink.findme.admin.application.usecase.UpdateUserStatusUseCase;
import com.geolink.findme.admin.domain.port.AddressServiceClientPort;
import com.geolink.findme.admin.domain.port.AuthServiceClientPort;
import com.geolink.findme.admin.domain.port.SupportTicketRepositoryPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {

    @Bean
    public CreateSupportTicketUseCase createSupportTicketUseCase(SupportTicketRepositoryPort repository) {
        return new CreateSupportTicketUseCase(repository);
    }

    @Bean
    public ListSupportTicketsUseCase listSupportTicketsUseCase(SupportTicketRepositoryPort repository) {
        return new ListSupportTicketsUseCase(repository);
    }

    @Bean
    public UpdateSupportTicketStatusUseCase updateSupportTicketStatusUseCase(SupportTicketRepositoryPort repository) {
        return new UpdateSupportTicketStatusUseCase(repository);
    }

    @Bean
    public ListUsersUseCase listUsersUseCase(AuthServiceClientPort authServiceClient) {
        return new ListUsersUseCase(authServiceClient);
    }

    @Bean
    public ListAddressesUseCase listAddressesUseCase(AddressServiceClientPort addressServiceClient) {
        return new ListAddressesUseCase(addressServiceClient);
    }

    @Bean
    public UpdateUserRoleUseCase updateUserRoleUseCase(AuthServiceClientPort authServiceClient) {
        return new UpdateUserRoleUseCase(authServiceClient);
    }

    @Bean
    public UpdateUserStatusUseCase updateUserStatusUseCase(AuthServiceClientPort authServiceClient) {
        return new UpdateUserStatusUseCase(authServiceClient);
    }
}
