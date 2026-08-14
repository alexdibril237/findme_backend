package com.geolink.findme.auth.domain.port;

import com.geolink.findme.auth.domain.model.User;

public interface JwtIssuerPort {

    String issueAccessToken(User user);

    /** Lève {@link com.geolink.findme.auth.domain.exception.InvalidOrExpiredTokenException} si invalide/expiré. */
    TokenClaims parseAndValidate(String accessToken);
}