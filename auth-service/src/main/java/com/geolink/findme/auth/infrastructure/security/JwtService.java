package com.geolink.findme.auth.infrastructure.security;

import com.geolink.findme.auth.domain.exception.InvalidOrExpiredTokenException;
import com.geolink.findme.auth.domain.model.Role;
import com.geolink.findme.auth.domain.model.User;
import com.geolink.findme.auth.domain.port.JwtIssuerPort;
import com.geolink.findme.auth.domain.port.TokenClaims;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtService implements JwtIssuerPort {

    private final SecretKey signingKey;
    private final Duration accessTokenTtl;

    public JwtService(@Value("${jwt.secret}") String secret,
                       @Value("${jwt.access-token-ttl-minutes:15}") long accessTokenTtlMinutes) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenTtl = Duration.ofMinutes(accessTokenTtlMinutes);
    }

    @Override
    public String issueAccessToken(User user) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("email", user.getEmail().value())
                .claim("role", user.getRole().name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(accessTokenTtl)))
                .signWith(signingKey)
                .compact();
    }

    @Override
    public TokenClaims parseAndValidate(String accessToken) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(accessToken)
                    .getPayload();

            return new TokenClaims(UUID.fromString(claims.getSubject()), claims.get("email", String.class),
                    Role.valueOf(claims.get("role", String.class)));
        } catch (ExpiredJwtException e) {
            throw new InvalidOrExpiredTokenException("Token expiré");
        } catch (JwtException | IllegalArgumentException e) {
            throw new InvalidOrExpiredTokenException("Token invalide");
        }
    }
}
