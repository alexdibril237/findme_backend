package com.geolink.findme.auth;

import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Socle commun des tests d'intégration de l'auth-service : démarre le contexte Spring complet
 * (couche web + sécurité + JPA + Flyway) contre une vraie base PostgreSQL 16 éphémère fournie
 * par Testcontainers.
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

    @Container
    @ServiceConnection
    @SuppressWarnings("resource")
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16");

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("jwt.secret", () -> JWT_SECRET);
    }
}