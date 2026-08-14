package com.geolink.findme.gateway.security;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

/**
 * Première ligne de validation JWT (signature + expiration), avant que la requête ne soit
 * proxyfiée vers le microservice cible. Chaque microservice revalide indépendamment le même
 * token (défense en profondeur, voir docs/conception §7.1) : la Gateway ne fait AUCUN choix
 * d'autorisation par rôle, uniquement un rejet rapide des tokens manifestement invalides.
 */
@Component
public class GatewayJwtValidator {

    private final SecretKey signingKey;

    public GatewayJwtValidator(@Value("${jwt.secret}") String secret) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public boolean isValid(String token) {
        try {
            Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
