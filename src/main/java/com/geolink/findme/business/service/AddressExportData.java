package com.geolink.findme.business.service;

import com.geolink.findme.data.entity.Address;

public record AddressExportData(Address address, String qrCodePngBase64) {
}
