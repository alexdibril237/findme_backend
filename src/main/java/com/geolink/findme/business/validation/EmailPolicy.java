package com.geolink.findme.business.validation;

import java.util.Locale;
import java.util.regex.Pattern;

/** Normalise et revalide une adresse email côté serveur, indépendamment de la validation Bean Validation du DTO. */
public final class EmailPolicy {

    private static final Pattern PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private EmailPolicy() {
    }

    public static String normalize(String rawEmail) {
        if (rawEmail == null) {
            throw new IllegalArgumentException("Adresse email invalide : null");
        }
        String normalized = rawEmail.trim().toLowerCase(Locale.ROOT);
        if (!PATTERN.matcher(normalized).matches()) {
            throw new IllegalArgumentException("Adresse email invalide : " + normalized);
        }
        return normalized;
    }
}
