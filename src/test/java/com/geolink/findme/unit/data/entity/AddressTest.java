package com.geolink.findme.unit.data.entity;
import com.geolink.findme.data.entity.Address;

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
    void belongsTo_vrai_pour_un_uuid_egal_mais_pas_le_meme_objet() {
        UUID owner = UUID.randomUUID();
        UUID sameValue = UUID.fromString(owner.toString());
        assertThat(sample(owner).belongsTo(sameValue)).isTrue();
    }

    @Test
    void belongsTo_faux_pour_un_autre_utilisateur() {
        assertThat(sample(UUID.randomUUID()).belongsTo(UUID.randomUUID())).isFalse();
    }

    @Test
    void sameIdentityAs_insensible_a_la_casse() {
        Address address = sample(UUID.randomUUID());
        assertThat(address.sameIdentityAs("cameroun", "DOUALA", "akwa", "rue 1", "12")).isTrue();
    }

    @Test
    void sameIdentityAs_faux_si_le_pays_differe() {
        Address address = sample(UUID.randomUUID());
        assertThat(address.sameIdentityAs("Nigeria", "Douala", "Akwa", "Rue 1", "12")).isFalse();
    }

    @Test
    void sameIdentityAs_faux_si_la_ville_differe() {
        Address address = sample(UUID.randomUUID());
        assertThat(address.sameIdentityAs("Cameroun", "Yaoundé", "Akwa", "Rue 1", "12")).isFalse();
    }

    @Test
    void sameIdentityAs_faux_si_le_quartier_differe() {
        Address address = sample(UUID.randomUUID());
        assertThat(address.sameIdentityAs("Cameroun", "Douala", "Bonapriso", "Rue 1", "12")).isFalse();
    }

    @Test
    void sameIdentityAs_faux_si_la_rue_differe() {
        Address address = sample(UUID.randomUUID());
        assertThat(address.sameIdentityAs("Cameroun", "Douala", "Akwa", "Rue 2", "12")).isFalse();
    }

    @Test
    void sameIdentityAs_faux_si_le_numero_differe() {
        Address address = sample(UUID.randomUUID());
        assertThat(address.sameIdentityAs("Cameroun", "Douala", "Akwa", "Rue 1", "13")).isFalse();
    }

    @Test
    void getters_et_setters_transportent_les_valeurs() {
        Address address = new Address();
        UUID id = UUID.randomUUID();
        address.setId(id);
        address.setPostalCode("1000");
        address.setLatitude(4.05);
        address.setLongitude(9.7);
        address.setPhotoUrl("http://files/photo.jpg");
        address.setLabel("Domicile");

        assertThat(address.getId()).isEqualTo(id);
        assertThat(address.getPostalCode()).isEqualTo("1000");
        assertThat(address.getLatitude()).isEqualTo(4.05);
        assertThat(address.getLongitude()).isEqualTo(9.7);
        assertThat(address.getPhotoUrl()).isEqualTo("http://files/photo.jpg");
        assertThat(address.getLabel()).isEqualTo("Domicile");
    }
}
