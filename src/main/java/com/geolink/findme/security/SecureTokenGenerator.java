package com.geolink.findme.security;

/** Génération et hashing des tokens opaques (refresh token, token de réinitialisation de mot de passe). */
public interface SecureTokenGenerator {

    /** Chaîne aléatoire encodée base64url, à transmettre en clair une seule fois au client. */
    String generateOpaqueToken();

    /** Hash déterministe (SHA-256) permettant de retrouver le token en base sans jamais y stocker sa valeur claire. */
    String hash(String rawToken);
}
