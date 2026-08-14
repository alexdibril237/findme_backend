package com.geolink.findme.admin.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SupportTicketJpaRepository extends JpaRepository<SupportTicketJpaEntity, UUID> {

    Page<SupportTicketJpaEntity> findByStatus(String status, Pageable pageable);
}
