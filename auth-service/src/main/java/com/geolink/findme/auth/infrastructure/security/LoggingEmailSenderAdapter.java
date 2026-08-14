package com.geolink.findme.auth.infrastructure.security;

import com.geolink.findme.auth.domain.port.EmailSenderPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Adaptateur par défaut : journalise le lien de réinitialisation au lieu d'appeler un fournisseur
 * SMTP réel (hors périmètre du contrat backend figé — voir docs/conception §7). Ne jamais logger
 * le token en clair en production ; conservé ici volontairement pour faciliter la démonstration/soutenance.
 */
@Component
public class LoggingEmailSenderAdapter implements EmailSenderPort {

    private static final Logger log = LoggerFactory.getLogger(LoggingEmailSenderAdapter.class);

    @Override
    public void sendPasswordResetLink(String toEmail, String resetToken) {
        log.info("[DEMO] Lien de réinitialisation pour {} : /auth/reset-password?token={}", toEmail, resetToken);
    }
}
