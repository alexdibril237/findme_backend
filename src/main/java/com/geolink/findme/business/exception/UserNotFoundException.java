package com.geolink.findme.business.exception;

import java.util.UUID;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(UUID userId) {
        super("Utilisateur introuvable : " + userId);
    }
}
