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

    // :search est casté en string : sur PostgreSQL un paramètre null non typé est lié en bytea,
    // ce qui fait échouer CONCAT/LOWER. Le CAST fixe le type texte du placeholder.
    @Query("""
            SELECT u FROM UserJpaEntity u
            WHERE :search IS NULL
               OR LOWER(u.email) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
               OR LOWER(u.firstName) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
               OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
            """)
    Page<UserJpaEntity> search(@Param("search") String search, Pageable pageable);
}
