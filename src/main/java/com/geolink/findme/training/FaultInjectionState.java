package com.geolink.findme.training;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * État partagé de l'injection de pannes (latence / erreurs artificielles), piloté par
 * {@link FaultInjectionController} et appliqué par {@link FaultInjectionFilter}.
 * N'existe qu'avec le profil Spring "training" (Module 7 Partie 2, CHAP 4) : sert à
 * déclencher volontairement les alertes Prometheus et valider le cycle
 * détection → diagnostic → retour à la normale, jamais activable en production.
 */
@Component
@Profile("training")
public class FaultInjectionState {

    private volatile int delayMs = 0;
    private volatile double errorRate = 0.0;

    public void configure(int delayMs, double errorRate) {
        this.delayMs = delayMs;
        this.errorRate = errorRate;
    }

    public int delayMs() {
        return delayMs;
    }

    public double errorRate() {
        return errorRate;
    }
}
