package com.geolink.findme.business.exception;

public class AddressQuotaExceededException extends RuntimeException {

    public AddressQuotaExceededException(int max) {
        super("Limite de " + max + " adresses atteinte pour cet utilisateur");
    }
}
