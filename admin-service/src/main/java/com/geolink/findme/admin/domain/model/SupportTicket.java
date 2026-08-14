package com.geolink.findme.admin.domain.model;

import java.time.Instant;
import java.util.UUID;

public class SupportTicket {

    private final UUID id;
    private final String name;
    private final String email;
    private final String message;
    private TicketStatus status;
    private final Instant createdAt;
    private Instant updatedAt;

    private SupportTicket(UUID id, String name, String email, String message, TicketStatus status,
                           Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.message = message;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static SupportTicket createNew(String name, String email, String message) {
        Instant now = Instant.now();
        return new SupportTicket(UUID.randomUUID(), name, email, message, TicketStatus.NON_TRAITE, now, now);
    }

    public static SupportTicket rehydrate(UUID id, String name, String email, String message, TicketStatus status,
                                           Instant createdAt, Instant updatedAt) {
        return new SupportTicket(id, name, email, message, status, createdAt, updatedAt);
    }

    public void markTreated() {
        this.status = TicketStatus.TRAITE;
        this.updatedAt = Instant.now();
    }

    public void markPending() {
        this.status = TicketStatus.NON_TRAITE;
        this.updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getMessage() {
        return message;
    }

    public TicketStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
