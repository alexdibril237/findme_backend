package com.geolink.findme.data.repository;

import com.geolink.findme.data.entity.Address;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface AddressRepository extends JpaRepository<Address, UUID> {

    long countByUserId(UUID userId);

    boolean existsByAddressCode(String addressCode);

    boolean existsByUserIdAndCountryIgnoreCaseAndCityIgnoreCaseAndDistrictIgnoreCaseAndStreetIgnoreCaseAndHouseNumberIgnoreCase(
            UUID userId, String country, String city, String district, String street, String houseNumber);

    // Les paramètres de filtre sont castés en string : sur PostgreSQL, un paramètre null non typé
    // est lié en bytea, ce qui fait échouer LOWER(bytea). Le CAST fixe le type texte du placeholder.
    @Query("""
            SELECT a FROM Address a
            WHERE a.userId = :userId
              AND (:country IS NULL OR LOWER(a.country) = LOWER(CAST(:country AS string)))
              AND (:city IS NULL OR LOWER(a.city) = LOWER(CAST(:city AS string)))
              AND (:district IS NULL OR LOWER(a.district) = LOWER(CAST(:district AS string)))
            """)
    Page<Address> findPageByUser(@Param("userId") UUID userId, @Param("country") String country,
                                  @Param("city") String city, @Param("district") String district,
                                  Pageable pageable);

    @Query("""
            SELECT a FROM Address a
            WHERE (:country IS NULL OR LOWER(a.country) = LOWER(CAST(:country AS string)))
              AND (:city IS NULL OR LOWER(a.city) = LOWER(CAST(:city AS string)))
              AND (:district IS NULL OR LOWER(a.district) = LOWER(CAST(:district AS string)))
            """)
    Page<Address> findPageForAdmin(@Param("country") String country, @Param("city") String city,
                                    @Param("district") String district, Pageable pageable);
}
