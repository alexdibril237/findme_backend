package com.geolink.findme.unit.business.validation;
import com.geolink.findme.business.validation.GeoPointPolicy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GeoPointPolicyTest {

    @Test
    void accepte_des_coordonnees_nulles() {
        assertThatCode(() -> GeoPointPolicy.validate(null, null)).doesNotThrowAnyException();
    }

    @Test
    void accepte_une_latitude_seule() {
        assertThatCode(() -> GeoPointPolicy.validate(45.0, null)).doesNotThrowAnyException();
    }

    @Test
    void accepte_une_longitude_seule() {
        assertThatCode(() -> GeoPointPolicy.validate(null, 45.0)).doesNotThrowAnyException();
    }

    @ParameterizedTest
    @CsvSource({
            "0, 0",
            "90, 180",
            "-90, -180",
            "45.5, -73.6",
            "89.999, 179.999",
            "-89.999, -179.999"
    })
    void accepte_les_coordonnees_dans_les_bornes(double latitude, double longitude) {
        assertThatCode(() -> GeoPointPolicy.validate(latitude, longitude)).doesNotThrowAnyException();
    }

    @ParameterizedTest
    @CsvSource({
            "90.1, 0",
            "-90.1, 0",
            "1000, 0"
    })
    void refuse_une_latitude_hors_bornes(double latitude, double longitude) {
        assertThatThrownBy(() -> GeoPointPolicy.validate(latitude, longitude))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @CsvSource({
            "0, 180.1",
            "0, -180.1",
            "0, 1000"
    })
    void refuse_une_longitude_hors_bornes(double latitude, double longitude) {
        assertThatThrownBy(() -> GeoPointPolicy.validate(latitude, longitude))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void refuse_quand_les_deux_coordonnees_sont_hors_bornes() {
        assertThatThrownBy(() -> GeoPointPolicy.validate(200.0, 200.0))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
