package com.geolink.findme.address.application.usecase;

import com.geolink.findme.address.domain.exception.AddressAccessDeniedException;
import com.geolink.findme.address.domain.exception.AddressNotFoundException;
import com.geolink.findme.address.domain.model.Address;
import com.geolink.findme.address.domain.port.AddressRepositoryPort;
import com.geolink.findme.address.domain.port.PhotoContent;
import com.geolink.findme.address.domain.port.PhotoStoragePort;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public class UploadAddressPhotoUseCase {

    private final AddressRepositoryPort addressRepository;
    private final PhotoStoragePort photoStorage;

    public UploadAddressPhotoUseCase(AddressRepositoryPort addressRepository, PhotoStoragePort photoStorage) {
        this.addressRepository = addressRepository;
        this.photoStorage = photoStorage;
    }

    public record Command(UUID addressId, UUID requesterId, PhotoContent content) {
    }

    @Transactional
    public Address execute(Command command) {
        Address address = addressRepository.findById(command.addressId())
                .orElseThrow(() -> new AddressNotFoundException(command.addressId()));
        if (!address.belongsTo(command.requesterId())) {
            throw new AddressAccessDeniedException();
        }

        photoStorage.validate(command.content());
        String url = photoStorage.store(address.getId(), command.content());
        address.attachPhoto(url);

        return addressRepository.save(address);
    }
}
