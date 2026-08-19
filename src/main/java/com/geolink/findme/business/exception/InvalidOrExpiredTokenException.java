package com.geolink.findme.business.exception;

public class InvalidOrExpiredTokenException extends RuntimeException {

    public InvalidOrExpiredTokenException(String message) {
        super(message);
    }
}
