package com.geolink.findme.address.application.usecase;

import com.geolink.findme.address.domain.model.Address;

public record AddressExportData(Address address, String qrCodePngBase64) {
}
