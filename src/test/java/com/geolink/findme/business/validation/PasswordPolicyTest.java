package com.geolink.findme.business.validation;

import com.geolink.findme.business.exception.WeakPasswordException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PasswordPolicyTest {

    @Test
    void accepte_un_mot_de_passe_conforme() {
        assertThatCode(() -> PasswordPolicy.validate("Password1")).doesNotThrowAnyException();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "Short1",     // trop court (< 8)
            "password1",  // pas de majuscule
            "PASSWORD",   // pas de chiffre
            "Passwordx"   // pas de chiffre
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
