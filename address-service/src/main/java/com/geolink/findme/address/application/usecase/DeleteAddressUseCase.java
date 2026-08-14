package com.geolink.findme.address.application.usecase;

import com.geolink.findme.address.domain.exception.AddressAccessDeniedException;
import com.geolink.findme.address.domain.exception.AddressNotFoundException;
import com.geolink.findme.address.domain.model.Address;
import com.geolink.findme.address.domain.port.AddressRepositoryPort;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public class DeleteAddressUseCase {

    private final AddressRepositoryPort addressRepository;

    public DeleteAddressUseCase(AddressRepositoryPort addressRepository) {
        this.addressRepository = addressRepository;
    }

    @Transactional
    public void execute(UUID addressId, UUID requesterId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new AddressNotFoundException(addressId));
        if (!address.belongsTo(requesterId)) {
            throw new AddressAccessDeniedException();
        }
        addressRepository.deleteById(addressId);
    }
}
