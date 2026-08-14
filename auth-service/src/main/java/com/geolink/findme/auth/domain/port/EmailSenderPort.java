package com.geolink.findme.auth.domain.port;

/**
 * Port d'envoi d'email. L'implémentation par défaut (infrastructure) journalise le lien plutôt
 * que d'appeler un fournisseur SMTP réel : le choix du fournisseur (SendGrid, SES, Resend...) est
 * hors périmètre du contrat backend figé et peut être branché ultérieurement sans changer le domaine.
 */
public interface EmailSenderPort {

    void sendPasswordResetLink(String toEmail, String resetToken);
}