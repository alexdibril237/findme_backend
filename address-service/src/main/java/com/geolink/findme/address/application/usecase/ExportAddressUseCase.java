package com.geolink.findme.address.application.usecase;

import com.geolink.findme.address.domain.exception.AddressAccessDeniedException;
import com.geolink.findme.address.domain.exception.AddressNotFoundException;
import com.geolink.findme.address.domain.model.Address;
import com.geolink.findme.address.domain.port.AddressRepositoryPort;
import com.geolink.findme.address.domain.port.QrCodeGeneratorPort;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public class ExportAddressUseCase {

    private final AddressRepositoryPort addressRepository;
    private final QrCodeGeneratorPort qrCodeGenerator;

    public ExportAddressUseCase(AddressRepositoryPort addressRepository, QrCodeGeneratorPort qrCodeGenerator) {
        this.addressRepository = addressRepository;
        this.qrCodeGenerator = qrCodeGenerator;
    }

    @Transactional(readOnly = true)
    public AddressExportData execute(UUID addressId, UUID requesterId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new AddressNotFoundException(addressId));
        if (!address.belongsTo(requesterId)) {
            throw new AddressAccessDeniedException();
        }

        String qrContent = "findme:address:" + address.getId();
        return new AddressExportData(address, qrCodeGenerator.generatePngBase64(qrContent));
    }
}
