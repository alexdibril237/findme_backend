package com.geolink.findme.auth.domain.exception;

public class EmailAlreadyUsedException extends RuntimeException {

    public EmailAlreadyUsedException(String email) {
        super("Un compte existe déjà avec l'adresse email : " + email);
    }
}