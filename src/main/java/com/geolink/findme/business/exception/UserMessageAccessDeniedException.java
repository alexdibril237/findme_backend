package com.geolink.findme.business.exception;

public class UserMessageAccessDeniedException extends RuntimeException {

    public UserMessageAccessDeniedException() {
        super("Vous n'êtes pas autorisé à accéder à ce message");
    }
}
