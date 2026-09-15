package com.geolink.findme.unit.business.validation;
import com.geolink.findme.business.validation.EmailPolicy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EmailPolicyTest {

    @ParameterizedTest
    @CsvSource({
            "'  Jane.DOE@Example.COM ', jane.doe@example.com",
            "Test@Example.com, test@example.com",
            "'  a@b.co  ', a@b.co",
            "MIXED.Case+tag@Sub.Domain.COM, mixed.case+tag@sub.domain.com",
            "already.lower@example.com, already.lower@example.com"
    })
    void normalise_en_minuscules_et_sans_espaces(String raw, String expected) {
        assertThat(EmailPolicy.normalize(raw)).isEqualTo(expected);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "pas-un-email",
            "a@b",
            "@example.com",
            "jane@",
            "jane doe@example.com",
            "no-at-sign.com",
            "@leadingat.com",
            "trailing@",
            "no.dot@domain",
            "spaced user@example.com",
            "user@ex ample.com",
            "user@@example.com",
            "   "
    })
    void refuse_un_email_invalide(String raw) {
        assertThatThrownBy(() -> EmailPolicy.normalize(raw)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void refuse_null() {
        assertThatThrownBy(() -> EmailPolicy.normalize(null)).isInstanceOf(IllegalArgumentException.class);
    }
}
