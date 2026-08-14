package com.geolink.findme.admin.application.usecase;

import com.geolink.findme.admin.domain.model.SupportTicket;
import com.geolink.findme.admin.domain.port.SupportTicketRepositoryPort;
import org.springframework.transaction.annotation.Transactional;

public class CreateSupportTicketUseCase {

    private final SupportTicketRepositoryPort repository;

    public CreateSupportTicketUseCase(SupportTicketRepositoryPort repository) {
        this.repository = repository;
    }

    public record Command(String name, String email, String message) {
    }

    @Transactional
    public SupportTicket execute(Command command) {
        return repository.save(SupportTicket.createNew(command.name(), command.email(), command.message()));
    }
}
