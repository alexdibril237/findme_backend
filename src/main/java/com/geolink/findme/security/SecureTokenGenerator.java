package com.geolink.findme.security;

/** Génération et hashing des tokens opaques (refresh token, token de réinitialisation de mot de passe). */
public interface SecureTokenGenerator {

    /** Chaîne aléatoire encodée base64url, à transmettre en clair une seule fois au client. */
    String generateOpaqueToken();

    /** Code numérique à 6 chiffres (zero-paddé), pour un envoi par email lisible par l'utilisateur. */
    String generateNumericCode();

    /** Hash déterministe (SHA-256) permettant de retrouver le token en base sans jamais y stocker sa valeur claire. */
    String hash(String rawToken);
}
