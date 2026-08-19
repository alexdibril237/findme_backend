package com.geolink.findme;

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
 * Socle commun des tests d'intégration : démarre le contexte Spring complet (couche web +
 * sécurité + JPA + Flyway) contre une vraie base PostgreSQL 16 éphémère fournie par
 * Testcontainers. Le stockage des photos est redirigé vers un dossier temporaire.
 *
 * <p>{@code @ServiceConnection} câble automatiquement la DataSource ET Flyway sur le conteneur
 * (URL/port dynamiques), ce qui évite le piège d'un {@code @DynamicPropertySource} appliqué trop
 * tard où Flyway tenterait de se connecter à {@code localhost:5432}. Le conteneur est statique,
 * donc démarré une seule fois et partagé par toutes les classes de test qui héritent de cette base.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
public abstract class AbstractIntegrationTest {

    /** Secret HMAC-SHA256 déterministe (>= 32 octets) pour signer/valider les JWT en test. */
    protected static final String JWT_SECRET = "test-secret-test-secret-test-secret-test-secret";
    private static final SecretKey SIGNING_KEY = Keys.hmacShaKeyFor(JWT_SECRET.getBytes(StandardCharsets.UTF_8));

    @Container
    @ServiceConnection
    @SuppressWarnings("resource")
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16");

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("jwt.secret", () -> JWT_SECRET);
        registry.add("storage.root", () -> System.getProperty("java.io.tmpdir") + "/findme-test-photos");
    }

    /** Forge un access token équivalent à celui émis par {@code AuthService} (sub, email, role). */
    protected String tokenFor(UUID userId, String role) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(userId.toString())
                .claim("email", "user-" + userId + "@geolink.africa")
                .claim("role", role)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(900)))
                .signWith(SIGNING_KEY)
                .compact();
    }

    protected String bearer(UUID userId, String role) {
        return "Bearer " + tokenFor(userId, role);
    }

    protected String bearer(String role) {
        return bearer(UUID.randomUUID(), role);
    }
}
