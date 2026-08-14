package com.geolink.findme.address.infrastructure.persistence;

import com.geolink.findme.address.domain.model.Address;
import com.geolink.findme.address.domain.model.GeoPoint;
import com.geolink.findme.address.domain.port.AddressRepositoryPort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class AddressRepositoryAdapter implements AddressRepositoryPort {

    private final AddressJpaRepository jpaRepository;

    public AddressRepositoryAdapter(AddressJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public long countByUser(UUID userId) {
        return jpaRepository.countByUserId(userId);
    }

    @Override
    public boolean existsSameIdentity(UUID userId, String country, String city, String district, String street,
                                       String houseNumber) {
        return jpaRepository
                .existsByUserIdAndCountryIgnoreCaseAndCityIgnoreCaseAndDistrictIgnoreCaseAndStreetIgnoreCaseAndHouseNumberIgnoreCase(
                        userId, country, city, district, street, houseNumber);
    }

    @Override
    public Optional<Address> findById(UUID id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Page<Address> findPageByUser(UUID userId, Pageable pageable, AddressFilters filters) {
        return jpaRepository.findPageByUser(userId, filters.country(), filters.city(), filters.district(), pageable)
                .map(this::toDomain);
    }

    @Override
    public Page<Address> findPageForAdmin(Pageable pageable, AddressFilters filters) {
        return jpaRepository.findPageForAdmin(filters.country(), filters.city(), filters.district(), pageable)
                .map(this::toDomain);
    }

    @Override
    public Address save(Address address) {
        return toDomain(jpaRepository.save(toEntity(address)));
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }

    private Address toDomain(AddressJpaEntity entity) {
        GeoPoint location = (entity.getLatitude() == null && entity.getLongitude() == null)
                ? null
                : new GeoPoint(entity.getLatitude(), entity.getLongitude());
        return Address.rehydrate(entity.getId(), entity.getUserId(), entity.getCountry(), entity.getCity(),
                entity.getDistrict(), entity.getStreet(), entity.getHouseNumber(), entity.getPostalCode(),
                location, entity.getPhotoUrl(), entity.getCreatedAt(), entity.getUpdatedAt());
    }

    private AddressJpaEntity toEntity(Address address) {
        GeoPoint location = address.getLocation();
        Double lat = location == null ? null : location.latitude();
        Double lng = location == null ? null : location.longitude();
        return new AddressJpaEntity(address.getId(), address.getUserId(), address.getCountry(), address.getCity(),
                address.getDistrict(), address.getStreet(), address.getHouseNumber(), address.getPostalCode(),
                lat, lng, address.getPhotoUrl(), address.getCreatedAt(), address.getUpdatedAt());
    }
}
