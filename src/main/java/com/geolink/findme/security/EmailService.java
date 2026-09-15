package com.geolink.findme.security;

/** Envoi d'emails transactionnels (réinitialisation de mot de passe). */
public interface EmailService {

    void sendPasswordResetCode(String toEmail, String code);
}
