package com.geolink.findme.auth.domain.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EmailTest {

    @Test
    void normalise_en_minuscules_et_sans_espaces() {
        assertThat(new Email("  Jane.DOE@Example.COM ").value()).isEqualTo("jane.doe@example.com");
    }

    @ParameterizedTest
    @ValueSource(strings = {"pas-un-email", "a@b", "@example.com", "jane@", "jane doe@example.com"})
    void refuse_un_email_invalide(String raw) {
        assertThatThrownBy(() -> new Email(raw)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void refuse_null() {
        assertThatThrownBy(() -> new Email(null)).isInstanceOf(IllegalArgumentException.class);
    }
}