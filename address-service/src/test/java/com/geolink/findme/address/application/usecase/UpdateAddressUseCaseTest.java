package com.geolink.findme.address.application.usecase;

import com.geolink.findme.address.domain.exception.AddressAccessDeniedException;
import com.geolink.findme.address.domain.exception.AddressNotFoundException;
import com.geolink.findme.address.domain.exception.DuplicateAddressException;
import com.geolink.findme.address.domain.model.Address;
import com.geolink.findme.address.domain.model.GeoPoint;
import com.geolink.findme.address.domain.port.AddressRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
class UpdateAddressUseCaseTest {

    @Mock
    private AddressRepositoryPort addressRepository;

    private UpdateAddressUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new UpdateAddressUseCase(addressRepository);
    }

    private Address existing(UUID owner) {
        return Address.createNew(owner, "Cameroun", "Douala", "Akwa", "Rue 1", "12", "0000",
                new GeoPoint(4.05, 9.7));
    }

    private UpdateAddressUseCase.Command command(UUID addressId, UUID requester) {
        return new UpdateAddressUseCase.Command(addressId, requester, "Cameroun", "Yaoundé", "Bastos",
                "Rue 5", "20", "1111", 3.87, 11.5);
    }

    @Test
    void met_a_jour_l_adresse_de_son_proprietaire() {
        UUID owner = UUID.randomUUID();
        Address address = existing(owner);
        when(addressRepository.findById(address.getId())).thenReturn(Optional.of(address));
        when(addressRepository.existsSameIdentity(eq(owner), anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(false);
        when(addressRepository.save(any(Address.class))).thenAnswer(inv -> inv.getArgument(0));

        Address result = useCase.execute(command(address.getId(), owner));

        assertThat(result.getCity()).isEqualTo("Yaoundé");
        assertThat(result.getDistrict()).isEqualTo("Bastos");
    }

    @Test
    void leve_not_found_quand_inexistante() {
        UUID id = UUID.randomUUID();
        when(addressRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(command(id, UUID.randomUUID())))
                .isInstanceOf(AddressNotFoundException.class);
    }

    @Test
    void leve_access_denied_pour_l_adresse_d_autrui() {
        Address address = existing(UUID.randomUUID());
        when(addressRepository.findById(address.getId())).thenReturn(Optional.of(address));

        assertThatThrownBy(() -> useCase.execute(command(address.getId(), UUID.randomUUID())))
                .isInstanceOf(AddressAccessDeniedException.class);

        verify(addressRepository, never()).save(any());
    }

    @Test
    void refuse_si_la_nouvelle_identite_est_deja_prise() {
        UUID owner = UUID.randomUUID();
        Address address = existing(owner);
        when(addressRepository.findById(address.getId())).thenReturn(Optional.of(address));
        when(addressRepository.existsSameIdentity(eq(owner), anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(true);

        assertThatThrownBy(() -> useCase.execute(command(address.getId(), owner)))
                .isInstanceOf(DuplicateAddressException.class);

        verify(addressRepository, never()).save(any());
    }
}