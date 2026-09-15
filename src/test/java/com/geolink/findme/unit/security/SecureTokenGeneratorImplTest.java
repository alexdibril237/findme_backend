package com.geolink.findme.unit.security;
import com.geolink.findme.security.SecureTokenGeneratorImpl;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SecureTokenGeneratorImplTest {

    private final SecureTokenGeneratorImpl generator = new SecureTokenGeneratorImpl();

    @Test
    void generateOpaqueToken_retourne_une_valeur_non_vide() {
        assertThat(generator.generateOpaqueToken()).isNotBlank();
    }

    @Test
    void generateOpaqueToken_produit_des_valeurs_differentes_a_chaque_appel() {
        String first = generator.generateOpaqueToken();
        String second = generator.generateOpaqueToken();

        assertThat(first).isNotEqualTo(second);
    }

    @Test
    void generateOpaqueToken_ne_contient_pas_de_caracteres_url_unsafe() {
        String token = generator.generateOpaqueToken();

        assertThat(token).matches("^[A-Za-z0-9_-]+$");
    }

    @Test
    void generateNumericCode_est_toujours_sur_six_chiffres() {
        for (int i = 0; i < 200; i++) {
            assertThat(generator.generateNumericCode()).matches("^\\d{6}$");
        }
    }

    @Test
    void hash_est_deterministe_pour_la_meme_entree() {
        assertThat(generator.hash("raw-token")).isEqualTo(generator.hash("raw-token"));
    }

    @Test
    void hash_differe_selon_l_entree() {
        assertThat(generator.hash("raw-token-a")).isNotEqualTo(generator.hash("raw-token-b"));
    }

    @Test
    void hash_produit_soixante_quatre_caracteres_hexadecimaux() {
        String hashed = generator.hash("raw-token");

        assertThat(hashed).hasSize(64).matches("^[0-9a-f]{64}$");
    }
}
