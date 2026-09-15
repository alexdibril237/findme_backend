package com.geolink.findme.unit.business.validation;
import com.geolink.findme.business.validation.PasswordPolicy;

import com.geolink.findme.business.exception.WeakPasswordException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PasswordPolicyTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "Password1",       // cas nominal
            "Passw0rd",        // exactement 8 caractères (borne basse acceptée)
            "P4ssword",        // 8 caractères
            "AAAAAAA1",        // que des majuscules + un chiffre
            "aaaaaaA1",        // une seule majuscule suffit
            "Longue-Phrase-De-Passe-99"
    })
    void accepte_les_mots_de_passe_conformes(String valid) {
        assertThatCode(() -> PasswordPolicy.validate(valid)).doesNotThrowAnyException();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "Short1",                 // trop court (< 8) malgré majuscule + chiffre
            "Passw0r",                // 7 caractères, juste sous la borne
            "password1",              // pas de majuscule
            "PASSWORD",               // pas de chiffre
            "Passwordx",              // pas de chiffre
            "12345678",               // que des chiffres, pas de majuscule
            "ALLUPPERCASE",           // pas de chiffre
            "toolongbutnouppercase1", // pas de majuscule malgré la longueur
            "NOLOWERNODIGIT",         // pas de chiffre
            "        ",               // espaces uniquement
            "",                       // chaîne vide
    })
    void refuse_les_mots_de_passe_non_conformes(String weak) {
        assertThatThrownBy(() -> PasswordPolicy.validate(weak))
                .isInstanceOf(WeakPasswordException.class);
    }

    @Test
    void refuse_un_mot_de_passe_null() {
        assertThatThrownBy(() -> PasswordPolicy.validate(null))
                .isInstanceOf(WeakPasswordException.class);
    }
}
