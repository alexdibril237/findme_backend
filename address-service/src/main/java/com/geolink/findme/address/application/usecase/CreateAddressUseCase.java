package com.geolink.findme.address.application.usecase;

import com.geolink.findme.address.domain.exception.DuplicateAddressException;
import com.geolink.findme.address.domain.model.Address;
import com.geolink.findme.address.domain.model.AddressQuotaPolicy;
import com.geolink.findme.address.domain.model.GeoPoint;
import com.geolink.findme.address.domain.port.AddressRepositoryPort;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public class

CreateAddressUseCase {

    private final AddressRepositoryPort addressRepository;

    public CreateAddressUseCase(AddressRepositoryPort addressRepository) {
        this.addressRepository = addressRepository;
    }

    public record Command(UUID userId, String country, String city, String district, String street,
                           String houseNumber, String postalCode, Double latitude, Double longitude) {
    }

    @Transactional
    public Address execute(Command command) {
        AddressQuotaPolicy.ensureCanCreate(addressRepository.countByUser(command.userId()));

        if (addressRepository.existsSameIdentity(command.userId(), command.country(), command.city(),
                command.district(), command.street(), command.houseNumber())) {
            throw new DuplicateAddressException();
        }

        Address address = Address.createNew(command.userId(), command.country(), command.city(), command.district(),
                command.street(), command.houseNumber(), command.postalCode(),
                new GeoPoint(command.latitude(), command.longitude()));

        return addressRepository.save(address);
    }
}
