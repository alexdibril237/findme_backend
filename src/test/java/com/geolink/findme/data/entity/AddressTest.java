package com.geolink.findme.data.entity;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AddressTest {

    private Address sample(UUID owner) {
        Address address = new Address();
        address.setId(UUID.randomUUID());
        address.setUserId(owner);
        address.setCountry("Cameroun");
        address.setCity("Douala");
        address.setDistrict("Akwa");
        address.setStreet("Rue 1");
        address.setHouseNumber("12");
        return address;
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
}
