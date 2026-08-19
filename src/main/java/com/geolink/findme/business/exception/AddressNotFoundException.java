package com.geolink.findme.business.exception;

import java.util.UUID;

public class AddressNotFoundException extends RuntimeException {

    public AddressNotFoundException(UUID id) {
        super("Adresse introuvable : " + id);
    }
}
