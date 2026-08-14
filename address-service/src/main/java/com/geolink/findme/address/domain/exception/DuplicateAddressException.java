package com.geolink.findme.address.domain.exception;

public class DuplicateAddressException extends RuntimeException {

    public DuplicateAddressException() {
        super("Cette adresse est déjà enregistrée pour cet utilisateur");
    }
}
