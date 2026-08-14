package com.geolink.findme.address.infrastructure.security;

import com.geolink.findme.address.domain.exception.InvalidTokenException;
import com.geolink.findme.address.domain.model.Role;
import com.geolink.findme.address.domain.port.JwtValidatorPort;
import com.geolink.findme.address.domain.port.TokenClaims;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * Revalide localement le JWT émis par auth-service (même secret partagé via JWT_SECRET) :
 * défense en profondeur, indépendante de la validation déjà faite par l'API Gateway
 * (voir docs/conception §7.1).
 */
@Component
public class JwtService implements JwtValidatorPort {

    private final SecretKey signingKey;

    public JwtService(@Value("${jwt.secret}") String secret) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public TokenClaims parseAndValidate(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return new TokenClaims(UUID.fromString(claims.getSubject()), claims.get("email", String.class),
                    Role.valueOf(claims.get("role", String.class)));
        } catch (ExpiredJwtException e) {
            throw new InvalidTokenException("Token expiré");
        } catch (JwtException | IllegalArgumentException e) {
            throw new InvalidTokenException("Token invalide");
        }
    }
}
