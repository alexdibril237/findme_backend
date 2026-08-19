package com.geolink.findme.business.validation;

import com.geolink.findme.business.exception.WeakPasswordException;

import java.util.regex.Pattern;

/**
 * Règle métier revalidée côté serveur indépendamment du frontend (cahier des charges §2.1) :
 * au moins 8 caractères, une majuscule et un chiffre.
 */
public final class PasswordPolicy {

    private static final Pattern UPPERCASE = Pattern.compile("[A-Z]");
    private static final Pattern DIGIT = Pattern.compile("[0-9]");
    private static final int MIN_LENGTH = 8;

    private PasswordPolicy() {
    }

    public static void validate(String rawPassword) {
        if (rawPassword == null
                || rawPassword.length() < MIN_LENGTH
                || !UPPERCASE.matcher(rawPassword).find()
                || !DIGIT.matcher(rawPassword).find()) {
            throw new WeakPasswordException();
        }
    }
}
