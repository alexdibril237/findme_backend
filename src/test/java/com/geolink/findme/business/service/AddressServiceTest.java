package com.geolink.findme.business.service;

import com.geolink.findme.business.exception.AddressAccessDeniedException;
import com.geolink.findme.business.exception.AddressNotFoundException;
import com.geolink.findme.business.exception.AddressQuotaExceededException;
import com.geolink.findme.business.exception.DuplicateAddressException;
import com.geolink.findme.business.validation.AddressQuotaPolicy;
import com.geolink.findme.data.entity.Address;
import com.geolink.findme.data.repository.AddressRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AddressServiceTest {

    @Mock
    private AddressRepository addressRepository;
    @Mock
    private PhotoStorageService photoStorageService;
    @Mock
    private QrCodeService qrCodeService;

    private AddressService addressService;
    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        addressService = new AddressServiceImpl(addressRepository, photoStorageService, qrCodeService);
    }

    private Address existing(UUID owner) {
        Address address = new Address();
        address.setId(UUID.randomUUID());
        address.setUserId(owner);
        address.setCountry("Cameroun");
        address.setCity("Douala");
        address.setDistrict("Akwa");
        address.setStreet("Rue 1");
        address.setHouseNumber("12");
        address.setPostalCode("0000");
        address.setLatitude(4.05);
        address.setLongitude(9.7);
        address.setCreatedAt(Instant.now());
        address.setUpdatedAt(Instant.now());
        return address;
    }

    // --- createAddress ---

    @Test
    void cree_une_adresse_sous_le_quota() {
        when(addressRepository.countByUserId(userId)).thenReturn(2L);
        when(addressRepository
                .existsByUserIdAndCountryIgnoreCaseAndCityIgnoreCaseAndDistrictIgnoreCaseAndStreetIgnoreCaseAndHouseNumberIgnoreCase(
                        eq(userId), anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(false);
        when(addressRepository.save(any(Address.class))).thenAnswer(inv -> inv.getArgument(0));

        Address created = addressService.createAddress(userId, "Domicile", "Cameroun", "Douala", "Akwa", "Rue 1", "12",
                "0000", 4.05, 9.7, "CM");

        assertThat(created.getCity()).isEqualTo("Douala");
        assertThat(created.getUserId()).isEqualTo(userId);
        verify(addressRepository).save(any(Address.class));
    }

    @Test
    void refuse_quand_quota_atteint() {
        when(addressRepository.countByUserId(userId)).thenReturn((long) AddressQuotaPolicy.MAX_ADDRESSES_PER_USER);

        assertThatThrownBy(() -> addressService.createAddress(userId, "Domicile", "Cameroun", "Douala", "Akwa", "Rue 1",
                "12", "0000", 4.05, 9.7, "CM"))
                .isInstanceOf(AddressQuotaExceededException.class);

        verify(addressRepository, never()).save(any());
    }

    @Test
    void refuse_une_adresse_en_double() {
        when(addressRepository.countByUserId(userId)).thenReturn(1L);
        when(addressRepository
                .existsByUserIdAndCountryIgnoreCaseAndCityIgnoreCaseAndDistrictIgnoreCaseAndStreetIgnoreCaseAndHouseNumberIgnoreCase(
                        eq(userId), anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(true);

        assertThatThrownBy(() -> addressService.createAddress(userId, "Domicile", "Cameroun", "Douala", "Akwa", "Rue 1",
                "12", "0000", 4.05, 9.7, "CM"))
                .isInstanceOf(DuplicateAddressException.class);

        verify(addressRepository, never()).save(any());
    }

    // --- getAddress ---

    @Test
    void retourne_l_adresse_de_son_proprietaire() {
        UUID owner = UUID.randomUUID();
        Address address = existing(owner);
        when(addressRepository.findById(address.getId())).thenReturn(Optional.of(address));

        Address result = addressService.getAddress(address.getId(), owner);

        assertThat(result).isSameAs(address);
    }

    @Test
    void get_leve_not_found_quand_adresse_inexistante() {
        UUID id = UUID.randomUUID();
        when(addressRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> addressService.getAddress(id, UUID.randomUUID()))
                .isInstanceOf(AddressNotFoundException.class);
    }

    @Test
    void get_leve_access_denied_quand_adresse_d_autrui() {
        Address address = existing(UUID.randomUUID());
        when(addressRepository.findById(address.getId())).thenReturn(Optional.of(address));

        assertThatThrownBy(() -> addressService.getAddress(address.getId(), UUID.randomUUID()))
                .isInstanceOf(AddressAccessDeniedException.class);
    }

    // --- updateAddress ---

    @Test
    void met_a_jour_l_adresse_de_son_proprietaire() {
        UUID owner = UUID.randomUUID();
        Address address = existing(owner);
        when(addressRepository.findById(address.getId())).thenReturn(Optional.of(address));
        when(addressRepository
                .existsByUserIdAndCountryIgnoreCaseAndCityIgnoreCaseAndDistrictIgnoreCaseAndStreetIgnoreCaseAndHouseNumberIgnoreCase(
                        eq(owner), anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(false);
        when(addressRepository.save(any(Address.class))).thenAnswer(inv -> inv.getArgument(0));

        Address result = addressService.updateAddress(address.getId(), owner, "Bureau", "Cameroun", "Yaoundé", "Bastos",
                "Rue 5", "20", "1111", 3.87, 11.5, "CM");

        assertThat(result.getCity()).isEqualTo("Yaoundé");
        assertThat(result.getDistrict()).isEqualTo("Bastos");
    }

    @Test
    void update_leve_not_found_quand_inexistante() {
        UUID id = UUID.randomUUID();
        when(addressRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> addressService.updateAddress(id, UUID.randomUUID(), "Bureau", "Cameroun", "Yaoundé",
                "Bastos", "Rue 5", "20", "1111", 3.87, 11.5, "CM"))
                .isInstanceOf(AddressNotFoundException.class);
    }

    @Test
    void update_leve_access_denied_pour_l_adresse_d_autrui() {
        Address address = existing(UUID.randomUUID());
        when(addressRepository.findById(address.getId())).thenReturn(Optional.of(address));

        assertThatThrownBy(() -> addressService.updateAddress(address.getId(), UUID.randomUUID(), "Bureau", "Cameroun",
                "Yaoundé", "Bastos", "Rue 5", "20", "1111", 3.87, 11.5, "CM"))
                .isInstanceOf(AddressAccessDeniedException.class);

        verify(addressRepository, never()).save(any());
    }

    @Test
    void refuse_si_la_nouvelle_identite_est_deja_prise() {
        UUID owner = UUID.randomUUID();
        Address address = existing(owner);
        when(addressRepository.findById(address.getId())).thenReturn(Optional.of(address));
        when(addressRepository
                .existsByUserIdAndCountryIgnoreCaseAndCityIgnoreCaseAndDistrictIgnoreCaseAndStreetIgnoreCaseAndHouseNumberIgnoreCase(
                        eq(owner), anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(true);

        assertThatThrownBy(() -> addressService.updateAddress(address.getId(), owner, "Bureau", "Cameroun", "Yaoundé",
                "Bastos", "Rue 5", "20", "1111", 3.87, 11.5, "CM"))
                .isInstanceOf(DuplicateAddressException.class);

        verify(addressRepository, never()).save(any());
    }

    // --- deleteAddress ---

    @Test
    void supprime_l_adresse_de_son_proprietaire() {
        UUID owner = UUID.randomUUID();
        Address address = existing(owner);
        when(addressRepository.findById(address.getId())).thenReturn(Optional.of(address));

        addressService.deleteAddress(address.getId(), owner);

        verify(addressRepository).deleteById(address.getId());
    }

    @Test
    void delete_leve_not_found_quand_inexistante() {
        UUID id = UUID.randomUUID();
        when(addressRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> addressService.deleteAddress(id, UUID.randomUUID()))
                .isInstanceOf(AddressNotFoundException.class);

        verify(addressRepository, never()).deleteById(id);
    }

    @Test
    void delete_leve_access_denied_pour_l_adresse_d_autrui() {
        Address address = existing(UUID.randomUUID());
        when(addressRepository.findById(address.getId())).thenReturn(Optional.of(address));

        assertThatThrownBy(() -> addressService.deleteAddress(address.getId(), UUID.randomUUID()))
                .isInstanceOf(AddressAccessDeniedException.class);

        verify(addressRepository, never()).deleteById(address.getId());
    }
}
