package com.geolink.findme.auth.domain.exception;

public class AccountDisabledException extends RuntimeException {

    public AccountDisabledException() {
        super("Ce compte a été désactivé");
    }
}