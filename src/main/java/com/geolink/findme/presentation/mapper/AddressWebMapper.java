package com.geolink.findme.presentation.mapper;

import com.geolink.findme.business.service.AddressExportData;
import com.geolink.findme.data.entity.Address;
import com.geolink.findme.presentation.dto.AddressExportResponse;
import com.geolink.findme.presentation.dto.AddressResponse;
import com.geolink.findme.presentation.dto.AddressSummaryResponse;
import org.springframework.stereotype.Component;

@Component
public class AddressWebMapper {

    public AddressResponse toResponse(Address address) {
        return new AddressResponse(
                address.getId(),
                address.getUserId(),
                address.getCountry(),
                address.getCity(),
                address.getDistrict(),
                address.getStreet(),
                address.getHouseNumber(),
                address.getPostalCode(),
                address.getLatitude(),
                address.getLongitude(),
                address.getPhotoUrl(),
                address.getCreatedAt(),
                address.getUpdatedAt()
        );
    }

    public AddressSummaryResponse toSummaryResponse(Address address) {
        return new AddressSummaryResponse(
                address.getId(),
                address.getUserId(),
                address.getCountry(),
                address.getCity(),
                address.getDistrict(),
                address.getStreet(),
                address.getHouseNumber(),
                address.getCreatedAt()
        );
    }

    public AddressExportResponse toExportResponse(AddressExportData data) {
        return new AddressExportResponse(toResponse(data.address()), data.qrCodePngBase64());
    }
}
