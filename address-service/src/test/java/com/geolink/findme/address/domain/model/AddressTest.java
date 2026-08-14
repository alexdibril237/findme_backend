package com.geolink.findme.address.domain.model;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AddressTest {

    private Address sample(UUID owner) {
        return Address.createNew(owner, "Cameroun", "Douala", "Akwa", "Rue 1", "12", "0000",
                new GeoPoint(4.05, 9.7));
    }

    @Test
    void belongsTo_vrai_pour_le_proprietaire() {
        UUID owner = UUID.randomUUID();
        assertThat(sample(owner).belongsTo(owner)).isTrue();
    }

    @Test
    void belongsTo_faux_pour_un_autre_utilisateur() {
        assertThat(sample(UUID.randomUUID()).belongsTo(UUID.randomUUID())).isFalse();
    }

    @Test
    void sameIdentityAs_insensible_a_la_casse() {
        Address address = sample(UUID.randomUUID());
        assertThat(address.sameIdentityAs("cameroun", "DOUALA", "akwa", "rue 1", "12")).isTrue();
        assertThat(address.sameIdentityAs("Cameroun", "Yaoundé", "Akwa", "Rue 1", "12")).isFalse();
    }

    @Test
    void updateDetails_modifie_les_champs_et_horodate() {
        Address address = sample(UUID.randomUUID());
        address.updateDetails("Cameroun", "Yaoundé", "Bastos", "Rue 5", "20", "1111", new GeoPoint(3.87, 11.5));
        assertThat(address.getCity()).isEqualTo("Yaoundé");
        assertThat(address.getDistrict()).isEqualTo("Bastos");
        assertThat(address.getHouseNumber()).isEqualTo("20");
    }

    @Test
    void attachPhoto_enregistre_l_url() {
        Address address = sample(UUID.randomUUID());
        address.attachPhoto("http://localhost/files/photo.jpg");
        assertThat(address.getPhotoUrl()).isEqualTo("http://localhost/files/photo.jpg");
    }
}