package com.geolink.findme.business.service;

import com.geolink.findme.business.exception.AddressAccessDeniedException;
import com.geolink.findme.business.exception.AddressNotFoundException;
import com.geolink.findme.business.exception.DuplicateAddressException;
import com.geolink.findme.business.validation.AddressQuotaPolicy;
import com.geolink.findme.business.validation.GeoPointPolicy;
import com.geolink.findme.data.entity.Address;
import com.geolink.findme.data.repository.AddressRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;
    private final PhotoStorageService photoStorageService;
    private final QrCodeService qrCodeService;

    public AddressServiceImpl(AddressRepository addressRepository, PhotoStorageService photoStorageService,
                               QrCodeService qrCodeService) {
        this.addressRepository = addressRepository;
        this.photoStorageService = photoStorageService;
        this.qrCodeService = qrCodeService;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Address> listMyAddresses(UUID userId, Pageable pageable, String country, String city, String district) {
        return addressRepository.findPageByUser(userId, country, city, district, pageable);
    }

    @Override
    @Transactional
    public Address createAddress(UUID userId, String country, String city, String district, String street,
                                  String houseNumber, String postalCode, Double latitude, Double longitude) {
        AddressQuotaPolicy.ensureCanCreate(addressRepository.countByUserId(userId));

        if (addressRepository
                .existsByUserIdAndCountryIgnoreCaseAndCityIgnoreCaseAndDistrictIgnoreCaseAndStreetIgnoreCaseAndHouseNumberIgnoreCase(
                        userId, country, city, district, street, houseNumber)) {
            throw new DuplicateAddressException();
        }
        GeoPointPolicy.validate(latitude, longitude);

        Instant now = Instant.now();
        Address address = new Address();
        address.setId(UUID.randomUUID());
        address.setUserId(userId);
        address.setCountry(country);
        address.setCity(city);
        address.setDistrict(district);
        address.setStreet(street);
        address.setHouseNumber(houseNumber);
        address.setPostalCode(postalCode);
        address.setLatitude(latitude);
        address.setLongitude(longitude);
        address.setCreatedAt(now);
        address.setUpdatedAt(now);

        return addressRepository.save(address);
    }

    @Override
    @Transactional(readOnly = true)
    public Address getAddress(UUID addressId, UUID requesterId) {
        return findOwned(addressId, requesterId);
    }

    @Override
    @Transactional
    public Address updateAddress(UUID addressId, UUID requesterId, String country, String city, String district,
                                  String street, String houseNumber, String postalCode, Double latitude, Double longitude) {
        Address address = findOwned(addressId, requesterId);

        boolean identityChanged = !address.sameIdentityAs(country, city, district, street, houseNumber);
        if (identityChanged && addressRepository
                .existsByUserIdAndCountryIgnoreCaseAndCityIgnoreCaseAndDistrictIgnoreCaseAndStreetIgnoreCaseAndHouseNumberIgnoreCase(
                        requesterId, country, city, district, street, houseNumber)) {
            throw new DuplicateAddressException();
        }
        GeoPointPolicy.validate(latitude, longitude);

        address.setCountry(country);
        address.setCity(city);
        address.setDistrict(district);
        address.setStreet(street);
        address.setHouseNumber(houseNumber);
        address.setPostalCode(postalCode);
        address.setLatitude(latitude);
        address.setLongitude(longitude);
        address.setUpdatedAt(Instant.now());

        return addressRepository.save(address);
    }

    @Override
    @Transactional
    public void deleteAddress(UUID addressId, UUID requesterId) {
        findOwned(addressId, requesterId);
        addressRepository.deleteById(addressId);
    }

    @Override
    @Transactional
    public Address uploadPhoto(UUID addressId, UUID requesterId, byte[] bytes, String contentType, long sizeBytes) {
        Address address = findOwned(addressId, requesterId);

        photoStorageService.validate(bytes, contentType, sizeBytes);
        String url = photoStorageService.store(address.getId(), bytes, contentType);
        address.setPhotoUrl(url);
        address.setUpdatedAt(Instant.now());

        return addressRepository.save(address);
    }

    @Override
    @Transactional(readOnly = true)
    public AddressExportData exportAddress(UUID addressId, UUID requesterId) {
        Address address = findOwned(addressId, requesterId);
        String qrContent = "findme:address:" + address.getId();
        return new AddressExportData(address, qrCodeService.generatePngBase64(qrContent));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Address> listAddressesForAdmin(Pageable pageable, String country, String city, String district) {
        return addressRepository.findPageForAdmin(country, city, district, pageable);
    }

    private Address findOwned(UUID addressId, UUID requesterId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new AddressNotFoundException(addressId));
        if (!address.belongsTo(requesterId)) {
            throw new AddressAccessDeniedException();
        }
        return address;
    }
}
