package com.geolink.findme.business.validation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EmailPolicyTest {

    @Test
    void normalise_en_minuscules_et_sans_espaces() {
        assertThat(EmailPolicy.normalize("  Jane.DOE@Example.COM ")).isEqualTo("jane.doe@example.com");
    }

    @ParameterizedTest
    @ValueSource(strings = {"pas-un-email", "a@b", "@example.com", "jane@", "jane doe@example.com"})
    void refuse_un_email_invalide(String raw) {
        assertThatThrownBy(() -> EmailPolicy.normalize(raw)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void refuse_null() {
        assertThatThrownBy(() -> EmailPolicy.normalize(null)).isInstanceOf(IllegalArgumentException.class);
    }
}
