package com.geolink.findme.address.domain.exception;

public class AddressAccessDeniedException extends RuntimeException {

    public AddressAccessDeniedException() {
        super("Vous n'êtes pas autorisé à accéder à cette adresse");
    }
}
