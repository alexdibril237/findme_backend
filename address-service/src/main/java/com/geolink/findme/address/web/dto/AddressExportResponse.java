package com.geolink.findme.address.web.dto;

public record AddressExportResponse(
        AddressResponse address,
        String qrCodePngBase64
) {
}
