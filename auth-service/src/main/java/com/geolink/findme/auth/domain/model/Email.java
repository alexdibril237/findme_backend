package com.geolink.findme.auth.domain.model;

import java.util.Locale;
import java.util.regex.Pattern;

public record Email(String value) {

    private static final Pattern PATTERN =
            Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    public Email {

        if (value == null) {
            throw new IllegalArgumentException("Adresse email invalide : " + value);
        }
        value = value.trim().toLowerCase(Locale.ROOT);
        if (!PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("Adresse email invalide : " + value);
        }
    }

    @Override
    public String toString() {
        return value;
    }
}