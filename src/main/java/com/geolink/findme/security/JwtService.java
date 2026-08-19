package com.geolink.findme.security;

import com.geolink.findme.business.exception.InvalidOrExpiredTokenException;
import com.geolink.findme.data.entity.User;

public interface JwtService {

    String issueAccessToken(User user);

    /** Lève {@link InvalidOrExpiredTokenException} si le token est invalide ou expiré. */
    TokenClaims parseAndValidate(String accessToken);
}
