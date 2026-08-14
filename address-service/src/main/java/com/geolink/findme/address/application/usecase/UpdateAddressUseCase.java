package com.geolink.findme.address.application.usecase;

import com.geolink.findme.address.domain.exception.AddressAccessDeniedException;
import com.geolink.findme.address.domain.exception.AddressNotFoundException;
import com.geolink.findme.address.domain.exception.DuplicateAddressException;
import com.geolink.findme.address.domain.model.Address;
import com.geolink.findme.address.domain.model.GeoPoint;
import com.geolink.findme.address.domain.port.AddressRepositoryPort;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public class UpdateAddressUseCase {

    private final AddressRepositoryPort addressRepository;

    public UpdateAddressUseCase(AddressRepositoryPort addressRepository) {
        this.addressRepository = addressRepository;
    }

    public record Command(UUID addressId, UUID requesterId, String country, String city, String district,
                           String street, String houseNumber, String postalCode, Double latitude, Double longitude) {
    }

    @Transactional
    public Address execute(Command command) {
        Address address = addressRepository.findById(command.addressId())
                .orElseThrow(() -> new AddressNotFoundException(command.addressId()));
        if (!address.belongsTo(command.requesterId())) {
            throw new AddressAccessDeniedException();
        }

        boolean identityChanged = !address.sameIdentityAs(command.country(), command.city(), command.district(),
                command.street(), command.houseNumber());
        if (identityChanged && addressRepository.existsSameIdentity(command.requesterId(), command.country(),
                command.city(), command.district(), command.street(), command.houseNumber())) {
            throw new DuplicateAddressException();
        }

        address.updateDetails(command.country(), command.city(), command.district(), command.street(),
                command.houseNumber(), command.postalCode(), new GeoPoint(command.latitude(), command.longitude()));

        return addressRepository.save(address);
    }
}
