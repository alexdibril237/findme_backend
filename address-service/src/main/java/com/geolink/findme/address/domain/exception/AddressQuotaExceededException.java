package com.geolink.findme.address.domain.exception;

public class AddressQuotaExceededException extends RuntimeException {

    public AddressQuotaExceededException(int max) {
        super("Limite de " + max + " adresses atteinte pour cet utilisateur");
    }
}
