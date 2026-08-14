package com.geolink.findme.admin.domain.exception;

import java.util.UUID;

public class SupportTicketNotFoundException extends RuntimeException {

    public SupportTicketNotFoundException(UUID id) {
        super("Ticket de support introuvable : " + id);
    }
}
