package com.geolink.findme.auth.domain.exception;

public class WeakPasswordException extends RuntimeException {

    public WeakPasswordException() {
        super("Le mot de passe doit contenir au moins 8 caractères, une majuscule et un chiffre");
    }
}