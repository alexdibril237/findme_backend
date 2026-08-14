package com.geolink.findme.auth.domain.exception;

/** Message volontairement générique : ne révèle jamais si l'email existe ou si c'est le mot de passe qui est faux. */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("Email ou mot de passe incorrect");
    }
}