package com.geolink.findme.business.exception;

public class AccountDisabledException extends RuntimeException {

    public AccountDisabledException() {
        super("Ce compte a été désactivé");
    }
}
