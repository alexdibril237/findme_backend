package com.geolink.findme.security;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CurrentUserProvider {

    public UUID requireCurrentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return UUID.fromString((String) principal);
    }

    /**
     * Utilise sur les endpoints publics (ex. formulaire de support) : renvoie l'id de
     * l'utilisateur si la requete porte un token valide, sinon {@code null} sans lever
     * d'exception (contrairement a {@link #requireCurrentUserId()}).
     */
    public UUID currentUserIdOrNull() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            return null;
        }
        try {
            return UUID.fromString((String) authentication.getPrincipal());
        } catch (IllegalArgumentException | ClassCastException e) {
            return null;
        }
    }
}
