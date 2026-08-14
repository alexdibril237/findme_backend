package com.geolink.findme.admin.infrastructure.security;

import com.geolink.findme.admin.domain.exception.InvalidTokenException;
import com.geolink.findme.admin.domain.model.Role;
import com.geolink.findme.admin.domain.port.JwtValidatorPort;
import com.geolink.findme.admin.domain.port.TokenClaims;
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
