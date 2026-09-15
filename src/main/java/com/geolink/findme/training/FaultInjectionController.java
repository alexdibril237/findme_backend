package com.geolink.findme.training;

import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Contrôleur de simulation de panne, usage formation uniquement (Module 7 Partie 2, CHAP 4).
 * Permet de déclencher une dégradation contrôlée (latence, taux d'erreur) sur
 * {@code /api/addresses/**} pour valider les règles d'alerte Prometheus définies dans
 * {@code infra/prometheus/rules.yml}. Protégé par {@code @Profile("training")} : ce
 * contrôleur n'existe pas quand ce profil n'est pas actif (404 systématique).
 */
@RestController
@RequestMapping("/api/addresses/_simulate")
@Profile("training")
public class FaultInjectionController {

    private final FaultInjectionState state;

    public FaultInjectionController(FaultInjectionState state) {
        this.state = state;
    }

    @PostMapping
    public void configure(@RequestParam(defaultValue = "0") int delayMs,
                           @RequestParam(defaultValue = "0.0") double errorRate) {
        state.configure(delayMs, errorRate);
    }
}
