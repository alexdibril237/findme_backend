package com.geolink.findme.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

/** Envoi de l'email de réinitialisation via l'API HTTP de Resend (https://resend.com). */
@Component
public class ResendEmailService implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(ResendEmailService.class);
    private static final String RESEND_ENDPOINT = "https://api.resend.com/emails";

    private final String apiKey;
    private final String from;
    private final RestClient restClient;

    public ResendEmailService(@Value("${resend.api-key:}") String apiKey,
                               @Value("${resend.from}") String from) {
        this.apiKey = apiKey;
        this.from = from;
        this.restClient = RestClient.create();
    }

    @Override
    public void sendPasswordResetCode(String toEmail, String code) {
        if (apiKey == null || apiKey.isBlank()) {
            // Pas de clé configurée (dev local sans RESEND_API_KEY) : on journalise
            // au lieu d'échouer, pour ne pas bloquer forgotPassword() en développement.
            log.warn("[EMAIL] RESEND_API_KEY absente — code de réinitialisation pour {} : {}", toEmail, code);
            return;
        }
        try {
            restClient.post()
                    .uri(RESEND_ENDPOINT)
                    .header("Authorization", "Bearer " + apiKey)
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .body(Map.of(
                            "from", from,
                            "to", List.of(toEmail),
                            "subject", "Votre code de réinitialisation findMe",
                            "html", buildHtml(code)
                    ))
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            // Ne jamais faire échouer forgotPassword() à cause d'un souci d'envoi d'email
            // (le contrat ne révèle jamais si l'email existe, cf. §2.1 cahier des charges) :
            // on journalise l'erreur et le code reste consultable côté serveur pour le support.
            log.error("[EMAIL] Échec de l'envoi du code de réinitialisation à {} (code : {}) : {}", toEmail, code, e.getMessage());
        }
    }

    private String buildHtml(String code) {
        return """
                <div style="font-family:Arial,sans-serif;max-width:480px;margin:0 auto;padding:24px">
                  <h2 style="color:#185FA5">findMe</h2>
                  <p>Voici votre code de réinitialisation de mot de passe :</p>
                  <p style="font-size:32px;font-weight:700;letter-spacing:6px;color:#0C447C">%s</p>
                  <p style="color:#666;font-size:13px">Ce code expire dans 30 minutes. Si vous n'êtes pas à l'origine de cette demande, ignorez cet email.</p>
                </div>
                """.formatted(code);
    }
}
