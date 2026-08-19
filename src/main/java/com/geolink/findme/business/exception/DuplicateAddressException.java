package com.geolink.findme.business.exception;

public class DuplicateAddressException extends RuntimeException {

    public DuplicateAddressException() {
        super("Cette adresse est déjà enregistrée pour cet utilisateur");
    }
}
