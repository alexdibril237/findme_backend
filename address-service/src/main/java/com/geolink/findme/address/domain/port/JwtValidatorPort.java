package com.geolink.findme.address.domain.port;

public interface JwtValidatorPort {

    /** Lève {@link com.geolink.findme.address.domain.exception.InvalidTokenException} si invalide/expiré. */
    TokenClaims parseAndValidate(String token);
}
