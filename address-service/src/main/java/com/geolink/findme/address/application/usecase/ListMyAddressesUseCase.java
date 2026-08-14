package com.geolink.findme.address.application.usecase;

import com.geolink.findme.address.domain.model.Address;
import com.geolink.findme.address.domain.port.AddressRepositoryPort;
import com.geolink.findme.address.domain.port.AddressRepositoryPort.AddressFilters;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public class ListMyAddressesUseCase {

    private final AddressRepositoryPort addressRepository;

    public ListMyAddressesUseCase(AddressRepositoryPort addressRepository) {
        this.addressRepository = addressRepository;
    }

    public record Query(UUID userId, Pageable pageable, AddressFilters filters) {
    }

    @Transactional(readOnly = true)
    public Page<Address> execute(Query query) {
        return addressRepository.findPageByUser(query.userId(), query.pageable(), query.filters());
    }
}
