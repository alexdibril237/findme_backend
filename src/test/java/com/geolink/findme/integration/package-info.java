/**
 * Tests d'intégration (classes suffixées {@code *IT}, exécutées par maven-failsafe-plugin via
 * {@code mvn verify}, jamais par {@code mvn test}). Ne pas y placer de tests qui pilotent
 * l'API HTTP (MockMvc) : cette base de code réserve l'intégration aux couches non-API
 * (ex. persistance JPA/Flyway via Testcontainers). Les tests unitaires vivent dans
 * {@link com.geolink.findme.unit}.
 */
package com.geolink.findme.integration;
