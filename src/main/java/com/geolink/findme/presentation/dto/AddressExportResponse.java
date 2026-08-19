package com.geolink.findme.presentation.dto;

public record AddressExportResponse(
        AddressResponse address,
        String qrCodePngBase64
) {
}
