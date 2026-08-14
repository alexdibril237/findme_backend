package com.geolink.findme.address.application.usecase;

import com.geolink.findme.address.domain.exception.AddressAccessDeniedException;
import com.geolink.findme.address.domain.exception.AddressNotFoundException;
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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeleteAddressUseCaseTest {

    @Mock
    private AddressRepositoryPort addressRepository;

    private DeleteAddressUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new DeleteAddressUseCase(addressRepository);
    }

    private Address existing(UUID owner) {
        return Address.createNew(owner, "Cameroun", "Douala", "Akwa", "Rue 1", "12", "0000",
                new GeoPoint(4.05, 9.7));
    }

    @Test
    void supprime_l_adresse_de_son_proprietaire() {
        UUID owner = UUID.randomUUID();
        Address address = existing(owner);
        when(addressRepository.findById(address.getId())).thenReturn(Optional.of(address));

        useCase.execute(address.getId(), owner);

        verify(addressRepository).deleteById(address.getId());
    }

    @Test
    void leve_not_found_quand_inexistante() {
        UUID id = UUID.randomUUID();
        when(addressRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(id, UUID.randomUUID()))
                .isInstanceOf(AddressNotFoundException.class);

        verify(addressRepository, never()).deleteById(id);
    }

    @Test
    void leve_access_denied_pour_l_adresse_d_autrui() {
        Address address = existing(UUID.randomUUID());
        when(addressRepository.findById(address.getId())).thenReturn(Optional.of(address));

        assertThatThrownBy(() -> useCase.execute(address.getId(), UUID.randomUUID()))
                .isInstanceOf(AddressAccessDeniedException.class);

        verify(addressRepository, never()).deleteById(address.getId());
    }
}