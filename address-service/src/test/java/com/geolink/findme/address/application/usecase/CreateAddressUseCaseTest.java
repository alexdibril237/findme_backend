package com.geolink.findme.address.application.usecase;

import com.geolink.findme.address.domain.exception.AddressQuotaExceededException;
import com.geolink.findme.address.domain.exception.DuplicateAddressException;
import com.geolink.findme.address.domain.model.Address;
import com.geolink.findme.address.domain.model.AddressQuotaPolicy;
import com.geolink.findme.address.domain.port.AddressRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
class CreateAddressUseCaseTest {

    @Mock
    private AddressRepositoryPort addressRepository;

    private CreateAddressUseCase useCase;
    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        useCase = new CreateAddressUseCase(addressRepository);
    }

    private CreateAddressUseCase.Command command() {
        return new CreateAddressUseCase.Command(userId, "Cameroun", "Douala", "Akwa", "Rue 1", "12",
                "0000", 4.05, 9.7);
    }

    @Test
    void cree_une_adresse_sous_le_quota() {
        when(addressRepository.countByUser(userId)).thenReturn(2L);
        when(addressRepository.existsSameIdentity(eq(userId), anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(false);
        when(addressRepository.save(any(Address.class))).thenAnswer(inv -> inv.getArgument(0));

        Address created = useCase.execute(command());

        assertThat(created.getCity()).isEqualTo("Douala");
        assertThat(created.getUserId()).isEqualTo(userId);
        verify(addressRepository).save(any(Address.class));
    }

    @Test
    void refuse_quand_quota_atteint() {
        when(addressRepository.countByUser(userId)).thenReturn((long) AddressQuotaPolicy.MAX_ADDRESSES_PER_USER);

        assertThatThrownBy(() -> useCase.execute(command()))
                .isInstanceOf(AddressQuotaExceededException.class);

        verify(addressRepository, never()).save(any());
    }

    @Test
    void refuse_une_adresse_en_double() {
        when(addressRepository.countByUser(userId)).thenReturn(1L);
        when(addressRepository.existsSameIdentity(eq(userId), anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(true);

        assertThatThrownBy(() -> useCase.execute(command()))
                .isInstanceOf(DuplicateAddressException.class);

        verify(addressRepository, never()).save(any());
    }
}