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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetAddressUseCaseTest {

    @Mock
    private AddressRepositoryPort addressRepository;

    private GetAddressUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetAddressUseCase(addressRepository);
    }

    private Address addressOwnedBy(UUID ownerId) {
        return Address.createNew(ownerId, "Cameroun", "Douala", "Akwa", "Rue 1", "12", "0000",
                new GeoPoint(4.05, 9.7));
    }

    @Test
    void retourne_l_adresse_de_son_proprietaire() {
        UUID owner = UUID.randomUUID();
        Address address = addressOwnedBy(owner);
        when(addressRepository.findById(address.getId())).thenReturn(Optional.of(address));

        Address result = useCase.execute(address.getId(), owner);

        assertThat(result).isSameAs(address);
    }

    @Test
    void leve_not_found_quand_adresse_inexistante() {
        UUID id = UUID.randomUUID();
        when(addressRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(id, UUID.randomUUID()))
                .isInstanceOf(AddressNotFoundException.class);
    }

    @Test
    void leve_access_denied_quand_adresse_d_autrui() {
        Address address = addressOwnedBy(UUID.randomUUID());
        when(addressRepository.findById(address.getId())).thenReturn(Optional.of(address));

        assertThatThrownBy(() -> useCase.execute(address.getId(), UUID.randomUUID()))
                .isInstanceOf(AddressAccessDeniedException.class);
    }
}