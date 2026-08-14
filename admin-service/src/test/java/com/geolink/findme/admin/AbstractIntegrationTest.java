package com.geolink.findme.admin;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

/**
 * Socle des tests d'intégration de l'admin-service : contexte Spring complet + PostgreSQL 16
 * éphémère (Testcontainers). Les endpoints testés ici (formulaire public de support, RBAC des
 * routes /api/admin/**) ne dépendent pas des appels REST vers auth/address-service.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
public abstract class AbstractIntegrationTest {

    protected static final String JWT_SECRET = "test-secret-test-secret-test-secret-test-secret";
    private static final SecretKey SIGNING_KEY = Keys.hmacShaKeyFor(JWT_SECRET.getBytes(StandardCharsets.UTF_8));

    @Container
    @ServiceConnection
    @SuppressWarnings("resource")
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16");

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("jwt.secret", () -> JWT_SECRET);
    }

    protected String bearer(String role) {
        Instant now = Instant.now();
        String token = Jwts.builder()
                .subject(UUID.randomUUID().toString())
                .claim("email", "agent@geolink.africa")
                .claim("role", role)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(900)))
                .signWith(SIGNING_KEY)
                .compact();
        return "Bearer " + token;
    }
}
