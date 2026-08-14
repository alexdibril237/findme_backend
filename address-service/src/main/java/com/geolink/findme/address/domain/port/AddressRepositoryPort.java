package com.geolink.findme.address.domain.port;

import com.geolink.findme.address.domain.model.Address;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface AddressRepositoryPort {

    long countByUser(UUID userId);

    boolean existsSameIdentity(UUID userId, String country, String city, String district, String street, String houseNumber);

    Optional<Address> findById(UUID id);

    Page<Address> findPageByUser(UUID userId, Pageable pageable, AddressFilters filters);

    /** Utilisé par admin-service via l'endpoint interne (vue transverse, tous utilisateurs). */
    Page<Address> findPageForAdmin(Pageable pageable, AddressFilters filters);

    Address save(Address address);

    void deleteById(UUID id);

    record AddressFilters(String country, String city, String district) {
        public static AddressFilters none() {
            return new AddressFilters(null, null, null);
        }
    }
}


