package com.geolink.findme.integration.data.repository;

import com.geolink.findme.business.model.AccountStatus;
import com.geolink.findme.business.model.AddressStatus;
import com.geolink.findme.business.model.Role;
import com.geolink.findme.data.entity.Address;
import com.geolink.findme.data.entity.User;
import com.geolink.findme.data.repository.AddressRepository;
import com.geolink.findme.data.repository.UserRepository;
import com.geolink.findme.integration.AbstractRepositoryIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Vérifie {@link AddressRepository} face à une vraie base PostgreSQL : requêtes {@code @Query}
 * de pagination, contraintes d'unicité, et le trigger SQL {@code trg_enforce_address_quota}
 * (filet de sécurité qui ne peut pas être vérifié par un test unitaire avec des mocks).
 */
class AddressRepositoryIT extends AbstractRepositoryIntegrationTest {

    @Autowired
    private AddressRepository addressRepository;
    @Autowired
    private UserRepository userRepository;

    private User persistedUser(String email) {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail(email);
        user.setPasswordHash("hashed");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setRole(Role.USER);
        user.setStatus(AccountStatus.ACTIVE);
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());
        return userRepository.saveAndFlush(user);
    }

    private Address newAddress(UUID userId, String country, String city, String district, String street,
                                String houseNumber, String addressCode) {
        Instant now = Instant.now();
        Address address = new Address();
        address.setId(UUID.randomUUID());
        address.setUserId(userId);
        address.setCountry(country);
        address.setCity(city);
        address.setDistrict(district);
        address.setStreet(street);
        address.setHouseNumber(houseNumber);
        address.setLabel("Domicile");
        address.setStatus(AddressStatus.PENDING);
        address.setAddressCode(addressCode);
        address.setCreatedAt(now);
        address.setUpdatedAt(now);
        return address;
    }

    @Test
    void countByUserId_compte_les_adresses_de_demonstration() {
        // Aya (33333333-...) possède 2 adresses seedées par V8.
        UUID aya = UUID.fromString("33333333-3333-3333-3333-333333333333");

        assertThat(addressRepository.countByUserId(aya)).isEqualTo(2L);
    }

    @Test
    void existsByAddressCode_vrai_apres_sauvegarde() {
        User user = persistedUser("owner1@example.com");
        addressRepository.saveAndFlush(newAddress(user.getId(), "Cameroun", "Douala", "Akwa", "Rue 1", "12", "FM-TEST-0001"));

        assertThat(addressRepository.existsByAddressCode("FM-TEST-0001")).isTrue();
        assertThat(addressRepository.existsByAddressCode("FM-INCONNU-9999")).isFalse();
    }

    @Test
    void existsByUserIdEtIdentite_est_insensible_a_la_casse() {
        User user = persistedUser("owner2@example.com");
        addressRepository.saveAndFlush(newAddress(user.getId(), "Cameroun", "Douala", "Akwa", "Rue 1", "12", "FM-TEST-0002"));

        boolean exists = addressRepository
                .existsByUserIdAndCountryIgnoreCaseAndCityIgnoreCaseAndDistrictIgnoreCaseAndStreetIgnoreCaseAndHouseNumberIgnoreCase(
                        user.getId(), "cameroun", "DOUALA", "akwa", "RUE 1", "12");

        assertThat(exists).isTrue();
    }

    @Test
    void findPageByUser_filtre_par_ville() {
        UUID aya = UUID.fromString("33333333-3333-3333-3333-333333333333");

        var page = addressRepository.findPageByUser(aya, null, "Douala", null, PageRequest.of(0, 10));

        assertThat(page.getContent()).extracting(Address::getCity).containsOnly("Douala");
    }

    @Test
    void findPageByUser_ne_retourne_que_les_adresses_du_proprietaire() {
        UUID aya = UUID.fromString("33333333-3333-3333-3333-333333333333");
        UUID jean = UUID.fromString("44444444-4444-4444-4444-444444444444");

        var page = addressRepository.findPageByUser(jean, null, null, null, PageRequest.of(0, 10));

        assertThat(page.getContent()).extracting(Address::getUserId).containsOnly(jean).doesNotContain(aya);
    }

    @Test
    void findPageForAdmin_filtre_par_pays_toutes_utilisateurs_confondus() {
        var page = addressRepository.findPageForAdmin("Cameroun", null, null, PageRequest.of(0, 50));

        assertThat(page.getContent()).extracting(Address::getCountry).containsOnly("Cameroun");
        assertThat(page.getTotalElements()).isGreaterThanOrEqualTo(3);
    }

    @Test
    void la_contrainte_unique_sur_l_identite_de_l_adresse_est_appliquee() {
        User user = persistedUser("owner3@example.com");
        addressRepository.saveAndFlush(newAddress(user.getId(), "Cameroun", "Douala", "Akwa", "Rue 1", "12", "FM-TEST-0003"));

        assertThatThrownBy(() -> addressRepository.saveAndFlush(
                newAddress(user.getId(), "Cameroun", "Douala", "Akwa", "Rue 1", "12", "FM-TEST-0004")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void la_contrainte_unique_sur_le_code_d_adresse_est_appliquee() {
        User user = persistedUser("owner4@example.com");
        addressRepository.saveAndFlush(newAddress(user.getId(), "Cameroun", "Douala", "Akwa", "Rue 1", "12", "FM-TEST-0005"));

        assertThatThrownBy(() -> addressRepository.saveAndFlush(
                newAddress(user.getId(), "Cameroun", "Douala", "Bonapriso", "Rue 2", "13", "FM-TEST-0005")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void le_trigger_sql_refuse_une_cinquieme_adresse_pour_le_meme_utilisateur() {
        User user = persistedUser("owner5@example.com");
        for (int i = 1; i <= 4; i++) {
            addressRepository.saveAndFlush(newAddress(user.getId(), "Cameroun", "Douala", "Quartier" + i, "Rue " + i,
                    String.valueOf(i), "FM-TEST-QUOTA-" + i));
        }

        assertThatThrownBy(() -> addressRepository.saveAndFlush(
                newAddress(user.getId(), "Cameroun", "Douala", "Quartier5", "Rue 5", "5", "FM-TEST-QUOTA-5")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
