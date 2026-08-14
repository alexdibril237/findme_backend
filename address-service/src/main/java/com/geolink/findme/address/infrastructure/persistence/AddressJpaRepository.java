package com.geolink.findme.address.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface AddressJpaRepository extends JpaRepository<AddressJpaEntity, UUID> {

    long countByUserId(UUID userId);

    boolean existsByUserIdAndCountryIgnoreCaseAndCityIgnoreCaseAndDistrictIgnoreCaseAndStreetIgnoreCaseAndHouseNumberIgnoreCase(
            UUID userId, String country, String city, String district, String street, String houseNumber);

    @Query("""
            SELECT a FROM AddressJpaEntity a
            WHERE a.userId = :userId
              AND (:country IS NULL OR LOWER(a.country) = LOWER(:country))
              AND (:city IS NULL OR LOWER(a.city) = LOWER(:city))
              AND (:district IS NULL OR LOWER(a.district) = LOWER(:district))
            """)
    Page<AddressJpaEntity> findPageByUser(@Param("userId") UUID userId, @Param("country") String country,
                                           @Param("city") String city, @Param("district") String district,
                                           Pageable pageable);

    @Query("""
            SELECT a FROM AddressJpaEntity a
            WHERE (:country IS NULL OR LOWER(a.country) = LOWER(:country))
              AND (:city IS NULL OR LOWER(a.city) = LOWER(:city))
              AND (:district IS NULL OR LOWER(a.district) = LOWER(:district))
            """)
    Page<AddressJpaEntity> findPageForAdmin(@Param("country") String country, @Param("city") String city,
                                             @Param("district") String district, Pageable pageable);
}
