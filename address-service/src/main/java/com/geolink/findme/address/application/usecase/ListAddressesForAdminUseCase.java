package com.geolink.findme.address.application.usecase;

import com.geolink.findme.address.domain.model.Address;
import com.geolink.findme.address.domain.port.AddressRepositoryPort;
import com.geolink.findme.address.domain.port.AddressRepositoryPort.AddressFilters;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

/** Consommé par admin-service via l'endpoint interne (vue transverse, tous utilisateurs). */
public class ListAddressesForAdminUseCase {

    private final AddressRepositoryPort addressRepository;

    public ListAddressesForAdminUseCase(AddressRepositoryPort addressRepository) {
        this.addressRepository = addressRepository;
    }

    public record Query(Pageable pageable, AddressFilters filters) {
    }

    @Transactional(readOnly = true)
    public Page<Address> execute(Query query) {
        return addressRepository.findPageForAdmin(query.pageable(), query.filters());
    }
}
