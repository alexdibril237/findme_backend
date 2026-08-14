package com.geolink.findme.auth.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UserJpaRepository extends JpaRepository<UserJpaEntity, UUID> {

    boolean existsByEmail(String email);

    Optional<UserJpaEntity> findByEmail(String email);

    @Query("""
            SELECT u FROM UserJpaEntity u
            WHERE :search IS NULL
               OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))
               OR LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%'))
               OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%'))
            """)
    Page<UserJpaEntity> search(@Param("search") String search, Pageable pageable);
}
