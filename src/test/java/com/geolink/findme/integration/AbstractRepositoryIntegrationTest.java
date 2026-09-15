package com.geolink.findme.integration;

import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Socle commun des tests d'intégration de persistance : démarre une vraie base PostgreSQL 16
 * éphémère (Testcontainers) sur laquelle Flyway applique les migrations réelles, puis expose les
 * repositories Spring Data JPA du module {@code com.geolink.findme.data.repository}.
 *
 * <p>{@code @AutoConfigureTestDatabase(replace = NONE)} empêche Spring de substituer une base
 * embarquée (H2) à la vraie DataSource PostgreSQL fournie par {@code @ServiceConnection}. Chaque
 * méthode de test s'exécute dans une transaction annulée à la fin (comportement par défaut de
 * {@code @DataJpaTest}), donc les écritures d'un test ne polluent jamais les suivants — seules les
 * données de démonstration insérées par les migrations Flyway (V7, V8, V13) restent visibles.
 *
 * <p>Ces tests couvrent volontairement ce qu'un test unitaire (mocks) ne peut pas vérifier : les
 * contraintes SQL réelles (unicité, clés étrangères, trigger de quota) et le comportement des
 * requêtes {@code @Query} face à de vraies données. Ils ne passent jamais par la couche HTTP.
 *
 * <p>{@code @DataJpaTest} n'active pas Flyway par défaut (il ne configure que la couche JPA) :
 * {@code @ImportAutoConfiguration(FlywayAutoConfiguration.class)} le réactive explicitement,
 * sinon Hibernate ({@code ddl-auto: validate}) échouerait faute de schéma.
 *
 * <p>Pattern "singleton container" (recommandé par Testcontainers pour partager une base entre
 * plusieurs classes de test) : le conteneur est démarré une seule fois dans un bloc statique et
 * jamais arrêté explicitement (Ryuk le nettoie à la fin de la JVM). On évite volontairement
 * {@code @Testcontainers}/{@code @Container}, qui arrêteraient le conteneur après chaque classe
 * de test et invalideraient le port JDBC mémorisé par le contexte Spring mis en cache — ce qui
 * provoquait des {@code Connection refused} sur toutes les classes {@code *IT} sauf la première.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ImportAutoConfiguration(FlywayAutoConfiguration.class)
public abstract class AbstractRepositoryIntegrationTest {

    @ServiceConnection
    @SuppressWarnings("resource")
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16");

    static {
        POSTGRES.start();
    }
}
