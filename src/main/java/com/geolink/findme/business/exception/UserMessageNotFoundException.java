package com.geolink.findme.business.exception;

import java.util.UUID;

public class UserMessageNotFoundException extends RuntimeException {

    public UserMessageNotFoundException(UUID id) {
        super("Message introuvable : " + id);
    }
}
