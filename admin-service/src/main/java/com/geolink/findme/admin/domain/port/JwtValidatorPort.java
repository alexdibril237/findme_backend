package com.geolink.findme.admin.domain.port;

public interface JwtValidatorPort {

    /** Lève {@link com.geolink.findme.admin.domain.exception.InvalidTokenException} si invalide/expiré. */
    TokenClaims parseAndValidate(String token);
}
