package com.geolink.findme.address.web.mapper;

import com.geolink.findme.address.application.usecase.AddressExportData;
import com.geolink.findme.address.domain.model.Address;
import com.geolink.findme.address.domain.model.GeoPoint;
import com.geolink.findme.address.web.dto.AddressExportResponse;
import com.geolink.findme.address.web.dto.AddressResponse;
import org.springframework.stereotype.Component;

@Component
public class AddressWebMapper {

    public AddressResponse toResponse(Address address) {
        GeoPoint location = address.getLocation();
        return new AddressResponse(
                address.getId(),
                address.getUserId(),
                address.getCountry(),
                address.getCity(),
                address.getDistrict(),
                address.getStreet(),
                address.getHouseNumber(),
                address.getPostalCode(),
                location == null ? null : location.latitude(),
                location == null ? null : location.longitude(),
                address.getPhotoUrl(),
                address.getCreatedAt(),
                address.getUpdatedAt()
        );
    }

    public AddressExportResponse toExportResponse(AddressExportData data) {
        return new AddressExportResponse(toResponse(data.address()), data.qrCodePngBase64());
    }
}
