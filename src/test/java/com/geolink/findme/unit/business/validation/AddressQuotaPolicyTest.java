package com.geolink.findme.unit.business.validation;
import com.geolink.findme.business.validation.AddressQuotaPolicy;

import com.geolink.findme.business.exception.AddressQuotaExceededException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AddressQuotaPolicyTest {

    @ParameterizedTest
    @ValueSource(longs = {0, 1, 2, 3})
    void autorise_la_creation_sous_le_quota(long currentCount) {
        assertThatCode(() -> AddressQuotaPolicy.ensureCanCreate(currentCount)).doesNotThrowAnyException();
    }

    @ParameterizedTest
    @ValueSource(longs = {4, 5, 10})
    void refuse_la_creation_au_dela_du_quota(long currentCount) {
        assertThatThrownBy(() -> AddressQuotaPolicy.ensureCanCreate(currentCount))
                .isInstanceOf(AddressQuotaExceededException.class);
    }

    @Test
    void expose_la_limite_a_quatre() {
        assertThatCode(() -> AddressQuotaPolicy.ensureCanCreate(AddressQuotaPolicy.MAX_ADDRESSES_PER_USER - 1))
                .doesNotThrowAnyException();
        assertThatThrownBy(() -> AddressQuotaPolicy.ensureCanCreate(AddressQuotaPolicy.MAX_ADDRESSES_PER_USER))
                .isInstanceOf(AddressQuotaExceededException.class);
    }
}
