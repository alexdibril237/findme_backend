package com.geolink.findme.business.exception;

public class WeakPasswordException extends RuntimeException {

    public WeakPasswordException() {
        super("Le mot de passe doit contenir au moins 8 caractères, une majuscule et un chiffre");
    }
}
