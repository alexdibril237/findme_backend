package com.geolink.findme.training;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Applique la latence/le taux d'erreur configurés via {@link FaultInjectionController} aux
 * requêtes réelles de {@code /api/addresses/**} (hors {@code _simulate} lui-même).
 * Usage formation uniquement (Module 7 Partie 2, CHAP 4) : n'est enregistré que si le
 * profil Spring "training" est actif.
 */
@Component
@Profile("training")
public class FaultInjectionFilter extends OncePerRequestFilter {

    private final FaultInjectionState state;

    public FaultInjectionFilter(FaultInjectionState state) {
        this.state = state;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String path = request.getRequestURI();
        if (path.startsWith("/api/addresses") && !path.endsWith("/_simulate")) {
            int delayMs = state.delayMs();
            if (delayMs > 0) {
                try {
                    Thread.sleep(delayMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            double errorRate = state.errorRate();
            if (errorRate > 0 && ThreadLocalRandom.current().nextDouble() < errorRate) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        "Erreur injectée (fault injection, profil training)");
                return;
            }
        }
        chain.doFilter(request, response);
    }
}
