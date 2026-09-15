package com.geolink.findme.unit.security;
import com.geolink.findme.security.ResendEmailService;
import com.geolink.findme.security.EmailService;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * Sans clé API configurée, {@link ResendEmailService} ne doit jamais tenter d'appel réseau
 * (cf. commentaire du code : ne pas bloquer forgotPassword() en développement local).
 */
class ResendEmailServiceTest {

    @Test
    void n_appelle_pas_l_api_et_ne_leve_rien_si_la_cle_est_absente() {
        EmailService service = new ResendEmailService(null, "noreply@findme.africa");

        assertThatCode(() -> service.sendPasswordResetCode("jane@doe.io", "123456")).doesNotThrowAnyException();
    }

    @Test
    void n_appelle_pas_l_api_et_ne_leve_rien_si_la_cle_est_vide() {
        EmailService service = new ResendEmailService("", "noreply@findme.africa");

        assertThatCode(() -> service.sendPasswordResetCode("jane@doe.io", "123456")).doesNotThrowAnyException();
    }

    @Test
    void n_appelle_pas_l_api_et_ne_leve_rien_si_la_cle_ne_contient_que_des_espaces() {
        EmailService service = new ResendEmailService("   ", "noreply@findme.africa");

        assertThatCode(() -> service.sendPasswordResetCode("jane@doe.io", "123456")).doesNotThrowAnyException();
    }
}
