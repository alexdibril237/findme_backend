package com.geolink.findme.business.service;

import com.geolink.findme.data.entity.Address;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface AddressService {

    Page<Address> listMyAddresses(UUID userId, Pageable pageable, String country, String city, String district);

    Address createAddress(UUID userId, String country, String city, String district, String street,
                           String houseNumber, String postalCode, Double latitude, Double longitude);

    Address getAddress(UUID addressId, UUID requesterId);

    Address updateAddress(UUID addressId, UUID requesterId, String country, String city, String district,
                           String street, String houseNumber, String postalCode, Double latitude, Double longitude);

    void deleteAddress(UUID addressId, UUID requesterId);

    Address uploadPhoto(UUID addressId, UUID requesterId, byte[] bytes, String contentType, long sizeBytes);

    AddressExportData exportAddress(UUID addressId, UUID requesterId);

    /** Utilisé par les endpoints admin (vue transverse, tous utilisateurs). */
    Page<Address> listAddressesForAdmin(Pageable pageable, String country, String city, String district);
}
