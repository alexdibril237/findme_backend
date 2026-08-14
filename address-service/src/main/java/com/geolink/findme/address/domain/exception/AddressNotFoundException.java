package com.geolink.findme.address.domain.exception;

import java.util.UUID;

public class AddressNotFoundException extends RuntimeException {

    public AddressNotFoundException(UUID id) {
        super("Adresse introuvable : " + id);
    }
}
